#pragma once

#include <substrait/plan.pb.h>
#include <vector>
#include <cstdint>
#include <string>

class SubstraitPlanProcessor {
public:
    static std::string processPlanFromBytes(const std::vector<uint8_t>& planBytes);
    static void processPlan(const ::substrait::Plan& plan);
    
private:
    static bool parseProtobuf(const uint8_t* buf, int bufLen, ::substrait::Plan* plan);
    static std::string substraitFromPbToJson(const uint8_t* data, int32_t size);
};
