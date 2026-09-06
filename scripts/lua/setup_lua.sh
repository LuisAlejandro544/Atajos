#!/usr/bin/env bash
set -euo pipefail

echo "============================================="
echo "📥 Configurando fuentes oficiales de Lua 5.4.7"
echo "============================================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
LUA_DIR="${PROJECT_ROOT}/app/src/main/cpp/lua"
LUA_VERSION="5.4.7"
LUA_TAR="lua-${LUA_VERSION}.tar.gz"
LUA_URL="https://www.lua.org/ftp/${LUA_TAR}"

mkdir -p "${LUA_DIR}"

if [ -f "${LUA_DIR}/lua.h" ] && [ -f "${LUA_DIR}/lauxlib.h" ] && [ -f "${LUA_DIR}/lualib.h" ]; then
    echo "✅ Código fuente de Lua 5.4.7 ya disponible en: ${LUA_DIR}"
else
    echo "🌐 Descargando código oficial de Lua ${LUA_VERSION} desde ${LUA_URL}..."
    TMP_DIR=$(mktemp -d)
    
    if command -v curl >/dev/null 2>&1; then
        curl -fsSL "${LUA_URL}" -o "${TMP_DIR}/${LUA_TAR}"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "${LUA_URL}" -O "${TMP_DIR}/${LUA_TAR}"
    else
        echo "❌ Se requiere curl o wget para descargar Lua."
        exit 1
    fi

    echo "📦 Descomprimiendo archivos fuentes..."
    tar -xzf "${TMP_DIR}/${LUA_TAR}" -C "${TMP_DIR}"
    
    # Copiar archivos fuente y encabezados de la biblioteca core
    cp -r "${TMP_DIR}/lua-${LUA_VERSION}/src/"*.[ch] "${LUA_DIR}/"
    
    # Eliminar ejecutables autónomos (main CLI) para evitar duplicados en la librería compartida JNI
    rm -f "${LUA_DIR}/lua.c" "${LUA_DIR}/luac.c"
    
    rm -rf "${TMP_DIR}"
    echo "✅ Lua ${LUA_VERSION} descargado e instalado correctamente en ${LUA_DIR}."
fi

# Validación final de integridad
if [ ! -f "${LUA_DIR}/lua.h" ]; then
    echo "❌ Error: lua.h no se encuentra en ${LUA_DIR}."
    exit 1
fi
echo "🎉 Motor de Lua 5.4.7 puro listo para compilar con CMake."
