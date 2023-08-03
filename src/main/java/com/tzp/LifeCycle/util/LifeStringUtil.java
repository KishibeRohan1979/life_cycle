package com.tzp.LifeCycle.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本模块关于String类型字符串的共有方法
 *
 * @author kangxvdong
 */
public class LifeStringUtil {

    /**
     * 首字母大写
     *
     * @param str 需要转化的英语字符串
     * @return 首字母大写的字符串
     */
    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 去重+要显示的字段
     *
     * @param showFields 需要显示的字段列表
     * @return 拼接好的字符串
     */
    public static String getSelectFields(List<String> showFields) {
        if (showFields != null && !showFields.isEmpty()) {
            StringBuffer strBuf = new StringBuffer();
            for (String str : showFields) {
                strBuf.append(str).append(",");
            }
            // 去掉最后一个空格
            strBuf.delete(strBuf.length() - 1, strBuf.length());
            // 头部添加去重标志
            strBuf.insert(0, "distinct ");
            return strBuf.toString();
        } else {
            return "distinct *";
        }
    }

    /**
     * 将一个map中的key值转换一种格式，例如studentId、userName转换的结果就是student_id、user_name
     *
     * @param map 需要转换key的map
     * @return 转化好的map
     */
    public static Map<String, Object> convertMapKeys(Map<String, Object> map) {
        Map<String, Object> convertedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String originalKey = entry.getKey();
            String convertedKey = convertString(originalKey);
            convertedMap.put(convertedKey, entry.getValue());
        }

        return convertedMap;
    }

    /**
     * 将字符串进行转换，例如studentId、userName转换的结果就是student_id、user_name
     * 将驼峰命名转换为下划线命名
     *
     * @param input 提供需要转化的字符串
     * @return 转化好的字符串
     */
    public static String convertString(String input) {
        // 将驼峰命名转换为下划线命名
        StringBuilder converted = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                converted.append("_").append(Character.toLowerCase(c));
            } else {
                converted.append(c);
            }
        }

        return converted.toString();
    }

}
