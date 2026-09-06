#!/usr/bin/env bash
set -euo pipefail

echo "=========================================================="
echo "🌐 Configurando Entorno Gráfico y Servidor Web noVNC"
echo "=========================================================="

export DEBIAN_FRONTEND=noninteractive

echo ">> [1/4] Instalando Xvfb, x11vnc, fluxbox y dependencias web..."
sudo apt-get update -qq
sudo apt-get install -y -qq xvfb x11vnc fluxbox net-tools python3 python3-pip python3-numpy

echo ">> [2/4] Configurando cliente Web noVNC y websockify..."
NOVNC_DIR="/opt/novnc"
if [ ! -d "${NOVNC_DIR}" ]; then
    sudo git clone --depth 1 https://github.com/novnc/noVNC.git "${NOVNC_DIR}"
    sudo git clone --depth 1 https://github.com/novnc/websockify.git "${NOVNC_DIR}/utils/websockify"
    sudo ln -sf "${NOVNC_DIR}/vnc.html" "${NOVNC_DIR}/index.html"
fi

echo ">> [3/4] Instalando cloudflared para túnel seguro a tu móvil..."
if ! command -v cloudflared >/dev/null 2>&1; then
    curl -fsSL https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb -o /tmp/cloudflared.deb
    sudo dpkg -i /tmp/cloudflared.deb || sudo apt-get install -f -y
    rm -f /tmp/cloudflared.deb
fi

echo ">> [4/4] Iniciando Display Virtual Xvfb (:99) y gestor de ventanas ligero..."
export DISPLAY=:99
if [ -n "${GITHUB_ENV:-}" ] && [ -f "${GITHUB_ENV}" ]; then
    echo "DISPLAY=:99" >> "${GITHUB_ENV}"
fi
Xvfb :99 -screen 0 800x1280x24 -retro &
sleep 2

fluxbox &
sleep 1

echo "✅ Entorno gráfico y servicios base listos en DISPLAY=:99."
