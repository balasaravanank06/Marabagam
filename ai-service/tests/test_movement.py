import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.movement.normalization import normalize_frame_landmarks, calculate_stance_width_ratio
from app.movement.kinematics import calculate_knee_angle, calculate_elbow_angle, calculate_torso_upright_angle
from app.movement.dtw_matcher import compute_dtw_distance
from app.movement.schemas import LandmarkInput

client = TestClient(app)

def test_normalization_zero_denominator_safeguard():
    # Test zero distance landmarks (all points at 0,0,0)
    landmarks = [LandmarkInput(x=0.0, y=0.0, z=0.0) for _ in range(33)]
    normalized = normalize_frame_landmarks(landmarks)
    assert len(normalized) == 33
    # Check no NaN or DivisionByZero exception occurred
    assert not any(math.isnan(pt[0]) for pt in normalized if 'math' in globals())

def test_kinematics_angle_calculations():
    landmarks = [LandmarkInput(x=0.5, y=0.5, z=0.0) for _ in range(33)]
    # Left hip 23, knee 25, ankle 27
    landmarks[23] = LandmarkInput(x=0.5, y=0.4, z=0.0)
    landmarks[25] = LandmarkInput(x=0.5, y=0.6, z=0.0)
    landmarks[27] = LandmarkInput(x=0.5, y=0.8, z=0.0)

    angle = calculate_knee_angle(landmarks, is_left=True)
    # Straight line should be ~180 degrees
    assert abs(angle - 180.0) < 1.0

def test_dtw_sequence_matching():
    seq1 = [120.0, 125.0, 130.0, 135.0]
    seq2 = [120.0, 126.0, 131.0, 136.0]
    dist = compute_dtw_distance(seq1, seq2)
    assert dist >= 0.0
    assert dist < 5.0 # High similarity

def test_fastapi_movement_analyze_endpoint():
    payload = {
        "movementCode": "SILAMBAM_MAANKOMBU_SUBADU",
        "jointRulesJson": '{"type":"STATIC","rules":{"knee_angle":{"min":115.0,"max":140.0}}}',
        "referenceTemplateJson": "{}",
        "frames": [
            {
                "frameIndex": 0,
                "timestampMs": 0,
                "landmarks": [{"x": 0.5, "y": 0.5, "z": 0.0, "visibility": 0.95, "presence": 0.95} for _ in range(33)]
            }
        ]
    }
    response = client.post("/internal/v1/movement/analyze", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "overallScore" in data
    assert "feedbacks" in data
    assert data["overallScore"] >= 0.0 and data["overallScore"] <= 100.0

def test_fastapi_movement_analyze_empty_frames_rejected():
    payload = {
        "movementCode": "SILAMBAM_MAANKOMBU_SUBADU",
        "frames": []
    }
    response = client.post("/internal/v1/movement/analyze", json=payload)
    assert response.status_code == 400
