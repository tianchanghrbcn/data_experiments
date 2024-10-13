import time
import pandas as pd
import numpy as np
from sklearn.cluster import Birch
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.metrics import silhouette_score, davies_bouldin_score
from sklearn.decomposition import PCA
import matplotlib.pyplot as plt
import seaborn as sns
import math

# 提供你的 CSV 文件的绝对路径
csv_file_path = r"D:\algorithm paper\data_cleaning\Datasets\flights\clean.csv"

start_time = time.time()

# 使用 pandas 读取 CSV 文件
print("加载数据集...")
df = pd.read_csv(csv_file_path)

# 打印数据集的列名
print("数据集的列名:", df.columns)

# 排除列名中包含 'id' 的列（例如 'tuple_id', 'index_id' 等）
excluded_columns = [col for col in df.columns if 'id' in col.lower()]
print(f"排除包含 'id' 的列: {excluded_columns}")

# 选择不包含 'id' 的列作为目标列
remaining_columns = df.columns.difference(excluded_columns)

# 随机选择一列作为分类任务的目标列（多类目标）
target_column = np.random.choice(remaining_columns)
print(f"随机选择的目标列是: {target_column}")

# 将目标列与特征列分开
y = df[target_column]  # 目标列
X = df.drop(columns=[target_column])  # 剩余的列作为特征列

# 如果目标列是类别型数据，进行编码
if y.dtype == 'object' or y.dtype == 'category':
    le = LabelEncoder()
    y = le.fit_transform(y)
    print(f"目标列 {target_column} 已进行编码处理")

# 对类别型数据进行频率编码（仅适用于特征列中的类别型数据）
for col in X.columns:
    if X[col].dtype == 'object' or X[col].dtype == 'category':
        X[col] = X[col].map(X[col].value_counts(normalize=True))

# 删除包含 NaN 的行
X = X.dropna()

# 标准化数据
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# 设置簇数量范围为2到数据样本数平方根的2倍
num_samples = X.shape[0]
max_clusters = min(math.isqrt(num_samples), num_samples)

# 定义加权评分的 alpha 和 beta
alpha = 0.7
beta = 0.3

# 定义簇数量的惩罚因子，针对簇数量小于某阈值时应用惩罚
penalty_factor = 1.1
min_clusters_threshold = 10  # 小于10个簇时增加惩罚

# 定义加权评分函数，处理 Silhouette Score 统一使用 1 + Silhouette，并加入簇数量惩罚
def weighted_score(silhouette, davies_bouldin, n_clusters):
    silhouette = 1 + silhouette  # 无论正负都使用 1 + silhouette_avg
    score = alpha * np.exp(silhouette) + beta * np.exp(-davies_bouldin)

    # 对簇数量小于某个阈值的情况增加惩罚
    if n_clusters < min_clusters_threshold:
        score += penalty_factor * (min_clusters_threshold - n_clusters)

    return score

# 遍历簇数量
best_weighted_score = float('-inf')
best_n_clusters = None
best_labels = None

# 存储所有簇数量下的结果
results = []

print(f"开始遍历簇数量，当前时间: {time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())}")

# 精细调整 BIRCH 的 threshold 和 branching_factor 参数
# 精细调整 BIRCH 的 threshold 和 branching_factor 参数
threshold_values = np.linspace(0.01, 0.5, 10)  # 将阈值范围调整到更小的区间
branching_factor_values = np.arange(50, 301, 50)  # 增加分支因子范围到300

for threshold in threshold_values:
    for branching_factor in branching_factor_values:
        for n_clusters in range(2, max_clusters + 1):
            print(f"正在处理簇数量: {n_clusters} (threshold={threshold}, branching_factor={branching_factor})...")

            # 使用 BIRCH 进行聚类
            birch_model = Birch(n_clusters=n_clusters, threshold=threshold, branching_factor=branching_factor)
            labels = birch_model.fit_predict(X_scaled)

            # 计算评估指标
            silhouette_avg = silhouette_score(X_scaled, labels)
            db_score = davies_bouldin_score(X_scaled, labels)

            # 计算加权评分并加入惩罚
            current_weighted_score = weighted_score(silhouette_avg, db_score, n_clusters)

            # 存储每次结果
            results.append({
                'n_clusters': n_clusters,
                'silhouette_score': silhouette_avg,
                'davies_bouldin_score': db_score,
                'weighted_score': current_weighted_score,
                'threshold': threshold,
                'branching_factor': branching_factor
            })

            print(
                f"簇数量: {n_clusters}, Silhouette Score: {silhouette_avg}, Davies-Bouldin Score: {db_score}, Weighted Score: {current_weighted_score}")

            # 判断是否为最优簇数
            if current_weighted_score > best_weighted_score:
                best_weighted_score = current_weighted_score
                best_n_clusters = n_clusters
                best_labels = labels
                best_threshold = threshold
                best_branching_factor = branching_factor

# 输出所有结果
print("\n所有簇数量的结果:")
for res in results:
    print(f"簇数量: {res['n_clusters']}, Silhouette Score: {res['silhouette_score']}, "
          f"Davies-Bouldin Score: {res['davies_bouldin_score']}, Weighted Score: {res['weighted_score']}, "
          f"Threshold: {res['threshold']}, Branching Factor: {res['branching_factor']}")

# 输出最优结果
print(f"\n最佳簇数量: {best_n_clusters} (threshold={best_threshold}, branching_factor={best_branching_factor})")
print(f"最优的 Weighted Score: {best_weighted_score}")

# 计算并输出最终的评估指标
final_silhouette_avg = silhouette_score(X_scaled, best_labels)
final_db_score = davies_bouldin_score(X_scaled, best_labels)

print(
    f"最终 Silhouette Score: {final_silhouette_avg}, 当前时间: {time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())}")
print(f"最终 Davies-Bouldin Score: {final_db_score}, 当前时间: {time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())}")

# 数据可视化部分
print("数据可视化...")

# 使用 PCA 将数据降维到 2D
pca = PCA(n_components=2)
X_pca = pca.fit_transform(X_scaled)

# 创建一个散点图来展示聚类结果
plt.figure(figsize=(10, 7))
sns.scatterplot(x=X_pca[:, 0], y=X_pca[:, 1], hue=best_labels, palette="Set1", legend="full", alpha=0.7)
plt.title(f'BIRCH Clustering with {best_n_clusters} Clusters (threshold={best_threshold}, branching_factor={best_branching_factor})')
plt.xlabel('PCA Component 1')
plt.ylabel('PCA Component 2')
plt.show()

end_time = time.time()
print(f"程序执行结束，总耗时: {end_time - start_time} 秒")
