from app.pipelines.stage_1_classification import ClassificationStage
from app.pipelines.stage_2_contextualize import ContextualizeStage
from app.pipelines.stage_3_decomposition import DecompositionStage
from app.pipelines.stage_4_retrieval import RetrievalStage
from app.pipelines.stage_6_generation import GenerationStage
from app.services.memory_workspace import MemoryWorkspace
import logging

logger = logging.getLogger(__name__)

class ChatbotPipeline:
    """
    Pipeline xử lý chatbot pháp lý 6 giai đoạn:
    1. Classification: Phân loại câu hỏi
    2. Contextualize: Tái cấu trúc với ngữ cảnh
    3. Decomposition: Phân rã thành nhiều queries
    4. Retrieval: Tìm kiếm điều luật
    5. Augmentation: (Tạm thời bỏ qua)
    6. Generation: Tạo câu trả lời
    
    Memory Strategy:
    - Lưu tối đa 10 lượt hỏi-trả lời
    - LRU: Xóa conversation cũ nhất khi vượt giới hạn
    - TTL: 7 ngày
    """
    
    def __init__(self):
        self.stage_1 = ClassificationStage()
        self.stage_2 = ContextualizeStage()
        self.stage_3 = DecompositionStage()
        self.stage_4 = RetrievalStage(top_k_per_query=5, max_total_results=10)
        self.stage_6 = GenerationStage()
        self.memory = MemoryWorkspace()

    async def process(self, user_id: str, question: str):
        """
        Xử lý câu hỏi qua toàn bộ pipeline
        
        Args:
            user_id: ID người dùng (để quản lý memory)
            question: Câu hỏi từ người dùng
            
        Returns:
            dict: Kết quả xử lý với các trường:
                - status: "OK" | "REJECTED" | "ERROR"
                - answer: Câu trả lời
                - context: Các điều luật liên quan (nếu có)
                - metadata: Thông tin chi tiết từng giai đoạn
        """
        logger.info(f"========== BẮT ĐẦU PIPELINE cho user {user_id} ==========")
        logger.info(f"Câu hỏi: {question}")
        
        metadata = {}
        
        try:
            # ===== LOAD MEMORY =====
            try:
                memory_messages = self.memory.get_conversation_history(user_id)
                memory_stats = self.memory.get_stats(user_id)
                logger.info(f"Memory: {memory_stats['total_turns']} turns, {memory_stats['total_messages']} messages")
                metadata["memory_stats"] = memory_stats
            except Exception as mem_error:
                logger.warning(f"Lỗi load memory: {mem_error}, tiếp tục với memory trống")
                memory_messages = []
                metadata["memory_stats"] = {"error": str(mem_error)}
            
            # ===== STAGE 1: CLASSIFICATION =====
            q_type = await self.stage_1.run(question)
            metadata["classification"] = q_type
            
            if q_type == "TOXIC":
                logger.warning("Pipeline dừng: Nội dung TOXIC")
                return {
                    "status": "REJECTED",
                    "answer": "Câu hỏi của bạn có nội dung không phù hợp. Vui lòng đặt câu hỏi lịch sự và đúng mực.",
                    "context": None,
                    "metadata": metadata
                }
            
            if q_type == "NON_LEGAL":
                logger.warning("Pipeline dừng: Không liên quan pháp luật")
                return {
                    "status": "REJECTED",
                    "answer": "Tôi chỉ có thể tư vấn các vấn đề liên quan đến pháp luật Việt Nam. Câu hỏi của bạn không thuộc lĩnh vực này.",
                    "context": None,
                    "metadata": metadata
                }
            
            # ===== SAVE QUESTION TO MEMORY (trước khi xử lý) =====
            self.memory.append_question(user_id, question)
            
            # ===== STAGE 2: CONTEXTUALIZE =====
            contextualized_question = await self.stage_2.run(question, memory_messages)
            metadata["contextualized_question"] = contextualized_question
            
            # ===== STAGE 3: DECOMPOSITION =====
            queries = await self.stage_3.run(contextualized_question)
            metadata["queries"] = queries
            metadata["num_queries"] = len(queries)
            
            # ===== STAGE 4: RETRIEVAL =====
            legal_docs = await self.stage_4.run(queries)
            metadata["num_legal_docs"] = len(legal_docs)
            
            if not legal_docs:
                logger.warning("Không tìm thấy điều luật liên quan")
                no_result_answer = "Tôi không tìm thấy điều luật phù hợp với câu hỏi của bạn. Vui lòng diễn đạt lại hoặc hỏi chi tiết hơn."
                
                # Lưu answer vào memory
                self.memory.update_answer(user_id, no_result_answer)
                
                return {
                    "status": "OK",
                    "answer": no_result_answer,
                    "context": [],
                    "metadata": metadata
                }
            
            # ===== STAGE 6: GENERATION =====
            final_answer = await self.stage_6.run(question, legal_docs)
            
            # ===== SAVE ANSWER TO MEMORY =====
            self.memory.update_answer(user_id, final_answer)
            logger.info(f"Đã lưu câu trả lời vào memory")
            
            logger.info("========== PIPELINE HOÀN THÀNH ==========")
            
            return {
                "status": "OK",
                "answer": final_answer,
                "context": legal_docs,
                "metadata": metadata
            }
            
        except Exception as e:
            logger.error(f"LỖI TRONG PIPELINE: {e}", exc_info=True)
            error_answer = "Đã xảy ra lỗi khi xử lý câu hỏi của bạn. Vui lòng thử lại sau."
            
            # Vẫn cố gắng lưu vào memory
            try:
                self.memory.update_answer(user_id, error_answer)
            except:
                pass
            
            return {
                "status": "ERROR",
                "answer": error_answer,
                "context": None,
                "metadata": metadata
            }