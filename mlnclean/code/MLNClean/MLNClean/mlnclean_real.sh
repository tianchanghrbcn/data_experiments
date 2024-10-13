#!/bin/bash

# Define variables
TASK_LIST=("flights" "rayyan" "hospital" "beers")
CNTS_LIST=(1 2 3)
NUMS_LIST=('01')

# Check if the JAR file exists and delete it if so, then recompile with Maven
JAR_FILE="target/MLNClean-1.0-SNAPSHOT.jar"
if [ -f "$JAR_FILE" ]; then
    echo "Found $JAR_FILE, deleting and recompiling..."
    rm "$JAR_FILE"
    mvn clean package
else
    echo "$JAR_FILE not found, compiling project..."
    mvn clean package
fi

# Loop through configurations and run the Java program
for CNT in "${CNTS_LIST[@]}"
do
    for NUM in "${NUMS_LIST[@]}"
    do        
        for TASK in "${TASK_LIST[@]}"
        do                
            DIRTY_DATA="${TASK}/dataset/"
            TASK_NAME="${TASK}-inner_outer_error-${NUM}"
            DIRTY_DATA_PATH="${DIRTY_DATA}${TASK_NAME}"
            echo "Hello, world!"
            echo "${DIRTY_DATA_PATH}"
            java -cp target/MLNClean-1.0-SNAPSHOT.jar main.Test "${DIRTY_DATA_PATH}" "trainData.csv" "testData.csv" 1 0 || true
        done
    done
done

