from fastapi import APIRouter, HTTPException
from bson import ObjectId

from app.schemas.request import SearchRequest
from app.schemas.response import SearchResponse, DieuResult
from app.services.embedding_service import embed_text
from app.core.mongo import db
from app.core.config import COLLECTION_DIEU, VECTOR_INDEX

router = APIRouter(prefix="/search", tags=["Search"])


@router.post("", response_model=SearchResponse)
def semantic_search(req: SearchRequest):
    text = req.text.strip()
    if not text:
        raise HTTPException(status_code=400, detail="Text must not be empty")

    vector = embed_text(text)

    pipeline = [
        {
            "$vectorSearch": {
                "index": VECTOR_INDEX,
                "path": "embedding",
                "queryVector": vector,
                "numCandidates": 100,
                "limit": req.top_k
            }
        },
        {
            "$project": {
                "_id": 1,
                "tieu_de": 1,
                "noi_dung": 1,
                "score": {"$meta": "vectorSearchScore"}
            }
        }
    ]

    docs = db[COLLECTION_DIEU].aggregate(pipeline)

    results = []
    for d in docs:
        results.append(
            DieuResult(
                id=str(d["_id"]),
                tieu_de=d.get("tieu_de"),
                noi_dung=d.get("noi_dung", []),
                score=d["score"]
            )
        )

    return SearchResponse(results=results)
