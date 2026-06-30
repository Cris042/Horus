# syntax=docker/dockerfile:1
# Imagem da API de teste de carga (FastAPI) — T-801, RF-032/RNF-004.
# Build a partir de loadtest/ (contexto = este diretório):
#   docker build -t horus/loadtest:dev loadtest/
# Python 3.13 / Uvicorn (ver lib.md). App ASGI: app.main:app.

FROM python:3.13-slim AS runtime
WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1
# Instala as dependências declaradas no pyproject (FastAPI, Uvicorn, Locust, OTel).
# README.md é referenciado pelo pyproject (campo readme) — necessário no build.
COPY pyproject.toml README.md ./
COPY app ./app
RUN pip install --no-cache-dir . \
    && useradd -r horus
USER horus
EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
