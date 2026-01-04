from pymongo import MongoClient
from app.core.config import MONGO_URI, MONGO_DB

client = MongoClient(
    MONGO_URI,
    tls=True,
    tlsAllowInvalidCertificates=True
)

db = client[MONGO_DB]
