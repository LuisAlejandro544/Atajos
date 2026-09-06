#!/usr/bin/env bash
set -euo pipefail

echo "============================================="
echo "🗣️ Configurando y descargando Piper TTS (Voces Neuronales)"
echo "============================================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ASSETS_PIPER_DIR="${PROJECT_ROOT}/app/src/main/assets/piper"

echo "Verificando dependencias de ONNX Runtime en Gradle..."
if grep -q "onnxruntime-android" "${PROJECT_ROOT}/gradle/libs.versions.toml" && grep -q "libs.onnxruntime.android" "${PROJECT_ROOT}/app/build.gradle.kts"; then
    echo "✅ Dependencia com.microsoft.onnxruntime:onnxruntime-android verificada en Gradle."
else
    echo "❌ Error: Falta la dependencia onnxruntime-android en Gradle."
    exit 1
fi

mkdir -p "${ASSETS_PIPER_DIR}"

BASE_URL="https://huggingface.co/rhasspy/piper-voices/resolve/v1.0.0/es/es_ES/carlfm/x_low"
MODEL_FILE="es_ES-carlfm-x_low.onnx"
CONFIG_FILE="es_ES-carlfm-x_low.onnx.json"

echo "📦 Comprobando modelo empaquetado de Piper TTS en assets (${MODEL_FILE})..."
if [ -f "${ASSETS_PIPER_DIR}/${MODEL_FILE}" ] && [ -s "${ASSETS_PIPER_DIR}/${MODEL_FILE}" ]; then
    echo "✅ Modelo ${MODEL_FILE} ya presente en assets/piper/."
else
    echo "⬇️ Descargando modelo ${MODEL_FILE} desde HuggingFace..."
    curl -L "${BASE_URL}/${MODEL_FILE}" -o "${ASSETS_PIPER_DIR}/${MODEL_FILE}"
fi

if [ -f "${ASSETS_PIPER_DIR}/${CONFIG_FILE}" ] && [ -s "${ASSETS_PIPER_DIR}/${CONFIG_FILE}" ]; then
    echo "✅ Configuración ${CONFIG_FILE} ya presente en assets/piper/."
else
    echo "⬇️ Descargando configuración ${CONFIG_FILE} desde HuggingFace..."
    curl -L "${BASE_URL}/${CONFIG_FILE}" -o "${ASSETS_PIPER_DIR}/${CONFIG_FILE}"
fi

echo "✅ Piper TTS configurado y listo con síntesis neuronal VITS offline en español."
