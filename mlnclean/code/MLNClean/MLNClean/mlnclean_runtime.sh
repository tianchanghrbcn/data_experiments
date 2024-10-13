#!/bin/bash

# Define variables
TASK_LIST=("tax")
# TASK="hospital"
CNTS_LIST=(1 2 3)
NUMS_LIST=('10k' '20k' '30k' '40k' '50k')

for CNT in "${CNTS_LIST[@]}"
do
    # Loop through error rates and run holoclean_run.py on dataset
    for NUM in "${NUMS_LIST[@]}"
    do        
        for TASK in "${TASK_LIST[@]}"
        do                
            DIRTY_DATA="${TASK}/dataset/"
            TASK_NAME="${TASK}-dirty-original_error-00${NUM}"
            DIRTY_DATA_PATH="${DIRTY_DATA}${TASK_NAME}"
            echo "Hello, world!"
            echo "${DIRTY_DATA_PATH}"
            java -cp target/MLNClean-1.0-SNAPSHOT.jar main.Test "${DIRTY_DATA_PATH}" "trainData.csv" "testData.csv" 1 0 > ./mln-runtime-output.txt 2>&1 || true
        done
    done
done