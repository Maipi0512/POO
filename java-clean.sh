#!/bin/bash
# Wrapper que limpia las variables GTK del snap de VSCode antes de lanzar Java.
# Usado por launch.json via "javaExec" para que F5 funcione.
exec env -i \
  HOME="$HOME" \
  USER="$USER" \
  PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin" \
  DISPLAY="${DISPLAY:-:0}" \
  XAUTHORITY="${XAUTHORITY:-$HOME/.Xauthority}" \
  /usr/lib/jvm/java-17-openjdk-amd64/bin/java "$@"
