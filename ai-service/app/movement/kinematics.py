import math
from typing import List, Tuple
from app.movement.schemas import LandmarkInput
from app.movement.normalization import (
    extract_landmark_point,
    LEFT_HIP, RIGHT_HIP, LEFT_KNEE, RIGHT_KNEE, LEFT_ANKLE, RIGHT_ANKLE,
    LEFT_SHOULDER, RIGHT_SHOULDER, LEFT_ELBOW, RIGHT_ELBOW, LEFT_WRIST, RIGHT_WRIST
)

def calculate_angle_3d(a: Tuple[float, float, float], b: Tuple[float, float, float], c: Tuple[float, float, float]) -> float:
    """Calculates angle ABC in degrees (vertex at B)."""
    ba = (a[0] - b[0], a[1] - b[1], a[2] - b[2])
    bc = (c[0] - b[0], c[1] - b[1], c[2] - b[2])

    dot_product = ba[0] * bc[0] + ba[1] * bc[1] + ba[2] * bc[2]
    mag_ba = math.sqrt(ba[0]**2 + ba[1]**2 + ba[2]**2)
    mag_bc = math.sqrt(bc[0]**2 + bc[1]**2 + bc[2]**2)

    if mag_ba < 1e-6 or mag_bc < 1e-6:
        return 180.0

    cosine = dot_product / (mag_ba * mag_bc)
    cosine = max(-1.0, min(1.0, cosine))
    return math.degrees(math.acos(cosine))

def calculate_knee_angle(landmarks: List[LandmarkInput], is_left: bool = True) -> float:
    if len(landmarks) < 33:
        return 180.0
    hip_idx = LEFT_HIP if is_left else RIGHT_HIP
    knee_idx = LEFT_KNEE if is_left else RIGHT_KNEE
    ank_idx = LEFT_ANKLE if is_left else RIGHT_ANKLE

    hip = extract_landmark_point(landmarks, hip_idx)
    knee = extract_landmark_point(landmarks, knee_idx)
    ank = extract_landmark_point(landmarks, ank_idx)

    return calculate_angle_3d(hip, knee, ank)

def calculate_elbow_angle(landmarks: List[LandmarkInput], is_left: bool = True) -> float:
    if len(landmarks) < 33:
        return 180.0
    sh_idx = LEFT_SHOULDER if is_left else RIGHT_SHOULDER
    elb_idx = LEFT_ELBOW if is_left else RIGHT_ELBOW
    wri_idx = LEFT_WRIST if is_left else RIGHT_WRIST

    sh = extract_landmark_point(landmarks, sh_idx)
    elb = extract_landmark_point(landmarks, elb_idx)
    wri = extract_landmark_point(landmarks, wri_idx)

    return calculate_angle_3d(sh, elb, wri)

def calculate_torso_upright_angle(landmarks: List[LandmarkInput]) -> float:
    if len(landmarks) < 33:
        return 0.0

    l_hip = extract_landmark_point(landmarks, LEFT_HIP)
    r_hip = extract_landmark_point(landmarks, RIGHT_HIP)
    l_sh = extract_landmark_point(landmarks, LEFT_SHOULDER)
    r_sh = extract_landmark_point(landmarks, RIGHT_SHOULDER)

    mid_hip = ((l_hip[0] + r_hip[0]) / 2.0, (l_hip[1] + r_hip[1]) / 2.0, (l_hip[2] + r_hip[2]) / 2.0)
    mid_sh = ((l_sh[0] + r_sh[0]) / 2.0, (l_sh[1] + r_sh[1]) / 2.0, (l_sh[2] + r_sh[2]) / 2.0)

    # Spine vector (Mid-Hip to Mid-Shoulder)
    spine = (mid_sh[0] - mid_hip[0], mid_sh[1] - mid_hip[1], mid_sh[2] - mid_hip[2])
    # Straight vertical vector pointing up (-y in image coordinates)
    vertical = (0.0, -1.0, 0.0)

    dot = spine[0] * vertical[0] + spine[1] * vertical[1] + spine[2] * vertical[2]
    mag_spine = math.sqrt(spine[0]**2 + spine[1]**2 + spine[2]**2)

    if mag_spine < 1e-6:
        return 0.0

    cos_val = dot / mag_spine
    cos_val = max(-1.0, min(1.0, cos_val))
    return math.degrees(math.acos(cos_val))
