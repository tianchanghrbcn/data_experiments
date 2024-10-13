import os
import time
import pandas as pd
import numpy as np
from sklearn.cluster import AgglomerativeClustering
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.metrics import silhouette_score, davies_bouldin_score
from sklearn.decomposition import PCA
import matplotlib.pyplot as plt
import math

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

# Load dataset
df = pd.read_csv(csv_file_path)
print("Loading dataset...")

# Exclude columns containing 'id'
excluded_columns = [col for col in df.columns if 'id' in col.lower()]
print(f"Excluded columns containing 'id': {excluded_columns}")

# Choose a random target column from the remaining columns
remaining_columns = df.columns.difference(excluded_columns)
target_column = np.random.choice(remaining_columns)
print(f"Randomly selected target column: {target_column}")

# Separate target and feature columns
y = df[target_column]
X = df.drop(columns=[target_column])

# Encode the target column if it's categorical
if y.dtype == 'object' or y.dtype == 'category':
    le = LabelEncoder()
    y = le.fit_transform(y)
    print(f"Target column {target_column} has been encoded")

# Perform frequency encoding for categorical features
for col in X.columns:
    if X[col].dtype == 'object' or X[col].dtype == 'category':
        X[col] = X[col].map(X[col].value_counts(normalize=True))

# Drop rows with NaN values
X = X.dropna()

# Standardize the data
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# Set maximum number of clusters based on the square root of the row count
max_clusters = 2 * math.isqrt(X.shape[0])

# Define weighted score parameters alpha and beta
alpha = 0.7
beta = 0.3

# Penalty factor for number of clusters less than the minimum threshold
penalty_factor = 1.1
min_clusters_threshold = 10

# Define the weighted scoring function
def weighted_score(silhouette, davies_bouldin, n_clusters):
    silhouette = 1 + silhouette
    score = alpha * np.exp(silhouette) + beta * np.exp(-davies_bouldin)
    if n_clusters < min_clusters_threshold:
        score += penalty_factor * (min_clusters_threshold - n_clusters)
    return score

# Iterate through cluster counts from 3 to max_clusters
best_weighted_score = float('-inf')
best_silhouette_score = float('-inf')
best_db_score = float('inf')
best_n_clusters = None
best_labels = None

output_txt = []
output_txt.append(f"Starting to iterate over cluster counts, current time: {time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())}")

for n_clusters in range(3, max_clusters + 1):
    hc = AgglomerativeClustering(n_clusters=n_clusters, metric='cosine', linkage='complete')
    labels = hc.fit_predict(X_scaled)

    silhouette_avg = silhouette_score(X_scaled, labels, metric='cosine')
    db_score = davies_bouldin_score(X_scaled, labels)
    current_weighted_score = weighted_score(silhouette_avg, db_score, n_clusters)

    output_txt.append(f"Cluster count: {n_clusters}, Silhouette Score: {silhouette_avg}, Davies-Bouldin Score: {db_score}, Weighted Score: {current_weighted_score}")

    if silhouette_avg > best_silhouette_score and db_score < best_db_score:
        best_weighted_score = current_weighted_score
        best_silhouette_score = silhouette_avg
        best_db_score = db_score
        best_n_clusters = n_clusters
        best_labels = labels

output_txt.append(f"\nBest number of clusters: {best_n_clusters}")
output_txt.append(f"Best Weighted Score: {best_weighted_score}")
output_txt.append(f"Final Silhouette Score: {silhouette_score(X_scaled, best_labels, metric='cosine')}")
output_txt.append(f"Final Davies-Bouldin Score: {davies_bouldin_score(X_scaled, best_labels)}")

# Generate output paths for saving the text output and plot
base_filename = os.path.splitext(os.path.basename(csv_file_path))[0]
output_dir = os.path.join(os.getcwd(), "results", "cluster_results")
os.makedirs(output_dir, exist_ok=True)
output_txt_file = os.path.join(output_dir, f"{base_filename}_HC.txt")
output_img_file = os.path.join(output_dir, f"{base_filename}_HC.png")

# Save text output
with open(output_txt_file, 'w', encoding='utf-8') as f:
    f.write("\n".join(output_txt))

print(f"Text output saved as {output_txt_file}")

# Visualize data and save plot
pca = PCA(n_components=3)
X_pca = pca.fit_transform(X_scaled)
fig = plt.figure(figsize=(12, 8))
ax = fig.add_subplot(111, projection='3d')
sc = ax.scatter(X_pca[:, 0], X_pca[:, 1], X_pca[:, 2], c=best_labels, cmap='Set1', alpha=0.7)
plt.colorbar(sc, ax=ax, label='Cluster Label')
ax.set_title(f'Agglomerative Clustering with {best_n_clusters} Clusters (3D)')
ax.set_xlabel('PCA Component 1')
ax.set_ylabel('PCA Component 2')
ax.set_zlabel('PCA Component 3')
plt.savefig(output_img_file)
print(f"Plot saved as {output_img_file}")

end_time = time.time()
print(f"Program completed in: {end_time - start_time} seconds")
