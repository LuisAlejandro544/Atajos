#!/usr/bin/env bash
set -euo pipefail

echo "=========================================================="
echo "🌐 Iniciando Streaming noVNC y Túnel Seguro Cloudflare"
echo "=========================================================="

export DISPLAY=:99

echo ">> [1/3] Levantando servidor VNC (x11vnc) en puerto 5900..."
x11vnc -display :99 -forever -shared -nopw -rfbport 5900 -bg -o /tmp/x11vnc.log
sleep 1

echo ">> [2/3] Levantando servidor Web noVNC en puerto 8080..."
/opt/novnc/utils/novnc_proxy --vnc localhost:5900 --listen 8080 >/tmp/novnc.log 2>&1 &
sleep 2

echo ">> [3/3] Creando Túnel HTTPS seguro y público con Cloudflare Tunnel..."
rm -f /tmp/cf_tunnel.log
cloudflared tunnel --url http://127.0.0.1:8080 >/tmp/cf_tunnel.log 2>&1 &
CF_PID=$!

echo ">> Esperando generación de enlace web seguro..."
URL=""
for i in {1..30}; do
    if [ -f /tmp/cf_tunnel.log ]; then
        URL=$(grep -oE "https://[a-zA-Z0-9-]+\.trycloudflare\.com" /tmp/cf_tunnel.log | head -n 1 || true)
        if [ -n "${URL}" ]; then
            break
        fi
    fi
    sleep 1
done

if [ -n "${URL}" ]; then
    echo ""
    echo "========================================================================="
    echo "📱 ¡CONEXIÓN WEB AL EMULADOR ANDROID 10 (32 BITS) DISPONIBLE!"
    echo "========================================================================="
    echo "Abre este enlace directamente en el navegador de tu teléfono móvil:"
    echo ""
    echo "👉 ${URL}/vnc.html?autoconnect=true&resize=scale"
    echo ""
    echo "Podrás interactuar con el dedo directamente en la pantalla del emulador."
    echo "========================================================================="
else
    echo "⚠️ Advertencia: No se pudo extraer la URL del túnel en 30s. Revisa los logs:"
    cat /tmp/cf_tunnel.log 2>/dev/null || true
fi
