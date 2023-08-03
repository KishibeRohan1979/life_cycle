package com.tzp.LifeCycle.dto;

import com.tzp.LifeCycle.entity.LifeCycleTactics;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 数据库数据更新时的类
 *
 * @author kangxvdong
 */
@Data
public class  DataBaseUpdateDto<T> {

    @ApiModelProperty(value = "需要更新数据的具体引用类", name = "tObject", dataType="Object")
    private T tObject;

    @ApiModelProperty(value = "策略类", name = "lifeCycleTactics", dataType="LifeCycleTactics")
    private LifeCycleTactics lifeCycleTactics;

    /**
     * 查看DataBaseUpdateDto<T>中的这个T具体是引用的什么类
     *
     * @param dto 参数
     * @return XXX.class
     */
    public static Class<?> getClassFromGenericType(DataBaseUpdateDto<?> dto) {
//        Type type = dto.getClass().getGenericSuperclass();
//        if (type instanceof ParameterizedType) {
//            ParameterizedType parameterizedType = (ParameterizedType) type;
//            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
//            if (actualTypeArguments.length > 0) {
//                Type actualType = actualTypeArguments[0];
//                if (actualType instanceof Class) {
//                    return (Class<?>) actualType;
//                }
//            }
//        }
//        throw new IllegalArgumentException("无法根据泛型类型确定类。");
        return dto.getTObject().getClass();
    }

}
