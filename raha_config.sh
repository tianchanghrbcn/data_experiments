#!/bin/bash

# Set working directory
WORK_DIR=$(pwd)
RAHA_DIR="${WORK_DIR}/raha"
HOLOCLEAN_DIR="${WORK_DIR}/Holoclean"

# Update apt sources to Tsinghua University mirrors for faster access in China
echo "Updating apt sources to Tsinghua University mirrors..."
sudo cp /etc/apt/sources.list /etc/apt/sources.list.bak
sudo bash -c 'cat > /etc/apt/sources.list' << EOF
deb https://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy main restricted universe multiverse
deb https://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-updates main restricted universe multiverse
deb https://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-backports main restricted universe multiverse
deb https://mirrors.tuna.tsinghua.edu.cn/ubuntu/ jammy-security main restricted universe multiverse
EOF

# Install necessary system libraries
echo "Installing necessary libraries for raha"
sudo apt update
sudo apt install -y software-properties-common libatlas-base-dev libblas-dev liblapack-dev gfortran

# Add PPA for Python 3.7 and Python 3.9 and install them
sudo add-apt-repository -y ppa:deadsnakes/ppa
sudo apt update
sudo apt install -y python3.7 python3.7-venv python3.7-dev python3.9 python3.9-venv

# Create virtual environment for raha and install dependencies
echo "Creating virtual environment for raha (Python 3.9)..."
cd "$RAHA_DIR" || exit
python3.9 -m venv venv
source venv/bin/activate
pip install wheel -i https://pypi.tuna.tsinghua.edu.cn/simple
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
deactivate
echo "Raha environment setup complete."

# Set the PYTHONPATH environment variable for raha
export PYTHONPATH="${PYTHONPATH}:${RAHA_DIR}"

echo "RAHA's PYTHONPATH has been set to include ${RAHA_DIR}."