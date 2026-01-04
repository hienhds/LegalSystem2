# ================== BASE IMAGE ==================
FROM python:3.12.6-slim

# ================== SYSTEM CONFIG ==================
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PORT=8090

WORKDIR /app

# ================== SYSTEM DEPENDENCIES ==================
# Needed for torch / sentence-transformers
RUN apt-get update && apt-get install -y \
    build-essential \
    git \
    curl \
    && rm -rf /var/lib/apt/lists/*

# ================== COPY REQUIREMENTS ==================
COPY requirements.txt .

# ================== CREATE VENV ==================
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# ================== INSTALL PYTHON LIBS ==================
RUN pip install --upgrade pip setuptools wheel \
    && pip install --no-cache-dir -r requirements.txt

# ================== COPY APPLICATION ==================
COPY app ./app

# ================== EXPOSE PORT ==================
EXPOSE 8090

# ================== START SERVICE ==================
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8091"]
