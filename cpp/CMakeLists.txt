cmake_minimum_required(VERSION 3.16)
project(opensearch-sql-native)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Set output directories
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

# Find JNI
find_package(JNI REQUIRED)
if(JNI_FOUND)
    message(STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message(STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
endif()

# Find Protobuf
find_package(Protobuf REQUIRED)

# Generate protobuf files
set(PROTO_DIR ${CMAKE_SOURCE_DIR}/proto)
set(GENERATED_DIR ${CMAKE_BINARY_DIR}/generated)
file(MAKE_DIRECTORY ${GENERATED_DIR})

# Generate protobuf C++ files
execute_process(
    COMMAND ${Protobuf_PROTOC_EXECUTABLE} 
        --proto_path=${PROTO_DIR}
        --cpp_out=${GENERATED_DIR}
        ${PROTO_DIR}/substrait/plan.proto
        ${PROTO_DIR}/substrait/algebra.proto
        ${PROTO_DIR}/substrait/type.proto
        ${PROTO_DIR}/substrait/extensions/extensions.proto
    WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
)

# Collect generated files
file(GLOB PROTO_SRCS "${GENERATED_DIR}/substrait/*.pb.cc")
file(GLOB PROTO_HDRS "${GENERATED_DIR}/substrait/*.pb.h")
file(GLOB PROTO_EXT_SRCS "${GENERATED_DIR}/substrait/extensions/*.pb.cc")
file(GLOB PROTO_EXT_HDRS "${GENERATED_DIR}/substrait/extensions/*.pb.h")

# Combine all proto sources
set(ALL_PROTO_SRCS ${PROTO_SRCS} ${PROTO_EXT_SRCS})

# Include directories
include_directories(${JNI_INCLUDE_DIRS})
include_directories(${Protobuf_INCLUDE_DIRS})
include_directories(${GENERATED_DIR})
include_directories(src)

# Add subdirectories
add_subdirectory(src)
