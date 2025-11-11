#include "SubstraitPlanProcessor.h"
#include <iostream>
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl.h>
#include <google/protobuf/util/json_util.h>
#include <google/protobuf/util/type_resolver_util.h>

std::string SubstraitPlanProcessor::processPlanFromBytes(const std::vector<uint8_t>& planBytes) {
    std::cout << "=== Substrait Plan Processing ===\n";
    std::cout << "Plan size: " << planBytes.size() << " bytes\n";
    
    try {
        // Generate JSON from protobuf bytes (like Gluten's parsePlan)
        auto planJson = substraitFromPbToJson(planBytes.data(), planBytes.size());
        std::cout << "Substrait Plan JSON:\n" << planJson << "\n";
        
        // Also parse and show basic info
        ::substrait::Plan plan;
        if (parseProtobuf(planBytes.data(), planBytes.size(), &plan)) {
            processPlan(plan);
        }
        
        std::cout << "=================================\n";
        return planJson;
    } catch (const std::exception& e) {
        std::string error = "Error converting substrait::Plan to JSON: " + std::string(e.what());
        std::cout << error << "\n";
        std::cout << "=================================\n";
        return error;
    }
}

void SubstraitPlanProcessor::processPlan(const ::substrait::Plan& plan) {
    std::cout << "Plan Summary:\n";
    std::cout << "  Relations count: " << plan.relations_size() << "\n";
    
    if (plan.has_version()) {
        std::cout << "  Version: " << plan.version().major_number() << "." 
                  << plan.version().minor_number() << "." 
                  << plan.version().patch_number() << "\n";
    }
    
    std::cout << "  Extensions count: " << plan.extensions_size() << "\n";
}

bool SubstraitPlanProcessor::parseProtobuf(const uint8_t* buf, int bufLen, ::substrait::Plan* plan) {
    google::protobuf::io::CodedInputStream codedStream{buf, bufLen};
    codedStream.SetRecursionLimit(100000);
    return plan->ParseFromCodedStream(&codedStream);
}

std::string SubstraitPlanProcessor::substraitFromPbToJson(const uint8_t* data, int32_t size) {
    std::string typeUrl = "/substrait.Plan";
    
    google::protobuf::io::ArrayInputStream bufStream{data, size};
    
    std::string out;
    google::protobuf::io::StringOutputStream outStream{&out};
    
    // Create type resolver for substrait types
    std::unique_ptr<google::protobuf::util::TypeResolver> typeResolver(
        google::protobuf::util::NewTypeResolverForDescriptorPool(
            "", google::protobuf::DescriptorPool::generated_pool()));
    
    auto status = google::protobuf::util::BinaryToJsonStream(
        typeResolver.get(), typeUrl, &bufStream, &outStream);
    
    if (!status.ok()) {
        throw std::runtime_error("BinaryToJsonStream failed: " + status.ToString());
    }
    
    return out;
}
