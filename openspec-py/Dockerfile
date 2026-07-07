FROM python:3.12-slim-bookworm

# Run as host user (avoids permission issues with mounted volumes)
ARG USER_ID=1000
ARG GROUP_ID=1000
RUN groupadd -g ${GROUP_ID} appuser 2>/dev/null || true \
    && useradd -u ${USER_ID} -g ${GROUP_ID} -m -s /bin/bash appuser 2>/dev/null || true

WORKDIR /app

# Install dependencies (pymysql is pure-Python, no build tools needed)
COPY requirements.txt requirements-dev.txt ./
RUN pip install --no-cache-dir -r requirements.txt -r requirements-dev.txt

RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8000
# --reload keeps the process alive (in-memory state persists between requests)
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]

