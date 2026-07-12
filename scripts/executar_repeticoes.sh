#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_DIR="/tmp/simulacao-evacuacao-build"
JADE_JAR="$ROOT_DIR/../simulacao-jade/lib/jade.jar"
REPETICOES="${REPETICOES:-5}"
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-30}"
SEED_BASE="${SEED_BASE:-$(date +%s)}"

cd "$ROOT_DIR"

echo "[repeticoes] Compilando projeto..."
javac -Xlint:unchecked -cp "$JADE_JAR" -d "$BUILD_DIR" $(find src -name '*.java' | sort)
echo "[repeticoes] Baseline: SEM_BRIGADISTA"
echo "[repeticoes] Repeticoes por cenario: $REPETICOES"
echo "[repeticoes] Seed base experimental: $SEED_BASE"

cenarios=(
  "SEM_BRIGADISTA"
  "COM_BRIGADISTA"
  "ALTO_CONGESTIONAMENTO"
  "SAIDA_BLOQUEADA"
  "RISCO_CRESCENTE"
)

cenario_indice=0
for cenario in "${cenarios[@]}"; do
  cenario_indice=$((cenario_indice + 1))
  for repeticao in $(seq 1 "$REPETICOES"); do
    execucao_id="${cenario}-seedbase-${SEED_BASE}-rep-${repeticao}"
    seed=$((SEED_BASE + cenario_indice * 1000 + repeticao))

    echo
    echo "[repeticoes] Executando $cenario ($repeticao/$REPETICOES) com seed=$seed..."
    timeout "$TIMEOUT_SECONDS" java \
      -Devacuacao.execucaoId="$execucao_id" \
      -Devacuacao.seed="$seed" \
      -cp "$BUILD_DIR:$JADE_JAR" \
      br.ufabc.sma.evacuacao.Main "$cenario" SEM_GUI RAPIDO
  done
done

echo
echo "[repeticoes] Execucoes concluídas."
echo "[repeticoes] Arquivos em output/:"
echo "  - relatorio-*.txt"
echo "  - resultados-cenarios.csv"
echo "  - metricas-ciclos.csv"
echo "  - comparativo-cenarios.txt"
echo "  - comparativo-apresentacao.txt"
echo "  - comparativo-apresentacao.md"
echo "  - estatisticas-cenarios.csv"
