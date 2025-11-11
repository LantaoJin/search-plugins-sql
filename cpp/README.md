# OpenSearch SQL Native Engine POC

This is a proof-of-concept (POC) for a Java+C++ hybrid project, demonstrating how to integrate C++ native code into the OpenSearch SQL project.

## Project Structure

```
cpp/
├── CMakeLists.txt              # Main CMake configuration file
├── src/
│   ├── CMakeLists.txt         # Source CMake configuration
│   ├── core/
│   │   ├── Calculator.h       # C++ core calculation class header
│   │   └── Calculator.cpp     # C++ core calculation class implementation
│   └── jni/
│       └── NativeEngine.cpp   # JNI wrapper implementation
└── build/                     # CMake build directory (auto-generated)

native/
└── src/main/java/org/opensearch/sql/native/
    ├── NativeEngine.java      # Java JNI wrapper
    └── NativeEngineDemo.java  # Demo program
```

## Features

### 1. Java Calling C++
- `nativeAdd(int a, int b)` - Addition operation implemented in C++
- `nativeMultiply(int a, int b)` - Multiplication operation implemented in C++

### 2. C++ Calling Java
- `nativeCallJava()` - C++ calls Java callback method
- `onNativeCallback(String message)` - Java callback method

## Build Requirements

- Java 11+
- CMake 3.16+
- C++17 compatible compiler (GCC/Clang/MSVC)
- Make (Linux/macOS) or Visual Studio (Windows)

## Quick Start

### 1. Build Project
```bash
# Execute in project root directory
./gradlew :native:build
```

### 2. Run Demo
```bash
./gradlew :native:run
```

### 3. Run Tests
```bash
./gradlew :native:test
```

### 4. One-click Build and Run
```bash
./build-and-run-demo.sh
```

## Build Process

1. **C++ Compilation**: Gradle calls CMake to compile C++ code and generate dynamic library
2. **Library Copy**: Copy generated dynamic library to Java project's libs directory
3. **Java Compilation**: Compile Java code including JNI interfaces
4. **Runtime Loading**: Java runtime loads C++ library via `System.loadLibrary()`

## Extension Guide

### Adding New C++ Features
1. Add new C++ classes in `cpp/src/core/`
2. Add JNI wrapper functions in `cpp/src/jni/NativeEngine.cpp`
3. Declare corresponding native methods in `native/src/main/java/.../NativeEngine.java`

### JNI Method Naming Convention
```cpp
JNIEXPORT <return_type> JNICALL 
Java_<package_name_with_underscores>_<class_name>_<method_name>(JNIEnv *env, ...)
```

## Notes

- Ensure JNI method signatures match Java declarations exactly
- Pay attention to memory management to avoid memory leaks
- Exception handling needs to be considered on both C++ and Java sides
- Cross-platform compatibility needs to be configured in CMakeLists.txt

## Troubleshooting

### Common Issues

1. **UnsatisfiedLinkError**: 
   - Check if dynamic library is correctly generated and copied
   - Confirm `java.library.path` is set correctly

2. **CMake cannot find JNI**:
   - Ensure JAVA_HOME environment variable is set correctly
   - Check if JDK installation is complete

3. **Compilation errors**:
   - Confirm C++ compiler supports C++17
   - Check if CMake version meets requirements

## Performance Considerations

- JNI calls have overhead, suitable for compute-intensive tasks
- Avoid frequent small data JNI calls
- Consider batch processing to reduce JNI boundary crossings
