package com.tzp.LifeCycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author kangxvdong
 */

@Data
@TableName("life_cycle_test")
public class LifeTest implements Serializable {

    @TableId(value = "test_id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id", name = "testId", dataType="Integer")
    private Integer testId;

    @TableField(value = "user_name")
    @ApiModelProperty(value = "用户名", name = "userName", dataType="String", required=true)
    private String userName;

    @TableField(value = "synopsis")
    @ApiModelProperty(value = "简介", name = "synopsis", required=true)
    private String synopsis;

}
