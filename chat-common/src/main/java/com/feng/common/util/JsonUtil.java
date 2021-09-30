package com.feng.common.util;

import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @Description Json 处理工具类
 * @Author fengsy
 * @Date 9/29/21
 */
public class JsonUtil {

    private static GsonBuilder builder = new GsonBuilder();

    private static final Gson gson;

    static {
        // 不需要html escape
        builder.disableHtmlEscaping();
        gson = new Gson();
    }

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
     * 使用谷歌 Gson 将 POJO object 转成字符串
     *
     * @param obj
     * @return
     */
    public static String pojoToJson(Object obj) {
        String json = gson.toJson(obj);
        return json;
    }

    /**
     * 将json bytes 转换成pojo对象
     * 
     * @param bytes
     * @param tClass
     * @param <T>
     *            pojo对象的类型
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
     * @param <T>
     *            pojo对象的类型
     * @return pojo对象
     */
    public static <T> T jsonToPojo(String json, Class<T> tClass) {
        T t = gson.fromJson(json, tClass);
        return t;
    }
}
