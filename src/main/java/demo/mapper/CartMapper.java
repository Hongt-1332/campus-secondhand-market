package demo.mapper;
import demo.entity.Cart;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CartMapper {
    // 添加购物车，存在则数量+1，不存在新增
    int insertOrUpdate(@Param("userId") Integer userId, @Param("productId") Integer productId);
    // 查询当前用户购物车列表（关联商品）
    List<Cart> selectByUserId(Integer userId);
    // 修改购物车数量
    int updateNum(@Param("id") Integer id, @Param("num") Integer num);
    // 删除单条购物车
    int deleteById(Integer id);
    // 清空当前用户购物车
    int deleteByUserId(Integer userId);
}