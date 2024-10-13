import os
import time
import math
import pandas as pd
import numpy as np
from sklearn.cluster import OPTICS
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.metrics import silhouette_score, davies_bouldin_score
from sklearn.metrics.pairwise import cosine_distances
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

start_time = time.time()

# Define the alpha and beta for weighted scoring
alpha = 0.7
beta = 0.3

# Load the dataset
df = pd.read_csv(csv_file_path)
output_txt = []
output_txt.append("Dataset columns: " + str(df.columns))

# Exclude columns containing 'id'
excluded_columns = [col for col in df.columns if 'id' in col.lower()]
output_txt.append(f"Excluded columns containing 'id': {excluded_columns}")

# Choose a random column as the target for classification
remaining_columns = df.columns.difference(excluded_columns)
target_column = np.random.choice(remaining_columns)
output_txt.append(f"Randomly selected target column: {target_column}")

# Separate the target column from the feature columns
y = df[target_column]
X = df.drop(columns=[target_column])

# Encode target column if it's categorical
if y.dtype == 'object' or y.dtype == 'category':
    le = LabelEncoder()
    y = le.fit_transform(y)
    output_txt.append(f"Target column {target_column} has been encoded")

# Frequency encode categorical features
for col in X.columns:
    if X[col].dtype == 'object' or X[col].dtype == 'category':
        X[col] = X[col].map(X[col].value_counts(normalize=True))

# Drop rows with NaN values
X = X.dropna()

# Standardize the data
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# Calculate cosine distances
X_cosine = cosine_distances(X_scaled)

# Parameter ranges
min_samples_range = [5, 10, 20, 30]
xi_range = [0.01, 0.05, 0.1]
min_cluster_size_range = [0.01, 0.02, 0.05]

# Define thresholds and penalty factor
min_clusters_threshold = 10
penalty_factor = 1.1

best_combined_score = float('-inf')
best_min_samples = None
best_xi = None
best_min_cluster_size = None
best_labels = None

output_txt.append(f"Starting OPTICS clustering, current time: {time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())}")

# Iterate over parameter combinations
for min_samples in min_samples_range:
    for xi in xi_range:
        for min_cluster_size in min_cluster_size_range:
            optics = OPTICS(min_samples=min_samples, xi=xi, min_cluster_size=min_cluster_size, metric='precomputed')
            optics.fit(X_cosine)
            labels = optics.labels_
            n_clusters = len(np.unique(labels)) - (1 if -1 in labels else 0)

            if n_clusters > 1:
                silhouette_avg = silhouette_score(X_cosine, labels, metric='precomputed')
                db_score = davies_bouldin_score(X_scaled, labels)
                combined_score = alpha * np.exp(1 + silhouette_avg) + beta * np.exp(-db_score)

                # Apply penalty if the number of clusters is below the threshold
                if n_clusters < min_clusters_threshold:
                    combined_score -= penalty_factor * (min_clusters_threshold - n_clusters)

                output_txt.append(
                    f"Clusters: {n_clusters}, min_samples: {min_samples}, xi: {xi}, min_cluster_size: {min_cluster_size}, "
                    f"Silhouette Score: {silhouette_avg}, Davies-Bouldin Score: {db_score}, Combined Score: {combined_score}")

                # Update the best results
                if combined_score > best_combined_score:
                    best_combined_score = combined_score
                    best_min_samples = min_samples
                    best_xi = xi
                    best_min_cluster_size = min_cluster_size
                    best_labels = labels

# Output the best results
if best_min_samples and best_xi and best_min_cluster_size:
    output_txt.append(f"\nBest parameters: min_samples={best_min_samples}, xi={best_xi}, min_cluster_size={best_min_cluster_size}")
    output_txt.append(f"Highest Combined Score: {best_combined_score}")
    n_clusters_final = len(np.unique(best_labels)) - (1 if -1 in best_labels else 0)
    output_txt.append(f"Final number of clusters: {n_clusters_final}")

    # Generate paths for saving results
    base_filename = os.path.splitext(os.path.basename(csv_file_path))[0]
    output_dir = os.path.join(os.getcwd(), "results", "cluster_results")
    os.makedirs(output_dir, exist_ok=True)
    output_txt_file = os.path.join(output_dir, f"{base_filename}_OPTICS.txt")
    output_img_file = os.path.join(output_dir, f"{base_filename}_OPTICS.png")

    # Save text output
    with open(output_txt_file, 'w', encoding='utf-8') as f:
        f.write("\n".join(output_txt))
    print(f"Text output saved as {output_txt_file}")

    # Data visualization: 3D scatter plot
    pca = PCA(n_components=3)
    X_pca = pca.fit_transform(X_scaled)

    fig = plt.figure(figsize=(12, 8))
    ax = fig.add_subplot(111, projection='3d')
    sc = ax.scatter(X_pca[:, 0], X_pca[:, 1], X_pca[:, 2], c=best_labels, cmap='Set1', alpha=0.7)
    plt.colorbar(sc, ax=ax, label='Cluster Label')
    ax.set_title(f'OPTICS Clustering with {n_clusters_final} Clusters')
    ax.set_xlabel('PCA Component 1')
    ax.set_ylabel('PCA Component 2')
    ax.set_zlabel('PCA Component 3')
    plt.savefig(output_img_file)
    print(f"Plot saved as {output_img_file}")

end_time = time.time()
print(f"Program completed in: {end_time - start_time} seconds")
