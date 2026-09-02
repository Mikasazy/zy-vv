package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    //新增菜品，包含口味数据
    void saveWithFlavor(DishDTO dishDTO);
    //分页查询菜品
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);
    //批量删除菜品
    void deleteBatch(List<Long> ids);
    //根据id查询菜品详情，包含口味数据
    DishVO getByIdWithFlavor(Long id);
    //根据id修改菜品，包含口味数据
    void updateWithFlavor(DishDTO dishDTO);
}
