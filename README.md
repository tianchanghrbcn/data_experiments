# Data Experiments Repository

This repository contains resources and code related to data cleaning systems, clustering algorithms, and experimental results. It includes:

- **Data Cleaning Systems**: Research papers and source code for various data cleaning algorithms, including implementations for systems like Raha, HoloClean, and MLNClean.
- **Clustering Algorithms**: Implementations of popular clustering algorithms such as K-Means, GMM, and others, used to evaluate the performance of cleaned data.
- **Experimental Results**: Detailed results from experiments conducted on the effectiveness of different data cleaning methods on clustering performance.

## Repository Structure

- `data_cleaning/`: Source code and configurations for data cleaning systems.
- `mlnclean/`: Codebase for MLNClean, including setup files and main scripts.
- `raha/`: Raha system source code and supplementary files.
- `results/`: Contains experimental results, including metrics and analysis files.
- `cluster.sh`: Script for running clustering experiments.
- `raha_config.sh`: Configuration script for Raha.
- `run_experiment.sh`: Script to run all experiments sequentially.

## Getting Started

To get started with this project, clone the repository and navigate to the relevant directories based on the task you want to perform. Ensure you have the necessary dependencies installed, which can be found in each data cleaning system's specific folder.

### Prerequisites

- Python 3.7 or above
- Java 8 for MLNClean
- PostgreSQL (for HoloClean)
- Git LFS for large files

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/tianchanghrbcn/data_experiments.git
   cd data_experiments
2. Install required dependencies for each system (refer to each system's README or setup instructions).

### Running Experiments
To run experiments, use the provided shell scripts:
  ```bash
  sh run_experiment.sh


If you have suggestions or improvements, feel free to open an issue or submit a pull request.

### License
This project is licensed under the MIT License - see the LICENSE file for details.

