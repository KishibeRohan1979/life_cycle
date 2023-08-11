package com.tzp.LifeCycle.dto;


import com.tzp.LifeCycle.entity.LifeCycleTactics;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 创建ES文档时使用的Dto类
 *
 * @author kangxvdong
 */
@Data
public class EsDto {

    @ApiModelProperty(value = "索引名称", name = "indexName")
    private String indexName;

    @ApiModelProperty(value = "数据列表", name = "dataList")
    private List<Map<String, Object>> dataList;

    @ApiModelProperty(value = "策略类，创建的时候不要添加这个属性", name = "lifeCycleTactics", dataType="LifeCycleTactics")
    private LifeCycleTactics lifeCycleTactics;

}
