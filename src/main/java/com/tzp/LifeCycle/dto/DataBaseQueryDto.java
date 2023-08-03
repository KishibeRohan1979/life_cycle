package com.tzp.LifeCycle.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 自定义查询类；
 * 目前还没有完全完善；之后看具体情况再改
 *
 * @author kangxvdong
 */
@Data
public class DataBaseQueryDto {

    @ApiModelProperty(value = "查找与字段值相等的数据", name = "queryAllEqualFields", dataType="Map<String, Object>")
    private Map<String, Object> queryAllEqualFields;

    @ApiModelProperty(value = "主键id", name = "pageSize", dataType="Long")
    private Long pageSize;

    @ApiModelProperty(value = "主键id", name = "pageNum", dataType="Long")
    private Long pageNum;

    @ApiModelProperty(value = "用于排序的字段", name = "orderByFields", dataType="String")
    private String orderByFields;

    @ApiModelProperty(value = "排序的规则（当orderByFields不为空，默认asc升序）", name = "orderRules", dataType="String")
    private String orderRules;

    @ApiModelProperty(value = "条件（比如like，等于，大于等）", name = "condition", dataType="String")
    private String condition;

}
