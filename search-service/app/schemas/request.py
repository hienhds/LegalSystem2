from pydantic import BaseModel, Field

class SearchRequest(BaseModel):
    text: str = Field(..., min_length=1)
    top_k: int = 5
