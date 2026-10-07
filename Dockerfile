FROM nvidia/cuda:12.1.1-cudnn8-runtime-ubuntu22.04

ARG KATAGO_VERSION=1.18.2

ENV DEBIAN_FRONTEND=noninteractive

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        wget \
        unzip \
        ca-certificates \
        zlib1g \
        libzip4 \
    && rm -rf /var/lib/apt/lists/*

RUN wget -q \
    "https://github.com/lightvector/KataGo/releases/download/v${KATAGO_VERSION}/katago-v${KATAGO_VERSION}-cuda12.1-cudnn8.9.7-linux-x64.zip" \
    -O /tmp/katago.zip \
    && unzip /tmp/katago.zip -d /tmp/katago \
    && install -m 0755 \
        "$(find /tmp/katago -type f -name katago | head -n 1)" \
        /usr/local/bin/katago \
    && rm -rf /tmp/katago /tmp/katago.zip

COPY analysis.cfg /app/analysis.cfg
COPY model.bin.gz /app/model.bin.gz

ENV KATAGO_BIN=/usr/local/bin/katago
ENV KATAGO_CONFIG=/app/analysis.cfg
ENV KATAGO_MODEL=/app/model.bin.gz
