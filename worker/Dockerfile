# syntax=docker/dockerfile:1
# Imagem do worker Rust de relatório/e-mail (T-801, RF-032/RNF-004/RNF-012).
# Build a partir de worker/ (contexto = este diretório):
#   docker build -t horus/report-worker:dev worker/
# Rust 1.85 (edição 2024 — ver lib.md); binário: horus-report-worker.

FROM rust:1-slim AS build
WORKDIR /build
# Cache de dependências: compila só os manifestos primeiro (com um main vazio),
# depois o código real — assim mudanças no src não reinstalam todas as crates.
COPY Cargo.toml Cargo.lock ./
RUN mkdir src && echo "fn main() {}" > src/main.rs \
    && cargo build --release \
    && rm -rf src
COPY src ./src
RUN touch src/main.rs && cargo build --release

FROM debian:bookworm-slim AS runtime
# Certificados TLS para SMTP/AMQP/OTLP sobre TLS.
RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r horus && useradd -r -g horus horus
COPY --from=build /build/target/release/horus-report-worker /usr/local/bin/horus-report-worker
USER horus
ENTRYPOINT ["horus-report-worker"]
