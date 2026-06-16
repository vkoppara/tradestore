FROM python:3.12-slim

WORKDIR /app

# Full set of runtime libs required by Docling's dependency chain:
#   libgomp1:     OpenMP (PyTorch CPU kernels)
#   libglib2.0-0: GLib (OpenCV, various)
#   libgl1:       OpenGL — libGL.so.1 (OpenCV)
#   libglx-mesa0: GLX backend needed by libgl1 on Debian slim
#   libxcb1:      X11 client lib (torch/torchvision headless)
#   libxext6:     X11 extension lib
#   libsm6:       X11 session management (OpenCV)
#   libxrender1:  X Rendering Extension (OpenCV)
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       libgomp1 \
       libglib2.0-0 \
       libgl1 \
       libglx-mesa0 \
       libxcb1 \
       libxext6 \
       libsm6 \
       libxrender1 \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# HF_HOME points to the shared Docker volume (loan-market_docling-models).
# worker.py calls StandardPdfPipeline.download_models_hf(force=False) at startup:
#   - first run : downloads models (~1.5 GB) into this volume, then sets HF_HUB_OFFLINE=1
#   - every run after: finds models already in volume, sets HF_HUB_OFFLINE=1 immediately
# No HF network calls happen during actual PDF processing.
ENV HF_HOME=/models
ENV PYTHONUNBUFFERED=1

CMD ["python", "worker.py", "--help"]
