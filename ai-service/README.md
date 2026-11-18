# Marabagam AI Microservice

## Overview
FastAPI microservice providing AI capability foundations for Marabagam.

## Technical Stack
- Python 3.12
- FastAPI
- Pydantic v2
- Uvicorn
- Pytest

## Running Natively
```bash
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

## Running Tests
```bash
pytest
```
