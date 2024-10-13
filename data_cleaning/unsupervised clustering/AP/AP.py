import os
import time
import pandas as pd
import numpy as np
from sklearn.cluster import AffinityPropagation
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

start_time = time.time()

# Initialize output storage
output_txt = []

# Load and preprocess data
df = pd.read_csv(csv_file_path)
output_txt.append("Data columns: " + str(df.columns))

# Exclude columns containing 'id'
excluded_columns = [col for col in df.columns if 'id' in col.lower()]
output_txt.append(f"Excluded columns with 'id': {excluded_columns}")

# Select random target column
remaining_columns = df.columns.difference(excluded_columns)
target_column = np.random.choice(remaining_columns)
output_txt.append(f"Randomly selected target column: {target_column}")

# Separate target column from features
y = df[target_column]
X = df.drop(columns=[target_column])

# Encode target column if necessary
if y.dtype == 'object' or y.dtype == 'category':
    le = LabelEncoder()
    y = le.fit_transform(y)
    output_txt.append(f"Encoded target column {target_column}")

# Frequency encode categorical features
for col in X.columns:
    if X[col].dtype == 'object' or X[col].dtype == 'category':
        X[col] = X[col].map(X[col].value_counts(normalize=True))

# Drop rows with NaN values
X = X.dropna()

# Standardize features
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# Parameter ranges for AffinityPropagation
damping_values = np.linspace(0.5, 0.9, 9)
preference_values = np.arange(-500, -100, 50)
best_combined_score = float('-inf')
best_labels = None
best_damping = None
best_preference = None

# Calculate combined score with weighted e-exponents
def calculate_combined_score(silhouette_avg, db_score):
    alpha, beta = 0.7, 0.3
    return alpha * np.exp(1 + silhouette_avg) + beta * np.exp(-db_score)

# Iterate over parameter combinations
output_txt.append(f"Starting AffinityPropagation clustering at {time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())}")
for damping in damping_values:
    for preference in preference_values:
        ap = AffinityPropagation(damping=damping, preference=preference, random_state=0)
        labels = ap.fit_predict(X_scaled)
        n_clusters = len(np.unique(labels))

        if n_clusters <= 1 or n_clusters >= len(X_scaled):
            output_txt.append(f"Clusters: {n_clusters}, damping: {damping}, preference: {preference} - Invalid, skipping")
            continue

        silhouette_avg = silhouette_score(X_scaled, labels)
        db_score = davies_bouldin_score(X_scaled, labels)
        combined_score = calculate_combined_score(silhouette_avg, db_score)

        output_txt.append(
            f"Clusters: {n_clusters}, damping: {damping}, preference: {preference}, "
            f"Silhouette: {silhouette_avg}, DB Score: {db_score}, Combined Score: {combined_score}")

        if combined_score > best_combined_score:
            best_combined_score = combined_score
            best_damping = damping
            best_preference = preference
            best_labels = labels
            best_n_clusters = n_clusters

# Generate output files
base_filename = os.path.splitext(os.path.basename(csv_file_path))[0]
output_dir = os.path.join(os.getcwd(), "results", "cluster_results")
os.makedirs(output_dir, exist_ok=True)
output_txt_file = os.path.join(output_dir, f"{base_filename}_AffinityPropagation.txt")
output_img_file = os.path.join(output_dir, f"{base_filename}_AffinityPropagation.png")

# Save results to text file
with open(output_txt_file, 'w', encoding='utf-8') as f:
    f.write("\n".join(output_txt))
print(f"Text output saved to {output_txt_file}")

# If best labels found, plot and save the image
if best_labels is not None:
    pca = PCA(n_components=3)
    X_pca = pca.fit_transform(X_scaled)

    fig = plt.figure(figsize=(12, 8))
    ax = fig.add_subplot(111, projection='3d')
    sc = ax.scatter(X_pca[:, 0], X_pca[:, 1], X_pca[:, 2], c=best_labels, cmap="Set1", alpha=0.7)
    plt.colorbar(sc, ax=ax, label='Cluster Label')
    ax.set_title(f'AffinityPropagation with {best_n_clusters} Clusters')
    ax.set_xlabel('PCA Component 1')
    ax.set_ylabel('PCA Component 2')
    ax.set_zlabel('PCA Component 3')
    plt.savefig(output_img_file)
    print(f"Plot saved as {output_img_file}")

end_time = time.time()
print(f"Program completed in: {end_time - start_time} seconds")
