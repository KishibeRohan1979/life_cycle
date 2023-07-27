package com.tzp.LifeCycle.entity;

import com.tzp.LifeCycle.enums.DataAccessType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ibatis拦截器用到的实体类；
 * 主要是用来送数据时，只能送一个对象，就把要送的数据包装在这里了
 *
 * @author kangxudong
 *
 * @NoArgsConstructor 生成一个无参数的构造方法
 * @AllArgsConstructor 生成一个全参数的构造方法
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JdbcRequestInfo {

    @ApiModelProperty(value = "请求的sql语句", name = "sql")
    private String sql;

    @ApiModelProperty(value = "请求的表名", name = "tableName")
    private String tableName;

    @ApiModelProperty(value = "发送请求的数据（select时可能是空的）", name = "requestObject")
    private Object requestObject;

    @ApiModelProperty(value = "发送请求的类型", name = "jdbcType")
    private DataAccessType jdbcType;

}
