#!/usr/bin/env bash
set -euo pipefail

echo "=========================================================="
echo "🎵 Conversión y Optimización de Sonidos de Notificación"
echo "=========================================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

SOURCE_DIR="${PROJECT_ROOT}/audio_assets/raw"
OUTPUT_DIR="${PROJECT_ROOT}/app/src/main/res/raw"

mkdir -p "${OUTPUT_DIR}"

if command -v ffmpeg >/dev/null 2>&1; then
    echo ">> [1/2] ffmpeg detectado en el sistema."
    
    # Archivo pop-up notification de GabrielAraujo (CC0)
    INPUT_WAV=""
    if [ -f "${SOURCE_DIR}/pop_notification.wav" ]; then
        INPUT_WAV="${SOURCE_DIR}/pop_notification.wav"
    elif [ -f "${SOURCE_DIR}/242502__gabrielaraujo__pop-upnotification.wav" ]; then
        INPUT_WAV="${SOURCE_DIR}/242502__gabrielaraujo__pop-upnotification.wav"
    elif [ -f "${PROJECT_ROOT}/242502__gabrielaraujo__pop-upnotification.wav" ]; then
        INPUT_WAV="${PROJECT_ROOT}/242502__gabrielaraujo__pop-upnotification.wav"
    fi

    if [ -n "${INPUT_WAV}" ]; then
        TARGET_OGG="${OUTPUT_DIR}/pop_notification.ogg"
        echo ">> [2/2] Convirtiendo $(basename "${INPUT_WAV}") -> pop_notification.ogg (Vorbis Q7, sin delay)..."
        ffmpeg -y -i "${INPUT_WAV}" \
            -c:a libvorbis -q:a 7 \
            -ar 44100 \
            "${TARGET_OGG}" >/dev/null 2>&1 || {
                echo "⚠️ ffmpeg con libvorbis falló, intentando conversión estándar..."
                ffmpeg -y -i "${INPUT_WAV}" "${TARGET_OGG}" >/dev/null 2>&1
            }
        echo "✅ Sonido generado exitosamente: ${TARGET_OGG}"
        ls -lh "${TARGET_OGG}"
    else
        echo "⚠️ No se encontró el archivo fuente WAV en ${SOURCE_DIR}. Verificando si ya existe OGG..."
    fi
else
    echo "⚠️ ffmpeg no está instalado en el entorno."
    if [ -f "${OUTPUT_DIR}/pop_notification.ogg" ]; then
        echo "ℹ️ El archivo pop_notification.ogg ya existe en el proyecto. Continuando..."
    else
        echo "❌ Advertencia: No existe pop_notification.ogg en ${OUTPUT_DIR} y ffmpeg no está disponible."
    fi
fi

echo "=========================================================="
echo "✨ Proceso de audio completado."
echo "=========================================================="
