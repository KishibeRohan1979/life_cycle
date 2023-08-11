package com.tzp.LifeCycle.dto;

import co.elastic.clients.elasticsearch._types.SortOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * es查询的时候使用的泛用类
 *
 * @author kangxvdong
 */
@Data
public class EsQueryDto<T> {

    @ApiModelProperty(value = "索引名称", name = "indexName")
    private String indexName;

    @ApiModelProperty(value = "评论类型", name = "type")
    private String type;

    @ApiModelProperty(value = "分词搜索关键字对象类型", name = "queryClazz")
    private Class<T> queryClazz;

    @ApiModelProperty(value = "分词搜索关键字对象，可以通过传一组key-value来精确查询", name = "queryObject")
    private T queryObject;

    @ApiModelProperty(value = "分词搜索关键字值", name = "queryString")
    private String queryString;

    @ApiModelProperty(value = "分词搜索匹配字段", name = "queryFields")
    private List<String> queryFields;

    @ApiModelProperty(value = "分词搜索匹配字段及其字段值", name = "matchMap")
    private Map<String, Object> matchMap;

    @ApiModelProperty(value = "精确搜索匹配字段及其字段值", name = "termMap")
    private Map<String, Object> termMap;

    @ApiModelProperty(value = "页码", name = "pageNum", dataType="int", notes="当前页码", required=true)
    private Long pageNum;

    @ApiModelProperty(value = "分页条数", name = "pageSize", dataType="int", notes="返回多少行信息", required=true)
    private Long pageSize;

    @ApiModelProperty(value = "排序字段", name = "orderField")
    private String orderField;

    @ApiModelProperty(value = "排序方式 asc/desc", name = "orderType")
    private String orderType;

    @ApiModelProperty(value = "高亮搜索字段", name = "highlightFields")
    private List<String> highlightFields;

    @ApiModelProperty(value = "分词方式", name = "analyzerType")
    private String analyzerType;

    @ApiModelProperty(value = "范围查询字段", name = "rangeField")
    private String rangeField;

    @ApiModelProperty(value = "范围-开始值", name = "startValue")
    private String  startValue;

    @ApiModelProperty(value = "范围-结束值", name = "endValue")
    private String  endValue;

    public String getOrderType() {
        if (StringUtils.isBlank(orderType)) {
            orderType = SortOrder.Desc.name();
        }
        return orderType;
    }

    public Long getPageSize() {
        return pageSize == 0 ? 10 : pageSize;
    }

    public Long getPageNum() {
        return pageNum != 0 ? pageNum - 1 : 0;
    }

}
