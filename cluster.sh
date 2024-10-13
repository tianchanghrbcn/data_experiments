#!/bin/bash

# Set working directory and paths
WORK_DIR=$(pwd)
DATA_CLEANING_DIR="${WORK_DIR}/data_cleaning"

# Define the base directory for results
RESULTS_DIR="${WORK_DIR}/results/cluster_results"

# Check and create the results directory if it doesn't exist
if [[ ! -d "$RESULTS_DIR" ]]; then
    mkdir -p "$RESULTS_DIR"
fi

# Prompt user for the CSV file path (relative paths accepted)
read -p "Please enter the relative path to the CSV file to be cleaned and clustered: " csv_file_path

# Resolve the relative path to an absolute path and export as environment variable
csv_file_path=$(realpath "$csv_file_path")
export CSV_FILE_PATH="$csv_file_path"

# Extract the base filename without extension for the output file name
base_filename=$(basename "$csv_file_path" .csv)

# Check if the file exists
if [[ ! -f "$csv_file_path" ]]; then
    echo "Error: File '$csv_file_path' not found. Please check the path and try again."
    exit 1
fi

# Clustering algorithm selection
echo "Choose a clustering algorithm:"
echo "0: K-Means"
echo "1: Gaussian Mixture Model (GMM)"
echo "2: Hierarchical Clustering (HC)"
echo "3: Affinity Propagation (AP)"
echo "4: DBSCAN"
echo "5: OPTICS"

read -p "Enter clustering algorithm number (0 to 5): " clustering_num
case $clustering_num in
    0)
        clustering_script="${DATA_CLEANING_DIR}/unsupervised clustering/K-means/K-Means.py"
        alg_name="K-Means"
        ;;
    1)
        clustering_script="${DATA_CLEANING_DIR}/unsupervised clustering/GMM/GMM.py"
        alg_name="GMM"
        ;;
    2)
        clustering_script="${DATA_CLEANING_DIR}/unsupervised clustering/HC/HC.py"
        alg_name="HC"
        ;;
    3)
        clustering_script="${DATA_CLEANING_DIR}/unsupervised clustering/AP/AP.py"
        alg_name="AP"
        ;;
    4)
        clustering_script="${DATA_CLEANING_DIR}/unsupervised clustering/DBSCAN/DBSCAN.py"
        alg_name="DBSCAN"
        ;;
    5)
        clustering_script="${DATA_CLEANING_DIR}/unsupervised clustering/OPTICS/OPTICS.py"
        alg_name="OPTICS"
        ;;
    *)
        echo "Invalid selection. Please enter a number from 0 to 5."
        exit 1
        ;;
esac

# Define output file path for logs
output_file="${RESULTS_DIR}/${base_filename}_${alg_name}.txt"

# Confirm selections
echo "Selections complete:"
echo "Data file: $csv_file_path"
echo "Clustering algorithm: $alg_name"
echo "Output file: $output_file"

# Run clustering algorithm and save output to file
echo "Running clustering algorithm: $alg_name"
python3 "$clustering_script" "$csv_file_path" "${RESULTS_DIR}/clustering_results.csv" &> "$output_file"
if [[ $? -ne 0 ]]; then
    echo "Clustering failed. Check log file: $output_file"
    exit 1
fi
echo "Clustering complete. Results saved to $RESULTS_DIR. Log saved to $output_file."
