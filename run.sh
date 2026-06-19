#!/bin/bash
# Ejecuta la app con entorno limpio, sin las variables GTK del snap de VSCode
set -e
cd "$(dirname "$0")"

DISPLAY_VAL="${DISPLAY:-:0}"
XAUTH_VAL="${XAUTHORITY:-$HOME/.Xauthority}"

exec env -i \
  HOME="$HOME" \
  USER="$USER" \
  PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" \
  DISPLAY="$DISPLAY_VAL" \
  XAUTHORITY="$XAUTH_VAL" \
  JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64" \
  /usr/lib/jvm/java-17-openjdk-amd64/bin/java \
  -cp target/classes \
  farmared.Main
