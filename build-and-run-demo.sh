#!/bin/bash

# Build and run the Java-C++ integration demo

set -e

echo "=== Building OpenSearch SQL Native Engine Demo ==="

# Check required tools
if ! command -v cmake &> /dev/null; then
    echo "Error: cmake is required but not installed."
    exit 1
fi

if ! command -v make &> /dev/null; then
    echo "Error: make is required but not installed."
    exit 1
fi

# Build project
echo "Building native module..."
./gradlew :native:build

# Run demo
echo ""
echo "Running demo..."
./gradlew :native:run -PmainClass=org.opensearch.sql.NativeEngineDemo

echo ""
echo "=== Build and demo completed ==="
