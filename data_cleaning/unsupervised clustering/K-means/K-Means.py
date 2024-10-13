import os
import time
import math
import pandas as pd
import numpy as np
from sklearn.cluster import KMeans
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.metrics import silhouette_score, davies_bouldin_score
from sklearn.decomposition import PCA
import matplotlib.pyplot as plt

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

# Define the alpha and beta for weighted scoring
alpha = 0.7
beta = 0.3

# Load the dataset
df = pd.read_csv(csv_file_path)
print("Dataset columns:", df.columns)

# Exclude columns containing 'id'
excluded_columns = [col for col in df.columns if 'id' in col.lower()]
print(f"Excluded columns containing 'id': {excluded_columns}")

# Choose a random column as the target for classification
remaining_columns = df.columns.difference(excluded_columns)
target_column = np.random.choice(remaining_columns)
print(f"Randomly selected target column: {target_column}")

# Separate the target column from the feature columns
y = df[target_column]
X = df.drop(columns=[target_column])

# Encode target column if it's categorical
if y.dtype == 'object' or y.dtype == 'category':
    le = LabelEncoder()
    y = le.fit_transform(y)
    print(f"Target column {target_column} has been encoded")

# Frequency encode categorical features
for col in X.columns:
    if X[col].dtype == 'object' or X[col].dtype == 'category':
        X[col] = X[col].map(X[col].value_counts(normalize=True))

# Drop rows with NaN values
X = X.dropna()

# Standardize the data
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# Set the maximum number of clusters as the square root of the row count
max_clusters = math.isqrt(X.shape[0])

# Variables to store best parameters
best_combined_score = float('-inf')
best_n_clusters = None
best_labels = None

output_txt = []

# Start clustering with K-Means
output_txt.append(f"Starting K-Means clustering, current time: {time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())}")

for n_clusters in range(2, max_clusters + 1):
    kmeans = KMeans(n_clusters=n_clusters, init='k-means++', random_state=0)
    labels = kmeans.fit_predict(X_scaled)

    silhouette_avg = silhouette_score(X_scaled, labels)
    db_score = davies_bouldin_score(X_scaled, labels)

    # Calculate combined score
    combined_score = alpha * np.exp(1 + silhouette_avg) + beta * np.exp(-db_score)
    output_txt.append(f"Clusters: {n_clusters}, Silhouette Score: {silhouette_avg}, Davies-Bouldin Score: {db_score}, Combined Score: {combined_score}")

    # Update best parameters if current score is better
    if combined_score > best_combined_score:
        best_combined_score = combined_score
        best_n_clusters = n_clusters
        best_labels = labels

# Output the best result
output_txt.append(f"\nBest number of clusters: {best_n_clusters}")
output_txt.append(f"Highest Combined Score: {best_combined_score}")

# Generate paths for saving results
base_filename = os.path.splitext(os.path.basename(csv_file_path))[0]
output_dir = os.path.join(os.getcwd(), "results", "cluster_results")
os.makedirs(output_dir, exist_ok=True)
output_txt_file = os.path.join(output_dir, f"{base_filename}_KMeans.txt")
output_img_file = os.path.join(output_dir, f"{base_filename}_KMeans.png")

# Save text output
with open(output_txt_file, 'w', encoding='utf-8') as f:
    f.write("\n".join(output_txt))
print(f"Text output saved as {output_txt_file}")

# Data visualization
pca = PCA(n_components=3)
X_pca = pca.fit_transform(X_scaled)

fig = plt.figure(figsize=(16, 10), dpi=120)
ax = fig.add_subplot(111, projection='3d')
sc = ax.scatter(X_pca[:, 0], X_pca[:, 1], X_pca[:, 2], c=best_labels, cmap='Set1', alpha=0.7, s=50)
colorbar = plt.colorbar(sc, ax=ax, pad=0.1)
colorbar.set_label('Cluster Label')

ax.set_title(f'K-Means Clustering with {best_n_clusters} Clusters (3D)', fontsize=15)
ax.set_xlabel('PCA Component 1', fontsize=12)
ax.set_ylabel('PCA Component 2', fontsize=12)
ax.set_zlabel('PCA Component 3', fontsize=12)
plt.savefig(output_img_file)
print(f"Plot saved as {output_img_file}")

end_time = time.time()
print(f"Program completed in: {end_time - start_time} seconds")
