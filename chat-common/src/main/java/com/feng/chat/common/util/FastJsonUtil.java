package com.feng.chat.common.util;

import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

/**
 * @Description Json 处理工具类 fastjson 实现
 * @Author fengsy
 * @Date 9/29/21
 */
public class FastJsonUtil {

    /**
     * 使用谷歌 Gson 将 POJO object 转成字符串后，进一步转成字节数组
     *
     * @param obj
     * @return
     */
    public static byte[] object2JsonBytes(Object obj) {

        // 把对象转换成JSON

        String json = pojoToJson(obj);
        try {
            return json.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将 POJO object 转成字符串
     *
     * @param obj
     * @return
     */
    public static String pojoToJson(Object obj) {
        String json = JSON.toJSONString(obj);
        return json;
    }

    /**
     * 将json bytes 转换成pojo对象
     *
     * @param bytes
     * @param tClass
     * @param <T>    pojo对象的类型
     * @return pojo对象
     */
    public static <T> T jsonBytes2Object(byte[] bytes, Class<T> tClass) {
        try {
            String json = new String(bytes, "UTF-8");
            T t = jsonToPojo(json, tClass);
            return t;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将json字符串转换成pojo对象
     *
     * @param json
     * @param tClass
     * @param <T>    pojo对象的类型
     * @return pojo对象
     */
    public static <T> T jsonToPojo(String json, Class<T> tClass) {
        T t = JSON.parseObject(json, tClass);
        return t;
    }

    public static <T> T jsonToPojo(String json, Type type) {
        T t = JSON.parseObject(json, type);
        return t;
    }
}
