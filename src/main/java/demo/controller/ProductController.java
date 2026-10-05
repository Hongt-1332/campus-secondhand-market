package demo.controller;

import com.alibaba.fastjson2.JSON;
import demo.common.Result;
import demo.entity.Product;
import demo.entity.User;
import demo.service.ProductService;
import demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;

    // 统一获取当前登录用户，null表示未登录/登录信息无效
    private User getCurrentUser(HttpServletRequest request) {
        String userJson = request.getHeader("loginUser");
        if (userJson == null || userJson.isEmpty()) {
            return null;
        }
        try {
            return JSON.parseObject(userJson, User.class);
        } catch (Exception e) {
            return null;
        }
    }

    // 统一鉴权：是否为管理员
    private boolean checkIsAdmin(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null && user.getIsAdmin() != null && user.getIsAdmin() == 1;
    }

    // 所有人可查询商品列表
    @GetMapping("/list")
    public Result<List<Product>> list(@RequestParam(required = false) String name) {
        return Result.success(productService.list(name));
    }

    // 所有人可新增商品：强制后端赋值卖家信息，屏蔽前端传入值
    @PostMapping
    public Result<Integer> add(HttpServletRequest request, @RequestBody Product product) {
        User loginUser = getCurrentUser(request);
        if (loginUser == null) {
            return Result.error("请先登录后再发布商品");
        }
        // 校验配送方式
        if (product.getDeliveryType() == null || (product.getDeliveryType() != 1 && product.getDeliveryType() != 2)) {
            return Result.error("请选择配送方式：1-自取，2-送到宿舍楼下");
        }
        // 查询完整用户信息，绑定卖家昵称、联系方式和发布者ID
        User currentUser = userService.getById(loginUser.getId());
        product.setSeller(currentUser.getNickname());
        product.setContact(currentUser.getContact());
        product.setUserId(currentUser.getId());

        int row = productService.add(product);
        return row > 0 ? Result.success(product.getId()) : Result.error("发布商品失败");
    }

    // 权限规则：管理员可编辑所有商品，普通用户仅能编辑自己发布的商品
    @PutMapping
    public Result<String> update(HttpServletRequest request, @RequestBody Product product) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return Result.error("请先登录");
        }

        // 先查询原商品信息
        Product oldProduct = productService.getById(product.getId());
        if (oldProduct == null) {
            return Result.error("商品不存在");
        }

        boolean isAdmin = checkIsAdmin(request);
        boolean isOwner = oldProduct.getUserId() != null
                && oldProduct.getUserId().equals(currentUser.getId());

        // 既不是管理员也不是商品发布者，拒绝操作
        if (!isAdmin && !isOwner) {
            return Result.error("权限不足：仅可编辑自己发布的商品");
        }

        // 普通用户修改时，强制保留原归属信息，防止前端篡改卖家、发布者ID
        if (!isAdmin) {
            product.setUserId(oldProduct.getUserId());
            product.setSeller(oldProduct.getSeller());
            product.setContact(oldProduct.getContact());
        }

        int row = productService.update(product);
        return row > 0 ? Result.success("修改成功") : Result.error("修改失败");
    }

    // 权限规则：管理员可删除所有商品，普通用户仅能删除自己发布的商品
    @DeleteMapping("/{id}")
    public Result<String> delete(HttpServletRequest request, @PathVariable Integer id) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return Result.error("请先登录");
        }

        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }

        boolean isAdmin = checkIsAdmin(request);
        boolean isOwner = product.getUserId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            return Result.error("权限不足：仅可删除自己发布的商品");
        }

        int row = productService.delete(id);
        return row > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }
}