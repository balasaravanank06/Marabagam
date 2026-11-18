import math
from typing import List, Dict, Tuple
from app.movement.schemas import LandmarkInput

# MediaPipe 33 Landmark Indices
LEFT_SHOULDER = 11
RIGHT_SHOULDER = 12
LEFT_ELBOW = 13
RIGHT_ELBOW = 14
LEFT_WRIST = 15
RIGHT_WRIST = 16
LEFT_HIP = 23
RIGHT_HIP = 24
LEFT_KNEE = 25
RIGHT_KNEE = 26
LEFT_ANKLE = 27
RIGHT_ANKLE = 28

def extract_landmark_point(landmarks: List[LandmarkInput], index: int) -> Tuple[float, float, float]:
    if index < 0 or index >= len(landmarks):
        return (0.0, 0.0, 0.0)
    lm = landmarks[index]
    return (lm.x, lm.y, lm.z)

def normalize_frame_landmarks(landmarks: List[LandmarkInput]) -> List[Tuple[float, float, float]]:
    if len(landmarks) < 33:
        # Pad or return original if incomplete
        return [(lm.x, lm.y, lm.z) for lm in landmarks]

    l_hip = extract_landmark_point(landmarks, LEFT_HIP)
    r_hip = extract_landmark_point(landmarks, RIGHT_HIP)
    l_sh = extract_landmark_point(landmarks, LEFT_SHOULDER)
    r_sh = extract_landmark_point(landmarks, RIGHT_SHOULDER)

    mid_hip = (
        (l_hip[0] + r_hip[0]) / 2.0,
        (l_hip[1] + r_hip[1]) / 2.0,
        (l_hip[2] + r_hip[2]) / 2.0
    )

    mid_sh = (
        (l_sh[0] + r_sh[0]) / 2.0,
        (l_sh[1] + r_sh[1]) / 2.0,
        (l_sh[2] + r_sh[2]) / 2.0
    )

    torso_dx = mid_sh[0] - mid_hip[0]
    torso_dy = mid_sh[1] - mid_hip[1]
    torso_dz = mid_sh[2] - mid_hip[2]
    torso_len = math.sqrt(torso_dx * torso_dx + torso_dy * torso_dy + torso_dz * torso_dz)

    # Zero-denominator safeguard
    scale = torso_len if torso_len > 1e-5 else 1.0

    normalized = []
    for lm in landmarks:
        nx = (lm.x - mid_hip[0]) / scale
        ny = (lm.y - mid_hip[1]) / scale
        nz = (lm.z - mid_hip[2]) / scale
        normalized.append((nx, ny, nz))

    return normalized

def calculate_stance_width_ratio(landmarks: List[LandmarkInput]) -> float:
    if len(landmarks) < 33:
        return 1.0

    l_ank = extract_landmark_point(landmarks, LEFT_ANKLE)
    r_ank = extract_landmark_point(landmarks, RIGHT_ANKLE)
    l_sh = extract_landmark_point(landmarks, LEFT_SHOULDER)
    r_sh = extract_landmark_point(landmarks, RIGHT_SHOULDER)

    ank_dist = math.sqrt((l_ank[0] - r_ank[0])**2 + (l_ank[1] - r_ank[1])**2)
    sh_dist = math.sqrt((l_sh[0] - r_sh[0])**2 + (l_sh[1] - r_sh[1])**2)

    # Zero-denominator safeguard
    if sh_dist < 1e-5:
        return 1.0

    return ank_dist / sh_dist
