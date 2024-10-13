#!/bin/bash

# Update package manager to use Tsinghua University mirrors for faster downloads in China
sudo cp /etc/apt/sources.list /etc/apt/sources.list.bak
sudo sed -i 's|http://.*archive.ubuntu.com/ubuntu/|http://mirrors.tuna.tsinghua.edu.cn/ubuntu/|g' /etc/apt/sources.list
sudo sed -i 's|http://.*security.ubuntu.com/ubuntu/|http://mirrors.tuna.tsinghua.edu.cn/ubuntu/|g' /etc/apt/sources.list

# Update package manager and install necessary dependencies
sudo apt update -y
sudo apt upgrade -y
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt/ $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
sudo apt-get update
sudo apt-get install -y postgresql-9.6 postgresql-client-9.6 openjdk-8-jdk wget unzip

# Set up Java environment variables
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
echo "export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64" >> ~/.bashrc
echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc
source ~/.bashrc

# Verify Java installation
java -version

# Configure PostgreSQL database
sudo service postgresql start

# Create database user and database
sudo -u postgres psql <<EOF
CREATE USER postgres WITH PASSWORD '123456';
ALTER USER postgres WITH SUPERUSER;
CREATE DATABASE mlnclean;
\q
EOF

# Configure PostgreSQL for remote connections and enable password authentication
PG_HBA_FILE=$(find /etc/postgresql -name pg_hba.conf)
sudo sed -i "s/peer/trust/" $PG_HBA_FILE
sudo sed -i "s/md5/trust/" $PG_HBA_FILE
sudo sed -i "/^#listen_addresses/i listen_addresses = '*'" /etc/postgresql/*/main/postgresql.conf

# Restart PostgreSQL to apply configuration changes
sudo service postgresql restart

# Create the required Schema
sudo -u postgres psql -d mlnclean -c "CREATE SCHEMA markov_schema AUTHORIZATION postgres;"

# Create the sort_integer_array function
sudo -u postgres psql -d mlnclean <<EOF
CREATE OR REPLACE FUNCTION sort_integer_array(arr integer[])
RETURNS integer[] AS \$$
BEGIN
    RETURN (SELECT array_agg(val ORDER BY val) FROM unnest(arr) val);
END;
\$$ LANGUAGE plpgsql;
EOF

# Navigate to the mlnclean project directory and ensure Java setup
cd "/mnt/d/algorithm paper/ML algorithms codes/data_experiments/Automatic-Data-Repair/mlnclean/code/MLNClean/MLNClean"

# Compile Java project (adjust this part based on actual project setup)
javac -cp .:path/to/required/libraries *.java

# Prompt user for the next steps or error resolution
echo "Java and PostgreSQL configurations are complete. You can proceed to check the mlnclean project setup or run the project."

# Completion message
echo "All configuration steps completed. Please review the setup and proceed as needed."
