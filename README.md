# Data Experiments Repository

This repository contains resources and code related to data cleaning systems, clustering algorithms, and experimental results. It includes:

- **Data Cleaning Systems**: Research papers and source code for various data cleaning algorithms, including implementations for systems like Raha and MLNClean.
- **Clustering Algorithms**: Implementations of popular clustering algorithms such as K-Means, GMM, HC, DBSCAN and others, used to evaluate the performance of cleaned data.
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
- PostgreSQL (for MLNClean)
- Git LFS for large files

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/tianchanghrbcn/data_experiments.git
   cd data_experiments
   ```
2. Install required dependencies for each system (refer to each system's README or setup instructions).

### Running Experiments
To run experiments, use the provided shell scripts:
  ```bash
  sh run_experiment.sh
  ```
### Contributing
If you have suggestions or improvements, feel free to open an issue or submit a pull reques

### License
This project is licensed under the MIT License - see the LICENSE file for details.

# 数据实验资料库

该资料库包含与数据清理系统、聚类算法和实验结果相关的资源和代码。它包括：

- **数据清理系统**：各种数据清理算法的研究论文和源代码，包括Raha、HoloClean和MLNClean等系统的实现。
- **聚类算法**：用于评估清理数据性能的常用聚类算法（如K-Means、GMM、HC、DBSCAN 等）的实现。
- **实验结果**：对不同数据清理方法对聚类性能的影响进行的实验的详细结果。

## 仓库结构

- `data_cleaning/`：数据清理系统的源代码和配置。
- `mlnclean/`：MLNClean的代码库，包括设置文件和主脚本。
- `raha/`：Raha系统的源代码和补充文件。
- `results/`：包含实验结果，包括指标和分析文件。
- `cluster.sh`：运行聚类实验的脚本。
- `raha_config.sh`：Raha的配置脚本。
- `run_experiment.sh`：按顺序运行所有实验的脚本。

## 入门

要开始使用该项目，请克隆存储库并根据要执行的任务导航到相关目录。确保已安装必要的依赖项，这些依赖项可在每个数据清理系统的特定文件夹中找到。

### 必备条件

- Python 3.7 或更高版本
- Java 8（用于 MLNClean）
- PostgreSQL（用于 MLNClean）
- Git LFS（用于大文件）

### 安装

1. 克隆存储库：
```bash
git clone https://github.com/tianchanghrbcn/data_experiments.git
cd data_experiments
```
2. 为每个系统安装所需的依赖项（请参阅每个系统的自述文件或安装说明）。

### 运行实验
要运行实验，请使用提供的shell脚本：
```bash
sh run_experiment.sh
```
### 贡献
如果您有任何建议或改进，请随时提出问题或提交拉取请求

### 许可
该项目根据MIT许可进行许可——详情请参阅LICENSE文件。

