FROM python:3.12.6

WORKDIR /app

COPY requirements.txt .
RUN pip install  -r requirements.txt

COPY app ./app
COPY models ./models

ENV PORT=8090

EXPOSE 8090

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8090"]
