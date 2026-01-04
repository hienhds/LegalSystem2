import grpc
import logging
from concurrent import futures
import asyncio

# Import các file vừa sinh ra
# Lưu ý: Cần chỉnh import trong file _pb2_grpc.py nếu lỗi, hoặc dùng sys.path
from app.grpc_gen import search_pb2
from app.grpc_gen import search_pb2_grpc

# Import logic cũ của bạn
from app.services.embedding_service import embed_text
from app.core.mongo import db
from app.core.config import COLLECTION_DIEU, VECTOR_INDEX

logger = logging.getLogger(__name__)

# === ĐÂY LÀ CLASS QUAN TRỌNG NHẤT ===
class SearchServiceImpl(search_pb2_grpc.SearchServiceServicer):
    
    # Ghi đè hàm SemanticSearch đã định nghĩa trong proto
    def SemanticSearch(self, request, context):
        try:
            print(f"DEBUG: Nhận request gRPC tìm kiếm: {request.text}")
            
            # 1. Logic Embedding (như code cũ)
            vector = embed_text(request.text)

            # 2. Logic Query MongoDB (như code cũ)
            pipeline = [
                {
                    "$vectorSearch": {
                        "index": VECTOR_INDEX,
                        "path": "embedding",
                        "queryVector": vector,
                        "numCandidates": 100,
                        "limit": request.top_k
                    }
                },
                {
                    "$project": {
                        "_id": 1, "tieu_de": 1, "noi_dung": 1,
                        "score": {"$meta": "vectorSearchScore"}
                    }
                }
            ]
            
            # Lưu ý: gRPC mặc định chạy thread, nên gọi db synchronous cũng ok
            docs = list(db[COLLECTION_DIEU].aggregate(pipeline))
            
            # 3. Chuyển đổi dữ liệu MongoDB sang format gRPC (Protobuf)
            grpc_results = []
            for d in docs:
                grpc_results.append(search_pb2.SearchResult(
                    id=str(d["_id"]),
                    tieu_de=d.get("tieu_de", "") or "",
                    noi_dung=str(d.get("noi_dung", "")) or "", # Ép kiểu string
                    score=d.get("score", 0.0)
                ))

            return search_pb2.SearchResponse(results=grpc_results)

        except Exception as e:
            logger.error(f"Lỗi Server gRPC: {e}")
            context.set_details(str(e))
            context.set_code(grpc.StatusCode.INTERNAL)
            return search_pb2.SearchResponse()

# Hàm chạy server
async def serve():
    server = grpc.aio.server()
    search_pb2_grpc.add_SearchServiceServicer_to_server(SearchServiceImpl(), server)
    
    # Lắng nghe ở cổng 50051
    server.add_insecure_port('[::]:50051')
    print("Search Service gRPC đang chạy ở port 50051...")
    await server.start()
    await server.wait_for_termination()

if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    asyncio.run(serve())