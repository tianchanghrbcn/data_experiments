#!/bin/bash

# Set working directory and paths
WORK_DIR=$(pwd)
DATA_CLEANING_DIR="${WORK_DIR}/data_cleaning"

# Define base results directory
RESULTS_DIR="${WORK_DIR}/results/cluster_results"

# Prompt user for the dataset name
read -p "Enter dataset name (beers, flights, hospital, movies, rayyan, restaurants, soccer, tax): " dataset_name

# Define the source directory based on dataset
SOURCE_DIR="${WORK_DIR}/results/raha-baran-results-${dataset_name}"

# Check if the source directory exists
if [[ ! -d "$SOURCE_DIR" ]]; then
    echo "Error: Dataset directory '$SOURCE_DIR' not found. Please check the dataset name and try again."
    exit 1
fi

# Check and create the results directory if it doesn't exist
if [[ ! -d "$RESULTS_DIR" ]]; then
    mkdir -p "$RESULTS_DIR"
fi

# Define clustering algorithms and paths
declare -A clustering_algorithms
clustering_algorithms=(
    ["K-Means"]="${DATA_CLEANING_DIR}/unsupervised clustering/K-means/K-Means.py"
    ["GMM"]="${DATA_CLEANING_DIR}/unsupervised clustering/GMM/GMM.py"
    ["HC"]="${DATA_CLEANING_DIR}/unsupervised clustering/HC/HC.py"
    ["AP"]="${DATA_CLEANING_DIR}/unsupervised clustering/AP/AP.py"
    ["DBSCAN"]="${DATA_CLEANING_DIR}/unsupervised clustering/DBSCAN/DBSCAN.py"
    ["OPTICS"]="${DATA_CLEANING_DIR}/unsupervised clustering/OPTICS/OPTICS.py"
)

# List all files in the dataset directory and process them
for csv_file_path in "$SOURCE_DIR"/*.csv; do
    # Check if there are CSV files in the directory
    if [[ ! -f "$csv_file_path" ]]; then
        echo "No CSV files found in directory '$SOURCE_DIR'. Exiting."
        exit 1
    fi
    
    # Get the base filename for the current CSV file
    base_filename=$(basename "$csv_file_path" .csv)
    
    # Export CSV file path as environment variable for the clustering scripts
    export CSV_FILE_PATH="$csv_file_path"
    
    # Run each clustering algorithm on the current CSV file
    for alg_name in "${!clustering_algorithms[@]}"; do
        clustering_script="${clustering_algorithms[$alg_name]}"
        
        # Define output file path for logs
        output_file="${RESULTS_DIR}/${base_filename}_${alg_name}.txt"
        
        # Confirm selections
        echo "Processing file: $csv_file_path"
        echo "Using clustering algorithm: $alg_name"
        
        # Run clustering algorithm and save output to file
        echo "Running clustering algorithm: $alg_name"
        python3 "$clustering_script" "$CSV_FILE_PATH" "${RESULTS_DIR}/${base_filename}_${alg_name}_results.csv" &> "$output_file"
        
        if [[ $? -ne 0 ]]; then
            echo "Clustering failed for $alg_name on file $csv_file_path. Check log file: $output_file"
        else
            echo "Clustering complete for $alg_name. Results saved to ${RESULTS_DIR}/${base_filename}_${alg_name}_results.csv. Log saved to $output_file."
        fi
    done
done
