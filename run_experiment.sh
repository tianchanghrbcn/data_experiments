#!/bin/bash

# Set working directory and paths
WORK_DIR=$(pwd)
RAHA_DIR="${WORK_DIR}/raha"
HOLOCLEAN_DIR="${WORK_DIR}/Holoclean"

# Dataset base path
DATA_CLEANING_DIR="${WORK_DIR}/data_cleaning"
BASE_PATH="${DATA_CLEANING_DIR}/Datasets"
RESULTS_DIR="${WORK_DIR}/results"

# Check and create RESULTS_DIR
if [[ ! -d "$RESULTS_DIR" ]]; then
    mkdir -p "$RESULTS_DIR"
fi

# Dataset selection
echo "Please choose a dataset:"
datasets=("toy" "beers" "flights" "hospital" "movies" "rayyan" "restaurants" "soccer" "tax")
for i in "${!datasets[@]}"; do
    echo "$i: ${datasets[$i]}"
done

read -p "Enter dataset number (0 to 8): " dataset_num
if [[ "$dataset_num" =~ ^[0-8]$ ]]; then
    dataset="${datasets[$dataset_num]}"
    dataset_folder="${BASE_PATH}/${dataset}"
    echo "Selected dataset folder: $dataset_folder"
else
    echo "Invalid selection. Please enter an integer from 0 to 8."
    exit 1
fi

# Check if dataset folder exists
if [[ ! -d "$dataset_folder" ]]; then
    echo "Error: Dataset folder does not exist."
    exit 1
fi

# List and select error rate file
error_rate_files=($(ls "$dataset_folder" | grep -E '^[0-9]+\.[0-9]+%.csv$' | sort -V))
error_rate_files=("clean.csv" "${error_rate_files[@]}")

echo "Choose an error rate file for ${dataset_folder}:"
for i in "${!error_rate_files[@]}"; do
    if [[ "$i" -eq 0 ]]; then
        echo "$i: clean"
    else
        rate=$(echo "${error_rate_files[$i]}" | sed 's/%\.csv//')
        echo "$i: $rate%"
    fi
done

read -p "Enter error rate number (0 to ${#error_rate_files[@]}): " error_rate_num
if [[ "$error_rate_num" =~ ^[0-9]+$ ]] && [[ "$error_rate_num" -ge 0 ]] && [[ "$error_rate_num" -lt ${#error_rate_files[@]} ]]; then
    selected_file="${error_rate_files[$error_rate_num]}"
    echo "Selected error rate file: $selected_file"
else
    echo "Invalid selection. Please enter a valid integer within the range."
    exit 1
fi

# Set the selected CSV file path
selected_file_path="${dataset_folder}/${selected_file}"
clean_file_path="${dataset_folder}/clean.csv"
echo "Loading data file: $selected_file_path"

# Select data cleaning algorithm
echo "Please choose a data cleaning algorithm:"
echo "0: raha-baran"
echo "1: Holoclean"
echo "2: mlnclean"

read -p "Enter algorithm number (0 to 2): " algorithm_num
if [[ "$algorithm_num" =~ ^[0-2]$ ]]; then
    case $algorithm_num in
        0)
            algorithm="raha-baran"
            algorithm_dir="$RAHA_DIR"
            python_env="${RAHA_DIR}/venv/bin/activate"
            ;;
        1)
            algorithm="Holoclean"
            algorithm_dir="$HOLOCLEAN_DIR"
            python_env="${HOLOCLEAN_DIR}/venv/bin/activate"
            ;;
        2)
            algorithm="mlnclean"
            algorithm_dir="$MLNCLEAN_DIR"
            python_env="${MLNCLEAN_DIR}/venv/bin/activate"
            ;;
    esac
    echo "Selected algorithm: $algorithm"
else
    echo "Invalid selection. Please enter an integer from 0 to 2."
    exit 1
fi

# Start timing
start_time=$(date +%s)

# Define function to print elapsed time every 15 seconds
print_time() {
    while true; do
        sleep 15
        current_time=$(date +%s)
        elapsed=$((current_time - start_time))
        echo "Elapsed time: $((elapsed / 60)) min $((elapsed % 60)) sec"
    done
}

# Start time printing process
print_time & time_printer_pid=$!

# Activate virtual environment and run detection
echo "Running error detection for algorithm: $algorithm"
source "$python_env"
export PYTHONPATH="${RAHA_DIR}"

# Capture error detection output
detection_output=$(python "${RAHA_DIR}/raha/detection.py" "$selected_file_path")
echo "$detection_output"
echo "Error detection complete."

# Run error correction with adjusted arguments
correction_output=$(python "${RAHA_DIR}/raha/correction.py" --dirty_path "$selected_file_path" --clean_path "$clean_file_path" --task_name "$dataset")
echo "$correction_output"
echo "Error correction complete."

# Stop the time printing process
kill $time_printer_pid 2>/dev/null

# Calculate and print total runtime
end_time=$(date +%s)
total_time=$((end_time - start_time))
echo "Total runtime: $((total_time / 60)) min $((total_time % 60)) sec"

# Rename 'flights' folder to 'raha-baran-results-${dataset}'
mv "${RESULTS_DIR}/${dataset}" "${RESULTS_DIR}/raha-baran-results-${dataset}"

echo "Folder renamed to raha-baran-results-${dataset}."
echo "Data error detection and correction complete."
