package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //开始分页
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新增套餐，包含套餐菜品关系数据
     * @param setmealDTO
     */
    @Transactional(rollbackFor = Exception.class)//开启事务，异常回滚
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        //属性拷贝
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //向套餐表插入一条数据
        setmealMapper.insert(setmeal);
        //获取套餐id
        Long setmealId = setmeal.getId();
        //向套餐菜品关系表插入多条数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && setmealDishes.size() > 0){
            //遍历套餐菜品数据，设置套餐id
            setmealDishes.forEach(sd -> {
                sd.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Transactional(rollbackFor = Exception.class)//开启事务，异常回滚
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断套餐是否起售
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //删除套餐数据
        setmealMapper.deleteByIds(ids);
        //删除套餐菜品关系数据
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    /**
     * 根据id查询套餐详情，包含菜品数据
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        //属性拷贝
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        //查询套餐菜品数据
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        //设置套餐菜品数据
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 修改套餐，包含套餐菜品关系数据
     * @param setmealDTO
     */
    @Transactional(rollbackFor = Exception.class)//开启事务，异常回滚
    @Override
    public void updateWithDish(SetmealDTO setmealDTO) {
        //属性拷贝
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //修改套餐数据
        setmealMapper.update(setmeal);
        //删除套餐菜品关系数据
        setmealDishMapper.deleteBySetmealIds(Collections.singletonList(setmealDTO.getId()));
        //重新插入套餐菜品关系数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(sd -> {
                sd.setSetmealId(setmealDTO.getId());
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐起售停售
     * @param id
     * @param status
     */
    @Override
    public void startOrStop(Long id, Integer status) {
        //起售套餐时，检查套餐内包含的菜品是否都已启售
        if (StatusConstant.ENABLE.equals(status)) {
            List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
            for (SetmealDish sd : setmealDishes) {
                Dish dish = dishMapper.getById(sd.getDishId());
                if (StatusConstant.DISABLE.equals(dish.getStatus())) {
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
    }
}