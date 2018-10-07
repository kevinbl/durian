package com.foo.durian.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Json解析工具类
 */
public class JsonUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final Splitter SPLITTER = Splitter.on("{");
    /**
     * json字符串解析成java对象
     *
     * @param <T>
     * @param jsonStr
     * @return
     */
    public static <T> T jsonToObject(String jsonStr, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            log.error("json解析异常,参数：{}", jsonStr, e);
            return null;
        }
    }

    /**
     *原始的转换
     */
    public static JSONObject jsonToJSONObject(String jsonStr){
        return (JSONObject) JSONValue.parse(jsonStr);
    }
    public static List<JSONObject> jsonObjectList(String jsonStr){
        return null;
    }
    /**
     * json字符串解析成java对象列表
     *
     * @param <T>
     * @param jsonStr
     * @return
     */
    public static <T> List<T> jsonToObjectList(String jsonStr, Class<T> clazz) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, clazz);
        try {
            return objectMapper.readValue(jsonStr, javaType);
        } catch (Exception e) {
            log.error("json解析异常,参数：{}", jsonStr, e);
            return null;
        }
    }

    /**
     * java对象转json
     *
     * @param <T>
     * @param t
     * @return
     */
    public static <T> String objectToJson(T t) {
        try {
            return objectMapper.writeValueAsString(t);
        } catch (Exception e) {
            log.error("对象转json异常,参数：{}", t, e);
            return null;
        }
    }

    /**
     * java对象列表转json
     *
     * @param <T>
     * @param
     * @return
     */
    public static <T> String objectListToJson(List<T> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("对象列表转json异常", e);
            return null;
        }
    }

    /**
     *拆分括号
     */
    public static List<String> splitterForService(String target){
        target = target.replace("[","").replace("]","");
        List<String> listResult = Lists.newArrayList();
        List<String> listTarget = SPLITTER.splitToList(target);
        for(int i=1;i<listTarget.size();i++){
            if(i==listTarget.size()-1){
                listResult.add("{"+listTarget.get(i));
                continue;
            }
            listResult.add("{"+listTarget.get(i).substring(0,listTarget.get(i).length()-1));
        }
        return listResult;
    }
}