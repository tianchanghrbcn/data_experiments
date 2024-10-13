import os
import time
import math
import pandas as pd
import numpy as np
from sklearn.mixture import GaussianMixture
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.metrics import silhouette_score, davies_bouldin_score
from sklearn.decomposition import PCA
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
from joblib import Parallel, delayed

# Get the CSV file path from the environment variable
csv_file_path = os.getenv("CSV_FILE_PATH")
if not csv_file_path:
    print("Error: CSV file path is not provided. Set 'CSV_FILE_PATH' environment variable.")
    exit(1)

# Normalize the path for cross-platform compatibility
csv_file_path = os.path.normpath(csv_file_path)

# Read the CSV file
try:
    data = pd.read_csv(csv_file_path)
    print("Data loaded successfully.")
except FileNotFoundError:
    print(f"Error: File '{csv_file_path}' not found. Please check the path and try again.")
    exit(1)

# Start timing
start_time = time.time()

# Define weighted score alpha and beta
alpha = 0.7
beta = 0.3

# Load dataset
df = pd.read_csv(csv_file_path)
print("Columns in the dataset:", df.columns)

# Exclude columns containing 'id'
excluded_columns = [col for col in df.columns if 'id' in col.lower()]
print(f"Excluding columns with 'id': {excluded_columns}")

# Randomly select a target column
remaining_columns = df.columns.difference(excluded_columns)
target_column = np.random.choice(remaining_columns)
print(f"Randomly selected target column: {target_column}")

# Separate target and feature columns
y = df[target_column]
X = df.drop(columns=[target_column])

# Encode target column if necessary
if y.dtype == 'object' or y.dtype == 'category':
    le = LabelEncoder()
    y = le.fit_transform(y)
    print(f"Target column {target_column} has been encoded.")

# Frequency encoding for categorical features
for col in X.columns:
    if X[col].dtype == 'object' or X[col].dtype == 'category':
        X[col] = X[col].map(X[col].value_counts(normalize=True))

# Drop rows with NaN values
X = X.dropna()

# Standardize data
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# Set max clusters based on the square root of the number of rows
max_clusters = 2 * math.isqrt(X.shape[0])

# Define evaluation function for parallel processing
def evaluate_gmm(n_components, cov_type):
    gmm = GaussianMixture(n_components=n_components, covariance_type=cov_type, random_state=0)
    gmm.fit(X_scaled)
    labels = gmm.predict(X_scaled)

    silhouette_avg = silhouette_score(X_scaled, labels)
    db_score = davies_bouldin_score(X_scaled, labels)

    # Calculate combined score
    combined_score = alpha * np.exp(1 + silhouette_avg) + beta * np.exp(-db_score)
    print(f"Clusters: {n_components}, Covariance Type: {cov_type}, Silhouette Score: {silhouette_avg}, Davies-Bouldin Score: {db_score}, Combined Score: {combined_score}")

    return n_components, cov_type, combined_score, labels

# Parallel computation over different cluster counts and covariance types
results = Parallel(n_jobs=-1)(delayed(evaluate_gmm)(n, cov_type)
                              for n in range(2, max_clusters + 1)
                              for cov_type in ['full', 'tied', 'diag', 'spherical'])

# Find the best result
best_result = max(results, key=lambda x: x[2])
best_n_components, best_cov_type, best_combined_score, best_labels = best_result

# Output the best result
print(f"\nBest number of clusters: {best_n_components}")
print(f"Best covariance type: {best_cov_type}")
print(f"Highest Combined Score: {best_combined_score}")

# Reduce data to 3D for visualization
pca = PCA(n_components=3)
X_pca = pca.fit_transform(X_scaled)

# Set up paths for output files
base_filename = os.path.splitext(os.path.basename(csv_file_path))[0]
output_dir = os.path.join(os.getcwd(), "results", "cluster_results")
os.makedirs(output_dir, exist_ok=True)
output_txt_file = os.path.join(output_dir, f"{base_filename}_GMM.txt")
output_img_file = os.path.join(output_dir, f"{base_filename}_GMM.png")

# Create and save 3D scatter plot
fig = plt.figure(figsize=(16, 10), dpi=120)
ax = fig.add_subplot(111, projection='3d')
sc = ax.scatter(X_pca[:, 0], X_pca[:, 1], X_pca[:, 2], c=best_labels, cmap='Set1', alpha=0.7, s=50)
colorbar = plt.colorbar(sc, ax=ax, pad=0.1)
colorbar.set_label('Cluster Label')

ax.set_title(f'GMM Clustering with {best_n_components} Components ({best_cov_type} covariance) (3D)', fontsize=15)
ax.set_xlabel('PCA Component 1', fontsize=12)
ax.set_ylabel('PCA Component 2', fontsize=12)
ax.set_zlabel('PCA Component 3', fontsize=12)

plt.savefig(output_img_file)
print(f"Plot saved as {output_img_file}")

# Save text output
with open(output_txt_file, 'w', encoding='utf-8') as f:
    f.write(f"Best number of clusters: {best_n_components}\n")
    f.write(f"Best covariance type: {best_cov_type}\n")
    f.write(f"Highest Combined Score: {best_combined_score}\n")
    f.write(f"Final Silhouette Score: {silhouette_score(X_scaled, best_labels)}\n")
    f.write(f"Final Davies-Bouldin Score: {davies_bouldin_score(X_scaled, best_labels)}\n")

print(f"Text output saved as {output_txt_file}")

# Print total runtime
end_time = time.time()
print(f"Execution completed in {end_time - start_time} seconds")
