import logging
import grpc
from fastapi import FastAPI
from app.api.search import router as search_router

from app.grpc_gen import search_pb2_grpc
from app.grpc_server import SearchServiceImpl  # DÙNG CLASS RIÊNG
# ⚠️ KHÔNG ĐỊNH NGHĨA SearchServiceImpl 2 LẦN

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Search Service",
    version="1.0.0"
)

app.include_router(search_router)

# 👉 BIẾN GIỮ SERVER (CỰC KỲ QUAN TRỌNG)
grpc_server = None


@app.on_event("startup")
async def startup_event():
    global grpc_server

    grpc_server = grpc.aio.server()
    search_pb2_grpc.add_SearchServiceServicer_to_server(
        SearchServiceImpl(), grpc_server
    )

    grpc_server.add_insecure_port("[::]:50051")
    await grpc_server.start()

    logger.info("🚀 gRPC Search Service STARTED at port 50051")


@app.on_event("shutdown")
async def shutdown_event():
    global grpc_server
    if grpc_server:
        await grpc_server.stop(0)
        logger.info("🛑 gRPC Search Service STOPPED")


@app.get("/health")
def health():
    return {
        "status": "UP",
        "grpc_port": 50051,
        "http_port": 8090
    }
