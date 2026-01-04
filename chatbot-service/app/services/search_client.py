import grpc
import os
import logging

# Import code sinh ra
from app.grpc_gen import search_pb2
from app.grpc_gen import search_pb2_grpc

logger = logging.getLogger(__name__)

# Lấy địa chỉ từ biến môi trường
SEARCH_HOST = os.getenv("SEARCH_SERVICE_HOST", "search-service")
SEARCH_PORT = os.getenv("SEARCH_SERVICE_PORT", "50051")

async def semantic_search(text: str):
    target = f"{SEARCH_HOST}:{SEARCH_PORT}"
    
    # Mở kênh kết nối (Channel)
    async with grpc.aio.insecure_channel(target) as channel:
        
        # === ĐÂY LÀ CLASS CLIENT (STUB) ===
        stub = search_pb2_grpc.SearchServiceStub(channel)
        
        # Tạo request object
        req = search_pb2.SearchRequest(text=text, top_k=5)
        
        try:
            logger.info(f"Đang gọi gRPC tới {target}...")
            
            # Gọi hàm (có timeout để tránh treo)
            response = await stub.SemanticSearch(req, timeout=30)
            
            # Chuyển đổi dữ liệu từ gRPC object về Dict (cho code cũ hiểu)
            results = []
            for r in response.results:
                results.append({
                    "id": r.id,
                    "tieu_de": r.tieu_de,
                    "noi_dung": r.noi_dung,
                    "score": r.score
                })
            return {"results": results}
            
        except grpc.RpcError as e:
            logger.error(f"Lỗi gọi gRPC: code={e.code()}, message={e.details()}")
            return {"results": []}