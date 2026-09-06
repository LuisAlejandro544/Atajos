#!/usr/bin/env bash
set -euo pipefail

echo "============================================="
echo "🐞 Configurando y descargando Lua Debug Library"
echo "============================================="

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
LUA_DIR="${PROJECT_ROOT}/app/src/main/cpp/lua"

# Asegurar que los fuentes de Lua 5.4.7 estén disponibles
if [ ! -f "${LUA_DIR}/lua.h" ]; then
    echo "Fuentes de Lua no encontrados, ejecutando setup_lua.sh primero..."
    bash "${PROJECT_ROOT}/scripts/lua/setup_lua.sh"
fi

# Verificar específicamente los archivos de la librería de debug de Lua 5.4.7
echo "Verificando módulos de Lua Debug Library..."
if [ -f "${LUA_DIR}/ldebug.c" ] && [ -f "${LUA_DIR}/ldblib.c" ] && [ -f "${LUA_DIR}/ldebug.h" ]; then
    echo "✅ Módulos nativos de Lua Debug Library encontrados:"
    echo "   - ${LUA_DIR}/ldebug.c (Motor interno de depuración y registro de stack)"
    echo "   - ${LUA_DIR}/ldblib.c (Librería estándar debug: traceback, getinfo, getlocal)"
    echo "   - ${LUA_DIR}/ldebug.h (Encabezados internos de depuración)"
else
    echo "❌ Error: Faltan archivos de Lua Debug Library en ${LUA_DIR}."
    exit 1
fi

# Verificar el handler nativo en native-lua.cpp
if grep -q "luaL_traceback" "${PROJECT_ROOT}/app/src/main/cpp/native-lua.cpp"; then
    echo "✅ Handler de depuración con traceback integrado en native-lua.cpp"
else
    echo "❌ Error: native-lua.cpp no contiene el handler de traceback."
    exit 1
fi

echo "✅ Lua Debug Library 5.4.7 verificada y lista para compilar con CMake."
