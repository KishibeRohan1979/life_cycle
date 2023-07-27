package com.tzp.LifeCycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 策略类；计划删除，与数据库访问的类
 *
 * @author kangxudong
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("life_cycle_tactics")
public class LifeCycleTactics implements Serializable {

    @TableId(value = "tactics_id", type = IdType.AUTO)
    @ApiModelProperty(value = "策略（该）表主键id", name = "tacticsId", dataType="Long")
    private Long tacticsId;

    @TableField(value = "scheduler_id")
    @ApiModelProperty(value = "定时任务id，该字段是唯一的（约定使用（表名+主键名+主键值）当作定时任务id）", name = "schedulerId")
    private String schedulerId;

    @TableField(value = "tactics_type")
    @ApiModelProperty(value = "策略类型（1、根据使用频率的周期；2、根据固定周期；）", name = "tacticsType", required=true)
    private Integer tacticsType;

    @TableField(value = "create_time")
    @ApiModelProperty(value = "创建时间", name = "createTime")
    private Long createTime;

    @TableField(value = "last_access_time")
    @ApiModelProperty(value = "最后访问时间", name = "lastAccessTime")
    private Long lastAccessTime;

    @TableField(value = "deadline")
    @ApiModelProperty(value = "存储周期（-1表示永久）", name = "deadline", required=true)
    private Long deadline;

    @TableField(value = "execution_time")
    @ApiModelProperty(value = "执行时间", name = "executionTime")
    private Long executionTime;

    @TableField(value = "access_address")
    @ApiModelProperty(value = "访问地址（包括关系型数据库表名、ES索引、文件路径等）", name = "accessAddress")
    private String accessAddress;

    @TableField(value = "detailed_data")
    @ApiModelProperty(value = "某个对象的详细数据（转json）", name = "detailedData")
    private String detailedData;

    @TableField(value = "data_object_type")
    @ApiModelProperty(value = "这个对象的类型（一般都是自定义对象，但是也要写，后续可以转回去）", name = "dataObjectType")
    private String dataObjectType;

    @TableField(value = "primary_key_name")
    @ApiModelProperty(value = "主键（关系型数据库表的主键、es索引的_id、文件的具体文件名）", name = "primaryKeyName")
    private String primaryKeyName;

    @TableField(value = "primary_key_type")
    @ApiModelProperty(value = "主键在（Java）程序中的数据类型", name = "primaryKeyType")
    private String primaryKeyType;

    @TableField(value = "primary_key_value")
    @ApiModelProperty(value = "主键的值", name = "primaryKeyValue")
    private String primaryKeyValue;

    @TableField(value = "data_type")
    @ApiModelProperty(value = "访问数据的类型（数据表、索引、文件夹统一写“list”；具体数据行、文档、文件用“item”表示）", name = "dataType", required=true)
    private String dataType;

    /**
     * 构造方法，只是没有tactics_id
     */
    public LifeCycleTactics(Integer tacticsType, String schedulerId, Long createTime, Long lastAccessTime, Long deadline, Long executionTime, String accessAddress, String detailedData, String dataObjectType, String primaryKeyName, String primaryKeyType, String primaryKeyValue, String dataType) {
        this.tacticsType = tacticsType;
        this.schedulerId = schedulerId;
        this.createTime = createTime;
        this.lastAccessTime = lastAccessTime;
        this.deadline = deadline;
        this.executionTime = executionTime;
        this.accessAddress = accessAddress;
        this.detailedData = detailedData;
        this.dataObjectType = dataObjectType;
        this.primaryKeyName = primaryKeyName;
        this.primaryKeyType = primaryKeyType;
        this.primaryKeyValue = primaryKeyValue;
        this.dataType = dataType;
    }

    /**
     * 构造方法，用在删除转map的时候
     */
    public LifeCycleTactics(String schedulerId, String accessAddress, String primaryKeyName, String primaryKeyValue) {
        this.schedulerId = schedulerId;
        this.accessAddress = accessAddress;
        this.primaryKeyName = primaryKeyName;
        this.primaryKeyValue = primaryKeyValue;
    }
}
