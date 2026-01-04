from pydantic import BaseModel
from typing import List, Optional

class DieuResult(BaseModel):
    id: str
    tieu_de: Optional[str]
    noi_dung: list[str]
    score: float


class SearchResponse(BaseModel):
    results: List[DieuResult]
