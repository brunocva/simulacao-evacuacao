#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_DIR="/tmp/simulacao-evacuacao-build"
JADE_JAR="$ROOT_DIR/../simulacao-jade/lib/jade.jar"
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-30}"

cd "$ROOT_DIR"

echo "[validacao] Compilando projeto..."
javac -Xlint:unchecked -cp "$JADE_JAR" -d "$BUILD_DIR" $(find src -name '*.java' | sort)

cenarios=(
  "COM_BRIGADISTA"
  "SEM_BRIGADISTA"
  "ALTO_CONGESTIONAMENTO"
  "SAIDA_BLOQUEADA"
  "RISCO_CRESCENTE"
)

for cenario in "${cenarios[@]}"; do
  echo
  echo "[validacao] Executando $cenario SEM_GUI RAPIDO..."
  log_file="$(mktemp)"
  if ! timeout "$TIMEOUT_SECONDS" java -cp "$BUILD_DIR:$JADE_JAR" br.ufabc.sma.evacuacao.Main "$cenario" SEM_GUI RAPIDO >"$log_file" 2>&1; then
    cat "$log_file"
    echo "[validacao] Falha na execucao do cenario $cenario."
    exit 1
  fi

  cat "$log_file"
  if ! grep -q "EVACUACAO_CONCLUIDA" "$log_file"; then
    echo "[validacao] Cenario $cenario nao concluiu a evacuacao."
    exit 1
  fi
done

echo
echo "[validacao] Validacao concluida."
echo "[validacao] Verifique os arquivos em output/:"
echo "  - resultados-cenarios.csv"
echo "  - metricas-ciclos.csv"
echo "  - comparativo-cenarios.txt"
echo "  - estatisticas-cenarios.csv"
