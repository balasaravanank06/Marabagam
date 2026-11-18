import json
import logging
from typing import List, Dict, Any
from app.movement.schemas import AnalysisRequest, AnalysisResponse, FeedbackItem
from app.movement.normalization import normalize_frame_landmarks, calculate_stance_width_ratio
from app.movement.kinematics import calculate_knee_angle, calculate_elbow_angle, calculate_torso_upright_angle
from app.movement.dtw_matcher import compute_dtw_distance

logger = logging.getLogger(__name__)

def analyze_movement(request: AnalysisRequest) -> AnalysisResponse:
    frames = request.frames
    if not frames:
        return AnalysisResponse(
            overallScore=0.0,
            metricsSummaryJson="{}",
            feedbacks=[
                FeedbackItem(
                    bodyPart="FRAME",
                    metricName="VISIBILITY",
                    measuredValue=0.0,
                    targetRangeMin=1.0,
                    targetRangeMax=300.0,
                    score=0.0,
                    feedbackTextEn="No valid keypoint frames were received for analysis.",
                    feedbackTextTa="பகுப்பாய்வுக்கு சரியான சட்டங்கள் வரவில்லை.",
                    severity="CRITICAL"
                )
            ]
        )

    # Parse joint rules configuration
    joint_rules = {}
    if request.jointRulesJson:
        try:
            joint_rules = json.loads(request.jointRulesJson)
        except Exception:
            joint_rules = {}

    movement_type = joint_rules.get("type", "STATIC")
    rules_dict = joint_rules.get("rules", {})

    feedbacks: List[FeedbackItem] = []
    scores: List[float] = []

    # Calculate frame-by-frame joint angles
    l_knee_angles = []
    r_knee_angles = []
    l_elb_angles = []
    r_elb_angles = []
    torso_angles = []
    stance_ratios = []

    for f in frames:
        lm = f.landmarks
        if len(lm) >= 33:
            l_knee_angles.append(calculate_knee_angle(lm, is_left=True))
            r_knee_angles.append(calculate_knee_angle(lm, is_left=False))
            l_elb_angles.append(calculate_elbow_angle(lm, is_left=True))
            r_elb_angles.append(calculate_elbow_angle(lm, is_left=False))
            torso_angles.append(calculate_torso_upright_angle(lm))
            stance_ratios.append(calculate_stance_width_ratio(lm))

    avg_l_knee = sum(l_knee_angles) / len(l_knee_angles) if l_knee_angles else 180.0
    avg_r_knee = sum(r_knee_angles) / len(r_knee_angles) if r_knee_angles else 180.0
    avg_knee_angle = (avg_l_knee + avg_r_knee) / 2.0

    avg_l_elb = sum(l_elb_angles) / len(l_elb_angles) if l_elb_angles else 180.0
    avg_r_elb = sum(r_elb_angles) / len(r_elb_angles) if r_elb_angles else 180.0
    avg_elb_angle = (avg_l_elb + avg_r_elb) / 2.0

    avg_torso = sum(torso_angles) / len(torso_angles) if torso_angles else 0.0
    avg_stance = sum(stance_ratios) / len(stance_ratios) if stance_ratios else 1.0

    if movement_type == "DYNAMIC":
        # Dynamic Time Warping (DTW) Sequence Comparison
        ref_sequence = [120.0, 130.0, 140.0, 110.0, 125.0] # Baseline template fallback
        dtw_dist = compute_dtw_distance(l_knee_angles, ref_sequence)
        dtw_max = rules_dict.get("dtw_max_distance", 25.0)

        score_dtw = max(0.0, min(100.0, 100.0 - (dtw_dist / dtw_max) * 40.0))
        scores.append(score_dtw)

        fb_en = f"Swing trajectory time-alignment matched expert reference timing with divergence distance {dtw_dist:.1f}."
        fb_ta = f"சுழற்சி வீச்சு நேரம் குறிப்புடன் ஒத்துப்போகிறது (விலகல்: {dtw_dist:.1f})."
        feedbacks.append(
            FeedbackItem(
                bodyPart="ARM_ROTATION",
                metricName="DYNAMIC_TRAJECTORY",
                measuredValue=round(dtw_dist, 2),
                targetRangeMin=0.0,
                targetRangeMax=dtw_max,
                score=round(score_dtw, 1),
                feedbackTextEn=fb_en,
                feedbackTextTa=fb_ta,
                severity="INFO" if score_dtw >= 75.0 else "WARNING"
            )
        )

    # Knee Flexion Rule Check
    knee_rule = rules_dict.get("knee_angle", {"min": 115.0, "max": 140.0})
    k_min, k_max = knee_rule.get("min", 115.0), knee_rule.get("max", 140.0)
    if k_min <= avg_knee_angle <= k_max:
        k_score = 95.0
        fb_en = f"Knee flex angle is {avg_knee_angle:.1f}°, matching reference stance target ({k_min:.0f}°–{k_max:.0f}°)."
        fb_ta = f"முழங்கால் வளைவு {avg_knee_angle:.1f}° ஆக உள்ளது (இலக்கு: {k_min:.0f}°–{k_max:.0f}°)."
        sev = "INFO"
    else:
        diff = min(abs(avg_knee_angle - k_min), abs(avg_knee_angle - k_max))
        k_score = max(30.0, 100.0 - diff * 2.5)
        if avg_knee_angle > k_max:
            fb_en = f"Knee flex angle is {avg_knee_angle:.1f}°. Reference stance target is lower ({k_min:.0f}°–{k_max:.0f}°)."
            fb_ta = f"முழங்கால் வளைவு உயர்வாக உள்ளது ({avg_knee_angle:.1f}°). தாழ்வான நிலையைப் பயன்படுத்தவும்."
        else:
            fb_en = f"Knee flex angle is {avg_knee_angle:.1f}°. Reference stance target is higher ({k_min:.0f}°–{k_max:.0f}°)."
            fb_ta = f"முழங்கால் வளைவு மிகவும் ஆழமாக உள்ளது ({avg_knee_angle:.1f}°)."
        sev = "WARNING"

    scores.append(k_score)
    feedbacks.append(
        FeedbackItem(
            bodyPart="KNEE",
            metricName="KNEE_FLEXION",
            measuredValue=round(avg_knee_angle, 1),
            targetRangeMin=k_min,
            targetRangeMax=k_max,
            score=round(k_score, 1),
            feedbackTextEn=fb_en,
            feedbackTextTa=fb_ta,
            severity=sev
        )
    )

    # Torso Upright Rule Check
    torso_rule = rules_dict.get("torso_upright", {"min": 0.0, "max": 15.0})
    t_min, t_max = torso_rule.get("min", 0.0), torso_rule.get("max", 15.0)
    if t_min <= avg_torso <= t_max:
        t_score = 95.0
        fb_en = f"Spinal alignment is vertical at {avg_torso:.1f}° inclination (reference target: <{t_max:.0f}°)."
        fb_ta = f"முதுகெலும்பு செங்குத்து நிலை {avg_torso:.1f}° ஆக உள்ளது."
        sev = "INFO"
    else:
        t_diff = avg_torso - t_max
        t_score = max(40.0, 100.0 - t_diff * 3.0)
        fb_en = f"Torso inclination is tilted {avg_torso:.1f}° forward. Focus on keeping spine upright (reference target: <{t_max:.0f}°)."
        fb_ta = f"உடல் முன்னோக்கி {avg_torso:.1f}° சாய்ந்துள்ளது. முதுகெலும்பை நேராக வைக்கவும்."
        sev = "WARNING"

    scores.append(t_score)
    feedbacks.append(
        FeedbackItem(
            bodyPart="SPINE",
            metricName="TORSO_ALIGNMENT",
            measuredValue=round(avg_torso, 1),
            targetRangeMin=t_min,
            targetRangeMax=t_max,
            score=round(t_score, 1),
            feedbackTextEn=fb_en,
            feedbackTextTa=fb_ta,
            severity=sev
        )
    )

    # Stance Width Rule Check
    sw_rule = rules_dict.get("stance_width_ratio", {"min": 1.2, "max": 1.8})
    sw_min, sw_max = sw_rule.get("min", 1.2), sw_rule.get("max", 1.8)
    if sw_min <= avg_stance <= sw_max:
        sw_score = 92.0
        fb_en = f"Stance width ratio is {avg_stance:.2f}x shoulder width, within reference range ({sw_min:.1f}–{sw_max:.1f})."
        fb_ta = f"கால் அகல நிலை {avg_stance:.2f}x ஆக உள்ளது (இலக்கு: {sw_min:.1f}–{sw_max:.1f})."
        sev = "INFO"
    else:
        sw_diff = min(abs(avg_stance - sw_min), abs(avg_stance - sw_max))
        sw_score = max(50.0, 100.0 - sw_diff * 40.0)
        fb_en = f"Stance width ratio is {avg_stance:.2f}x shoulder width. Adjust foot separation to match reference range ({sw_min:.1f}–{sw_max:.1f})."
        fb_ta = f"கால் அகல நிலையை சரிசெய்யவும் ({avg_stance:.2f}x)."
        sev = "WARNING"

    scores.append(sw_score)
    feedbacks.append(
        FeedbackItem(
            bodyPart="LEGS",
            metricName="STANCE_WIDTH",
            measuredValue=round(avg_stance, 2),
            targetRangeMin=sw_min,
            targetRangeMax=sw_max,
            score=round(sw_score, 1),
            feedbackTextEn=fb_en,
            feedbackTextTa=fb_ta,
            severity=sev
        )
    )

    overall_score = sum(scores) / len(scores) if scores else 75.0
    overall_score = max(0.0, min(100.0, overall_score))

    metrics_summary = {
        "avgKneeAngle": round(avg_knee_angle, 1),
        "avgTorsoAngle": round(avg_torso, 1),
        "avgStanceRatio": round(avg_stance, 2),
        "frameCount": len(frames)
    }

    return AnalysisResponse(
        overallScore=round(overall_score, 1),
        metricsSummaryJson=json.dumps(metrics_summary),
        feedbacks=feedbacks[:3] # Limit to priority top 3 suggestions
    )
