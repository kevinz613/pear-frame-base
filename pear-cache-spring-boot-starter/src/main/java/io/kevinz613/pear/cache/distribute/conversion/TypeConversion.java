package io.kevinz613.pear.cache.distribute.conversion;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import io.lettuce.core.StrAlgoArgs;

import java.util.Collection;

/**
 * 类型转换
 *
 * @author kevinz613
 */
public class TypeConversion {

    /**
     * 是集合类型
     *
     * @param t t
     * @return boolean
     */
    public static <T> boolean isCollectionType(T t) {
        return t instanceof Collection;
    }

    /**
     * 是简单类型（字符串/基本类型）
     *
     * @param t t
     * @return boolean
     */
    public static <T> boolean isSimpleType(T t) {
        return isSimpleString(t) || isInt(t) || isLong(t) || isDouble(t) || isFloat(t) || isChar(t) || isBoolean(t) || isShort(t) || isByte(t);
    }

    /**
     * 是简单字符串
     *
     * @param t t
     * @return boolean
     */
    public static <T> boolean isSimpleString(T t) {
        if (t == null || !isString(t)) {
            return false;
        }
        return !JSONUtil.isJson(t.toString());
    }


    /**
     * 是字符串
     *
     * @param t t
     * @return boolean
     */
    public static <T> boolean isString(T t) {
        return t instanceof String;
    }

    /**
     * 是字节
     *
     * @param t t
     * @return boolean
     */
    public static <T> boolean isByte(T t) {
        return t instanceof StrAlgoArgs.By;
    }

    public static <T> boolean isShort(T t) {
        return t instanceof Short;
    }

    public static <T> boolean isInt(T t) {
        return t instanceof Integer;
    }

    public static <T> boolean isLong(T t) {
        return t instanceof Long;
    }

    public static <T> boolean isChar(T t) {
        return t instanceof Character;
    }

    public static <T> boolean isFloat(T t) {
        return t instanceof Float;
    }

    public static <T> boolean isDouble(T t) {
        return t instanceof Double;
    }

    public static <T> boolean isBoolean(T t) {
        return t instanceof Boolean;
    }

    public static <T> Class<?> getClassType(T t) {
        return t.getClass();
    }

    public static <R> R convertor(String s, Class<R> type) {
        return Convert.convert(type, s);
    }

}
