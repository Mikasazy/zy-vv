package com.sky.service.impl;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品，包含口味数据
     *
     * @param dishDTO
     */
    @Transactional(rollbackFor = Exception.class)//开启事务，异常回滚
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        //属性拷贝
        BeanUtils.copyProperties(dishDTO, dish);
        //像菜品表插入一条数据
        dishMapper.insert(dish);
        //获取菜品id
        Long dishId = dish.getId();
        //像菜品口味表插入多条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            //遍历口味数据，设置菜品id
            flavors.forEach(flavor -> {
                flavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 分页查询菜品
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Transactional(rollbackFor = Exception.class)//开启事务，异常回滚
    public void deleteBatch(List<Long> ids) {
        //起售的菜品不能删除 被套餐引用的菜品不能删除 口味数据也要删除
        //判断菜品是否起售
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //起售的菜品不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断菜品是否被套餐引用
        List<Long> setmealIds = setmealDishMapper.getSetmealByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            //被套餐引用的菜品不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品数据
           /*for(Long id : ids){
               dishMapper.deleteById(id);
               //删除菜品口味数据
               dishFlavorMapper.deleteByDishId(id);
           }*/
        //根据菜品id集合批量删除菜品数据
        dishMapper.deleteByIds(ids);
        //根据菜品id集合批量删除菜品口味数据
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询菜品详情，包含口味数据
     *
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        //属性拷贝
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        //查询菜品口味数据
        List<DishFlavor> dishflavors = dishFlavorMapper.getByDishId(id);
        //设置菜品口味数据
        dishVO.setFlavors(dishflavors);
        return dishVO;
    }

    /**
     * 根据id修改菜品，包含口味数据
     *
     * @param dishDTO
     */
    @Transactional(rollbackFor = Exception.class)//开启事务，异常回滚
    public void updateWithFlavor(DishDTO dishDTO) {
        //属性拷贝
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //修改菜品数据
        dishMapper.update(dish);
        //删除菜品口味数据
        dishFlavorMapper.deleteByDishId(dish.getId());
        //重新插入菜品口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            //遍历口味数据，设置菜品id
            flavors.forEach(flavor -> {
                flavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        return dishMapper.list(categoryId);
    }

    /**
     * 菜品起售停售
     * @param id
     * @param status
     */
    @Override
    public void startOrStop(Long id, Integer status) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }
}