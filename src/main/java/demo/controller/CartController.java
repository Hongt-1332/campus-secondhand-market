package demo.controller;

import com.alibaba.fastjson2.JSON;
import demo.common.Result;
import demo.entity.Cart;
import demo.service.CartService;
import demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    // 获取登录用户ID
    private Integer getLoginUserId(HttpServletRequest request){
        String userJson = request.getHeader("loginUser");
        // 增加判空，未登录直接抛出提示，不进JSON解析报错
        if(userJson == null || userJson.trim().isEmpty()){
            return null;
        }
        try {
            User user = JSON.parseObject(userJson,User.class);
            return user.getId();
        }catch (Exception e){
            return null;
        }
    }

    // 添加商品到购物车
    @PostMapping("/add/{productId}")
    public Result<String> addCart(HttpServletRequest request, @PathVariable Integer productId){
        Integer userId = getLoginUserId(request);
        cartService.addCart(userId,productId);
        return Result.success("加入购物车成功");
    }

    // 获取购物车列表
    @GetMapping("/list")
    public Result<List<Cart>> cartList(HttpServletRequest request){
        Integer userId = getLoginUserId(request);
        List<Cart> list = cartService.getCartList(userId);
        return Result.success(list);
    }

    // 修改购物车商品数量
    @PutMapping("/num")
    public Result<String> updateNum(@RequestParam Integer id,@RequestParam Integer num){
        if(num < 1) return Result.error("数量不能小于1");
        cartService.updateCartNum(id,num);
        return Result.success("修改成功");
    }

    // 删除购物车单项
    @DeleteMapping("/{id}")
    public Result<String> deleteCart(@PathVariable Integer id){
        cartService.deleteCart(id);
        return Result.success("已移除");
    }

    // 清空全部购物车
    @DeleteMapping("/clear")
    public Result<String> clearCart(HttpServletRequest request){
        Integer userId = getLoginUserId(request);
        cartService.clearCart(userId);
        return Result.success("购物车已清空");
    }
}