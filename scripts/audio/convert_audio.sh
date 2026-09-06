#!/usr/bin/env bash
set -euo pipefail

echo "=========================================================="
echo "🎵 Conversor de Audio a OGG (Cero Latencia / Sin Delay)"
echo "=========================================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

SOURCE_DIR="${PROJECT_ROOT}/audio_assets/raw"
OUTPUT_DIR="${PROJECT_ROOT}/app/src/main/res/raw"

mkdir -p "${SOURCE_DIR}"
mkdir -p "${OUTPUT_DIR}"

if command -v ffmpeg >/dev/null 2>&1; then
    echo ">> [1/2] ffmpeg detectado en el sistema."
    
    # Procesar cualquier archivo de audio compatible en audio_assets/raw
    CONVERTED_COUNT=0
    
    shopt -s nullglob
    AUDIO_FILES=("${SOURCE_DIR}"/*.wav "${SOURCE_DIR}"/*.mp3 "${SOURCE_DIR}"/*.m4a "${SOURCE_DIR}"/*.flac "${SOURCE_DIR}"/*.aac "${SOURCE_DIR}"/*.opus)
    shopt -u nullglob

    if [ ${#AUDIO_FILES[@]} -eq 0 ]; then
        echo "⚠️ No se encontraron archivos de audio en ${SOURCE_DIR}."
    else
        echo ">> [2/2] Procesando ${#AUDIO_FILES[@]} archivo(s) de audio..."
        for audio_file in "${AUDIO_FILES[@]}"; do
            filename=$(basename -- "$audio_file")
            # Normalizar nombre a minúsculas y caracteres seguros para recursos Android (a-z, 0-9, _)
            clean_name=$(echo "${filename%.*}" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '_' | sed 's/^_//;s/_$//')
            
            # Si el archivo es o contiene "pop", aseguramos la generación de pop_notification.ogg
            if [[ "${clean_name}" == *"pop"* ]] || [ ${#AUDIO_FILES[@]} -eq 1 ]; then
                target_ogg="${OUTPUT_DIR}/pop_notification.ogg"
            else
                target_ogg="${OUTPUT_DIR}/${clean_name}.ogg"
            fi

            echo "   -> Convirtiendo: ${filename} -> $(basename "${target_ogg}") (Vorbis Q7, sin delay)..."
            ffmpeg -y -i "${audio_file}" \
                -c:a libvorbis -q:a 7 \
                -ar 44100 \
                "${target_ogg}" >/dev/null 2>&1 || {
                    echo "   ⚠️ Falló libvorbis, intentando codificación automática OGG..."
                    ffmpeg -y -i "${audio_file}" "${target_ogg}" >/dev/null 2>&1
                }
            
            echo "   ✅ Listo: $(basename "${target_ogg}") ($(du -h "${target_ogg}" | cut -f1))"
            CONVERTED_COUNT=$((CONVERTED_COUNT + 1))
        done
        echo ">> Total convertidos: ${CONVERTED_COUNT} archivo(s) OGG en ${OUTPUT_DIR}"
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
echo "✨ Proceso de conversión de audio completado."
echo "=========================================================="
