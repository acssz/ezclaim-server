#!/usr/bin/env bash
set -euo pipefail

# Build the Spring Boot jar using a Dockerized Maven+JDK toolchain.
# This script requires Docker and internet access for Maven dependencies.

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
IMAGE="maven:3.9-eclipse-temurin-24"

echo "[build] Using image: $IMAGE"
echo "[build] Project root: $ROOT_DIR"

# Create a local Maven cache directory to speed up repeated builds
mkdir -p "$ROOT_DIR/.m2"

docker run --rm \
  -v "$ROOT_DIR":/workspace \
  -v "$ROOT_DIR/.m2":/root/.m2 \
  -w /workspace \
  "$IMAGE" \
  mvn -B -e clean package

echo "[build] Build completed. Jar(s) in target/"

