package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id列表查询套餐id列表
     * @param dishIds
     * @return
     */

    List<Long> getSetmealByDishIds(List<Long> dishIds);
}