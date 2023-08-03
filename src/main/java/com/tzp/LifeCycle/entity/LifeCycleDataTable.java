package com.tzp.LifeCycle.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author kangxvdong
 */
@Data
@TableName("life_cycle_data_table")
public class LifeCycleDataTable implements Serializable {

    @TableId(value = "index_name")
    @ApiModelProperty(value = "此表主键id，存储的信息是动态、可自定义表单的表名（索引名）", name = "indexName", dataType="String")
    private String indexName;

    @TableField(value = "all_fields")
    @ApiModelProperty(value = "字段字典<字段，字段类型>", name = "allFields", dataType="Object")
    private Object allFields;

    @TableField(value = "primary_key_name")
    @ApiModelProperty(value = "主键是哪个字段", name = "primaryKeyName", dataType="String")
    private String primaryKeyName;

}
