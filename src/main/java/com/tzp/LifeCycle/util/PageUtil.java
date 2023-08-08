package com.tzp.LifeCycle.util;

import lombok.Data;

import java.io.Serializable;

/**
 * es里面用来对分词的一种判断；
 * fastest最快的用ik_smart分词
 * 如果是其他的就用ik_max_word分词
 *
 * @author kangxvdong
 */

@Data
public class PageUtil implements Serializable {

    public static final String IK_SMART = "ik_smart";

    public static final String IK_MAX_WORD = "ik_max_word";

    private Long total;

    private Long size;

    private Long totalPage;

    private Long currentPage;

    /**
     * 判断一下用什么分词器
     *
     * @param type 分词类型
     * @return 返回es具体分词器
     */
    public static String getAnalyzerType(String type) {
        if ("fastest".equals(type)) {
            return IK_SMART;
        }
        return IK_MAX_WORD;
    }

    /**
     * 查询的时候把空格删掉
     *
     * @param queryString 查询的字符
     * @return 返回结果
     */
    public static String deleteNull (String queryString) {
        StringBuffer stringBuffer = new StringBuffer();
        String[] strings = queryString.split(" ");
        for (String str : strings) {
            stringBuffer.append(str);
        }
        return stringBuffer.toString();
    }

    public PageUtil(Long total, Long size, Long totalPage, Long currentPage) {
        this.total = total;
        this.size = size;
        this.totalPage = totalPage;
        this.currentPage = currentPage;
    }

    public static PageUtil getPage(Long total, Long size, Long currentPage) {
        long totalPage;
        if (total % size == 0) {
            totalPage = total / size;
        } else {
            totalPage = total / size + 1;
        }
        return new PageUtil(total, size, totalPage, currentPage);
    }

}
