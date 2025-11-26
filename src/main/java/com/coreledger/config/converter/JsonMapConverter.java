package com.coreledger.config.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON Map 转换器
 * <p>用于 JPA 实体中 Map<String, String> 与数据库 VARCHAR(JSON) 的转换</p>
 * <p>主要用于商品SKU的属性值映射 attr_value_map</p>
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Slf4j
@Converter(autoApply = false)
public class JsonMapConverter implements AttributeConverter<Map<String, String>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting Map to JSON", e);
            return null;
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to Map: {}", dbData, e);
            return new HashMap<>();
        }
    }
}
