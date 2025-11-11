# Create JNI library
add_library(opensearch-sql-native SHARED
    jni/NativeEngine.cpp
    core/Calculator.cpp
    velox/SubstraitPlanProcessor.cpp
    ${ALL_PROTO_SRCS}
)

# Link JNI libraries and protobuf
target_link_libraries(opensearch-sql-native ${JNI_LIBRARIES} ${Protobuf_LIBRARIES})

# Set library output name
set_target_properties(opensearch-sql-native PROPERTIES
    OUTPUT_NAME "opensearch-sql-native"
    PREFIX ""
)
