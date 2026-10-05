package demo.service;
import demo.entity.Cart;
import demo.mapper.CartMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CartService {
    @Autowired
    private CartMapper cartMapper;

    // 添加商品到购物车
    public void addCart(Integer userId, Integer productId){
        cartMapper.insertOrUpdate(userId,productId);
    }

    // 查询用户购物车
    public List<Cart> getCartList(Integer userId){
        return cartMapper.selectByUserId(userId);
    }

    // 修改数量
    public void updateCartNum(Integer id,Integer num){
        cartMapper.updateNum(id,num);
    }

    // 删除购物车项
    public void deleteCart(Integer id){
        cartMapper.deleteById(id);
    }

    // 清空购物车
    public void clearCart(Integer userId){
        cartMapper.deleteByUserId(userId);
    }
}