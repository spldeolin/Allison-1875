package com.spldeolin.allison1875.common.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.spldeolin.allison1875.common.exception.JsonException;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON工具类
 *
 * @author Deolin 2018-04-02
 */
@Slf4j
public class JsonUtils {

    private static final ObjectMapper om = createObjectMapper();

    private JsonUtils() {
        throw new UnsupportedOperationException("Never instantiate me.");
    }

    /**
     * 创建一个基础ObjectMapper对象
     */
    public static ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();

        // 反序列化时，忽略DataModel中不存在的属性，而不是抛出异常
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 序列化没有property的DataModel时，当作{ }，而不是抛出异常
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 使用所在操作系统的时区
        om.setTimeZone(TimeZone.getDefault());

        // Java8 LocalDateTime、LocalDate、LocalTime的pattern（yyyy-MM-dd HH:mm:ss、yyyy-MM-dd、HH:mm:ss）
        om.registerModule(java8timeSimplePattern());

        // Java1 java.util.Date的pattern（yyyy-MM-dd HH:mm:ss）
        om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        return om;
    }

    public static String toJson(Object object) {
        return toJson(object, om);
    }

    public static String toJson(Object object, ObjectMapper om) {
        try {
            return om.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("object={}", object, e);
            throw new JsonException(e);
        }
    }

    public static String toJsonPrettily(Object object) {
        return toJsonPrettily(object, om);
    }

    public static String toJsonPrettily(Object object, ObjectMapper om) {
        try {
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("object={}", object, e);
            throw new JsonException("转化JSON失败");
        }
    }

    /**
     * 压缩JSON（去除美化JSON中多余的换行与空格，如果参数字符串不是一个JSON，则无事发生）
     */
    public static String compressJson(String json) {
        try {
            Map<?, ?> map = om.readValue(json, Map.class);
            return toJson(map);
        } catch (JsonProcessingException e) {
            // is not a json
            return json;
        }
    }

    public static <T> T toObject(String json, Class<T> clazz) throws JsonException {
        return toObject(json, clazz, om);
    }

    public static <T> T toObject(String json, Class<T> clazz, ObjectMapper om) throws JsonException {
        try {
            return om.readValue(json, clazz);
        } catch (IOException e) {
            log.error("json={}, clazz={}", json, clazz, e);
            throw new JsonException(e);
        }
    }

    public static <T> List<T> toListOfObject(String json, Class<T> clazz) throws JsonException {
        return toListOfObject(json, clazz, om);
    }

    public static <T> List<T> toListOfObject(String json, Class<T> clazz, ObjectMapper om) throws JsonException {
        try {
            CollectionType collectionType = om.getTypeFactory().constructCollectionType(List.class, clazz);
            return om.readValue(json, collectionType);
        } catch (IOException e) {
            log.error("json={}, clazz={}", json, clazz, e);
            throw new JsonException(e);
        }
    }

    public static <T> T toParameterizedObject(String json, TypeReference<T> typeReference) throws JsonException {
        return toParameterizedObject(json, typeReference, om);
    }

    public static <T> T toParameterizedObject(String json, TypeReference<T> typeReference, ObjectMapper om)
            throws JsonException {
        try {
            return om.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("json={}, typeReference={}", json, typeReference, e);
            throw new JsonException(e);
        }
    }

    public static JsonNode toTree(String json) throws JsonException {
        return toTree(json, om);
    }

    public static JsonNode toTree(String json, ObjectMapper om) throws JsonException {
        try {
            return om.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("json={}", json, e);
            throw new JsonException(e);
        }
    }

    private static SimpleModule java8timeSimplePattern() {
        SimpleModule module = new JavaTimeModule();
        DateTimeFormatter ldtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter ldFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter ltFormattter = DateTimeFormatter.ofPattern("HH:mm:ss");
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(ldtFormatter))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(ldtFormatter))
                .addSerializer(LocalDate.class, new LocalDateSerializer(ldFormatter))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(ldFormatter))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(ltFormattter))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(ltFormattter));
        return module;
    }

}