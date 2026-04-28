#!/bin/bash
# run_tsp.sh
# Runs TSP experiments across all TMG files in a folder and saves results.
#
# Usage: ./run_tsp.sh <tmg_folder> <output_folder> [max_subset_size]
#
# Arguments:
#   tmg_folder      - path to folder containing .tmg files
#   output_folder   - path to folder where results will be saved
#   max_subset_size - max cities for Nearest Neighbor and 2-opt (default: 20)
#                     Held-Karp will be capped lower (default: 15) due to memory

# ── argument handling ────────────────────────────────────────────────────────

if [ "$#" -lt 2 ]; then
    echo "Usage: $0 <tmg_folder> <output_folder> [max_subset_size]"
    exit 1
fi

TMG_FOLDER="$1"
OUTPUT_FOLDER="$2"
MAX_SUBSET="${3:-20}"       # default 20 for NN and 2-opt
HK_MAX=15                   # Held-Karp cap -- memory blows up past ~20 cities

# ── setup ────────────────────────────────────────────────────────────────────

mkdir -p "$OUTPUT_FOLDER"
SUMMARY="$OUTPUT_FOLDER/summary.csv"

# write CSV header
echo "file,subset_size,nn_distance,nn_time_ms,twoopt_distance,twoopt_time_ms,heldkarp_distance,heldkarp_time_ms" > "$SUMMARY"

# check that the TMG folder exists
if [ ! -d "$TMG_FOLDER" ]; then
    echo "Error: TMG folder '$TMG_FOLDER' not found."
    exit 1
fi

# check that TSPExperiment.class exists (i.e. code has been compiled)
if [ ! -f "TSPExperiment.class" ]; then
    echo "TSPExperiment.class not found -- compiling..."
    javac HighwayGraph.java TSPSolver.java TSPExperiment.java
    if [ $? -ne 0 ]; then
        echo "Compilation failed. Exiting."
        exit 1
    fi
fi

# ── main loop ────────────────────────────────────────────────────────────────

COUNT=0
ERRORS=0

for TMG_FILE in "$TMG_FOLDER"/*.tmg; do

    # skip if no tmg files found
    [ -e "$TMG_FILE" ] || { echo "No .tmg files found in $TMG_FOLDER"; exit 1; }

    BASENAME=$(basename "$TMG_FILE" .tmg)
    FILE_OUTPUT="$OUTPUT_FOLDER/$BASENAME"
    mkdir -p "$FILE_OUTPUT"

    echo "──────────────────────────────────────────"
    echo "Processing: $BASENAME"

    # run the experiment, passing flags for algorithm selection and subset size
    # output goes to per-file result files
    java -Xmx512m TSPExperiment \
        "$TMG_FILE" \
        --max-subset "$MAX_SUBSET" \
        --hk-max "$HK_MAX" \
        --nn-out    "$FILE_OUTPUT/nn_route.pth" \
        --twoopt-out "$FILE_OUTPUT/twoopt_route.pth" \
        --hk-out    "$FILE_OUTPUT/heldkarp_route.pth" \
        --csv-out   "$FILE_OUTPUT/results.csv" \
        2> "$FILE_OUTPUT/error.log"

    EXIT_CODE=$?

    if [ $EXIT_CODE -ne 0 ]; then
        echo "  WARNING: $BASENAME exited with code $EXIT_CODE -- check $FILE_OUTPUT/error.log"
        ERRORS=$((ERRORS + 1))
    else
        echo "  Done. Results saved to $FILE_OUTPUT/"

        # append this file's CSV row to the summary
        if [ -f "$FILE_OUTPUT/results.csv" ]; then
            # skip the header line from the per-file CSV
            tail -n +2 "$FILE_OUTPUT/results.csv" >> "$SUMMARY"
        fi
    fi

    COUNT=$((COUNT + 1))

done

# ── summary ──────────────────────────────────────────────────────────────────

echo "══════════════════════════════════════════"
echo "Finished. Processed $COUNT file(s), $ERRORS error(s)."
echo "Summary CSV: $SUMMARY"
echo "Per-file results in: $OUTPUT_FOLDER/"
