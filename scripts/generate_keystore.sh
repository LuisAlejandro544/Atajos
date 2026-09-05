#!/usr/bin/env bash
set -euo pipefail

echo "============================================="
echo "🔑 Configurando Android Debug Keystore"
echo "============================================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KEYSTORE_PATH="${PROJECT_ROOT}/debug.keystore"
BASE64_PATH="${PROJECT_ROOT}/debug.keystore.base64"

if [ -f "${KEYSTORE_PATH}" ]; then
    echo "✅ Keystore ya existente en: ${KEYSTORE_PATH}"
elif [ -f "${BASE64_PATH}" ]; then
    echo "📦 Decodificando keystore desde base64..."
    base64 -d "${BASE64_PATH}" > "${KEYSTORE_PATH}" 2>/dev/null || base64 --decode "${BASE64_PATH}" > "${KEYSTORE_PATH}"
    echo "✅ Keystore restaurado con éxito desde base64."
else
    echo "⚡ Generando nuevo debug.keystore con keytool..."
    keytool -genkeypair \
        -v \
        -keystore "${KEYSTORE_PATH}" \
        -storepass android \
        -alias androiddebugkey \
        -keypass android \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -dname "CN=Android Debug,O=Android,C=US"
    echo "✅ Keystore generado exitosamente en: ${KEYSTORE_PATH}"
fi

chmod 644 "${KEYSTORE_PATH}"
echo "🔒 Permisos asignados a debug.keystore."
