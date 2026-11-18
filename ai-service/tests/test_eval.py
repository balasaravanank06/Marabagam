import pytest
import json
import os
import asyncio
from app.rag.rag_service import rag_service

def test_ai_evaluation_benchmark():
    dataset_path = os.path.join(os.path.dirname(__file__), "eval_dataset.json")
    with open(dataset_path, "r", encoding="utf-8") as f:
        dataset = json.load(f)

    total_queries = len(dataset)
    grounded_hits = 0
    refusal_correct = 0
    total_insufficient = 0

    for item in dataset:
        res = asyncio.run(rag_service.query_cultural_guide(item["query"]))
        answer = res["answer"]
        
        if item["is_sufficient_evidence"]:
            # Check if keywords are matched in grounded answer
            if any(k.lower() in answer.lower() for k in item["expected_answer_keywords"]):
                grounded_hits += 1
        else:
            total_insufficient += 1
            if "sufficient" in answer.lower() or "போதுமான" in answer:
                refusal_correct += 1

    groundedness_score = (grounded_hits / (total_queries - total_insufficient)) * 100 if (total_queries - total_insufficient) > 0 else 100.0
    refusal_accuracy = (refusal_correct / total_insufficient) * 100 if total_insufficient > 0 else 100.0

    print(f"\nAI Benchmark Evaluation Results:")
    print(f"Groundedness Score: {groundedness_score:.2f}% (Target: >= 95%)")
    print(f"Refusal Accuracy: {refusal_accuracy:.2f}% (Target: 100%)")

    assert groundedness_score >= 95.0
    assert refusal_accuracy == 100.0
