package com.tzp.LifeCycle.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tzp.LifeCycle.dto.DataBaseQueryDto;
import com.tzp.LifeCycle.entity.LifeTest;
import com.tzp.LifeCycle.mapper.LifeTestMapper;
import com.tzp.LifeCycle.service.LifeTestService;
import com.tzp.LifeCycle.util.LifeStringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

/**
 * @author kangxvdong
 */

@Slf4j
@Service
public class LifeTestServiceImpl implements LifeTestService {

    @Autowired
    private LifeTestMapper dataBaseMapper;

    /**
     * 根据id查询数据
     *
     * @param id 要查询对象的id
     * @return 返回查询的数据
     */
    @Override
    public LifeTest queryById(Serializable id) {
        return dataBaseMapper.selectById(id);
    }

    /**
     * 根据字段条件模糊查询返回分页查询结果
     *
     * @param dto 要查询的查询对象类
     * @return 返回查询的数据集合
     */
    @Override
    public Page<LifeTest> queryList(DataBaseQueryDto dto) {
        Page<LifeTest> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        QueryWrapper<LifeTest> wrapper = new QueryWrapper<>(null);
        // 拼接显示字段的方法
        wrapper.select("distinct *");
        if ( dto.getQueryAllEqualFields() != null && !dto.getQueryAllEqualFields().isEmpty() ) {
            wrapper.allEq(dto.getQueryAllEqualFields());
        }
        return dataBaseMapper.selectPage(page, wrapper);
    }

    /**
     * 添加一条数据
     *
     * @param t 创建使用的对象数据
     * @return 返回成功了几条数据
     */
    @Override
    public Integer createOne(LifeTest t) {
        return dataBaseMapper.insert(t);
    }

    /**
     * 批量添加数据
     *
     * @param list 创建使用的对象数据
     * @return 返回成功了几条数据
     */
    @Override
    public Integer createByList(List<LifeTest> list) {
        return null;
    }

    /**
     * 根据对象删除一条数据
     *
     * @param t 要删除的对象
     * @return 返回删除数据的行数
     */
    @Override
    public Integer deleteOne(LifeTest t) {
        return dataBaseMapper.deleteById(t);
    }

    /**
     * 根据id修改一条数据
     *
     * @param t 准备好修改数据的对象
     * @return 返回修改数据的行数
     */
    @Override
    public Integer updateOne(LifeTest t) {
        return dataBaseMapper.updateById(t);
    }

}
