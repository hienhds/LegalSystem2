from app.services.search_client import semantic_search
import logging
from typing import List, Dict

logger = logging.getLogger(__name__)

class RetrievalStage:
    """
    Giai đoạn 4: Truy xuất thông tin
    - Tìm kiếm điều luật liên quan cho từng query con
    - Gộp và loại bỏ trùng lặp
    - Sắp xếp theo độ liên quan (score)
    """
    
    def __init__(self, top_k_per_query: int = 5, max_total_results: int = 10):
        """
        Args:
            top_k_per_query: Số kết quả tối đa cho mỗi query con
            max_total_results: Số kết quả tối đa sau khi gộp
        """
        self.top_k_per_query = top_k_per_query
        self.max_total_results = max_total_results

    async def run(self, queries: List[str]) -> List[Dict]:
        """
        Tìm kiếm điều luật cho tất cả các queries
        
        Args:
            queries: Danh sách truy vấn từ Stage 3
            
        Returns:
            List[Dict]: Danh sách điều luật đã được gộp và sắp xếp
        """
        logger.info(f"[STAGE 4] Bắt đầu tìm kiếm cho {len(queries)} queries")
        
        all_results = []
        seen_ids = set()  # Để loại bỏ trùng lặp
        
        # Tìm kiếm cho từng query
        for idx, query in enumerate(queries, 1):
            logger.info(f"[STAGE 4] Tìm kiếm query {idx}/{len(queries)}: {query[:60]}...")
            
            try:
                search_result = await semantic_search(query)
                results = search_result.get("results", [])
                
                logger.info(f"[STAGE 4] Query {idx} tìm thấy {len(results)} kết quả")
                
                # Thêm kết quả, loại bỏ trùng lặp
                for result in results[:self.top_k_per_query]:
                    doc_id = result.get("id")
                    if doc_id and doc_id not in seen_ids:
                        seen_ids.add(doc_id)
                        all_results.append(result)
                        
            except Exception as e:
                logger.error(f"[STAGE 4] Lỗi tìm kiếm query {idx}: {e}")
                continue
        
        # Sắp xếp theo score giảm dần
        all_results.sort(key=lambda x: x.get("score", 0), reverse=True)
        
        # Giới hạn số lượng kết quả
        final_results = all_results[:self.max_total_results]
        
        logger.info(f"[STAGE 4] Tổng cộng {len(final_results)} điều luật sau khi gộp và lọc")
        
        # Log top 3 kết quả để debug
        for idx, result in enumerate(final_results[:3], 1):
            logger.info(f"  Top {idx}: {result.get('tieu_de', 'N/A')[:60]}... (score: {result.get('score', 0):.3f})")
        
        return final_results