import math
from typing import List, Tuple, Union

try:
    from fastdtw import fastdtw
    from scipy.spatial.distance import euclidean
    HAS_FASTDTW = True
except ImportError:
    HAS_FASTDTW = False

def pure_python_dtw(q: List[float], r: List[float]) -> float:
    """Fallback pure-Python DTW implementation."""
    n, m = len(q), len(r)
    if n == 0 or m == 0:
        return 0.0

    dtw_matrix = [[float('inf')] * (m + 1) for _ in range(n + 1)]
    dtw_matrix[0][0] = 0.0

    for i in range(1, n + 1):
        for j in range(1, m + 1):
            cost = abs(q[i - 1] - r[j - 1])
            dtw_matrix[i][j] = cost + min(
                dtw_matrix[i - 1][j],      # Insertion
                dtw_matrix[i][j - 1],      # Deletion
                dtw_matrix[i - 1][j - 1]   # Match
            )

    return dtw_matrix[n][m] / max(n, m)

def compute_dtw_distance(series1: List[float], series2: List[float]) -> float:
    if not series1 or not series2:
        return 0.0

    if HAS_FASTDTW:
        try:
            distance, _ = fastdtw(series1, series2, dist=euclidean)
            return distance / max(len(series1), len(series2))
        except Exception:
            return pure_python_dtw(series1, series2)
    else:
        return pure_python_dtw(series1, series2)
