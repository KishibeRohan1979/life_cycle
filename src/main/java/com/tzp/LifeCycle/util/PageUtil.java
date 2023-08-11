package com.tzp.LifeCycle.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 关于分页的一些自定义方法。包括ES和关系型数据库
 * IK_SMART和IK_MAX_WORD是ES中对查询时使用的分词器结构，在查询时会用到
 *   因此也将这个加入到这里，但是不代表两个内容是 PageUtil 的属性
 *
 * @author kangxvdong
 */

@Data
public class PageUtil<T> implements Serializable {

    public static final String IK_SMART = "ik_smart";

    public static final String IK_MAX_WORD = "ik_max_word";

    private Long total;

    private Long size;

    private Long totalPage;

    private Long currentPage;

    public PageUtil(Long total, Long size, Long totalPage, Long currentPage) {
        this.total = total;
        this.size = size;
        this.totalPage = totalPage;
        this.currentPage = currentPage;
    }

    /**
     * 判断一下用什么分词器
     * fastest最快的用ik_smart分词
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

    /**
     * 给ES分页用的自定义方法
     *
     * @param total 总行数
     * @param size 一页多少行
     * @param currentPage 当前页，由于ES第一页是0，所以在返回显示用的page时，+1代替
     * @return PageUtil对象
     */
    public static <T> PageUtil<T> getPage(Long total, Long size, Long currentPage) {
        long totalPage;
        if (total % size == 0) {
            totalPage = total / size;
        } else {
            totalPage = total / size + 1;
        }
        return new PageUtil<>(total, size, totalPage, currentPage+1);
    }

    /**
     * 大批量数据分页查询返回集合
     *
     * @param mapper 查询mapper类
     * @param wrapper 查询条件类
     * @param <T> 泛型对象
     * @return 查询结果集合
     */
    public static <T> List<T> getPageListByDataBase(BaseMapper<T> mapper, QueryWrapper<T> wrapper) {
        // 每页查询的数据量
        int pageSize = 1000;
        int currentPage = 1;
        boolean hasNextPage = true;
        List<T> resultList = new ArrayList<>();
        while (hasNextPage) {
            // 分页查询数据
            Page<T> page = new Page<>(currentPage, pageSize);
            Page<T> resultPage = mapper.selectPage(page, wrapper);

            resultList.addAll(resultPage.getRecords());

            // 判断是否还有下一页
            hasNextPage = resultPage.hasNext();
            currentPage++;
        }
        return resultList;
    }

}
