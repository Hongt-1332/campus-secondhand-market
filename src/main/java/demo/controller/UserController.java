package demo.controller;

import com.alibaba.fastjson2.JSON;
import demo.common.PageResult;
import demo.common.Result;
import demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 统一管理员鉴权方法
     * 从请求头获取登录用户信息，判断是否为管理员
     */
    private boolean checkIsAdmin(HttpServletRequest request){
        // 前端登录后把userInfo放到请求头loginUser
        String userJson = request.getHeader("loginUser");
        if(userJson == null || userJson.isEmpty()){
            return false;
        }
        try{
            User loginUser = JSON.parseObject(userJson, User.class);
            // isAdmin=1 代表管理员，0普通用户
            return loginUser.getIsAdmin() != null && loginUser.getIsAdmin() == 1;
        }catch (Exception e){
            return false;
        }
    }

    // ========== 管理员专属接口（普通用户全部禁止访问） ==========
    /**
     * 用户列表分页查询
     * 普通用户调用直接返回权限不足，看不到任何数据
     */
    @GetMapping("/list")
    public Result<PageResult<User>> getUserList(HttpServletRequest request,
                                                @RequestParam(required = false) String username,
                                                @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                                @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        // 鉴权拦截：普通用户直接拒绝
        if(!checkIsAdmin(request)){
            return Result.error("无权限，仅管理员可查看用户列表");
        }
        PageResult<User> pageResult = userService.list(username, pageNum, pageSize);
        return Result.success(pageResult);
    }

    /**
     * 新增用户
     */
    /**
     * 自助注册接口，所有人都能访问，不需要管理员权限
     */
    @PostMapping("/register")
    public Result<Integer> register(@RequestBody User user) {
        int res = userService.register(user);
        if (res == -1) {
            return Result.error("用户名已被占用");
        }
        if (res == -2) {
            return Result.error("用户名或密码不能为空");
        }
        return Result.success(1);
    }

    /**
     * 修改用户
     */
    @PutMapping
    public Result<String> updateUser(HttpServletRequest request, @RequestBody User user) {
        if(!checkIsAdmin(request)){
            return Result.error("无权限，仅管理员可修改用户");
        }
        int rows = userService.update(user);
        return rows > 0 ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(HttpServletRequest request, @PathVariable Integer id) {
        if(!checkIsAdmin(request)){
            return Result.error("无权限，仅管理员可删除用户");
        }
        int rows = userService.deleteById(id);
        return rows > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ========== 公共接口（所有用户可用：登录，无需鉴权） ==========
    /**
     * 登录接口，不做管理员限制
     */
    /**
     * 登录接口，兼容管理员密码参数
     */
    @PostMapping("/login")
    public Result<User> login(
            @RequestBody User loginParam
    ) {
        // 前端把adminPwd放在请求体里，不用@RequestParam
        String adminPwd = loginParam.getAdminPwd();
        User loginUser = userService.login(loginParam.getUsername(), loginParam.getPassword(), adminPwd);
        if(loginUser == null){
            return Result.error("用户名或密码错误");
        }
        return Result.success(loginUser);
    }
    /**
     * 获取当前登录用户个人资料
     */
    @GetMapping("/profile")
    public Result<User> getProfile(HttpServletRequest request) {
        String userJson = request.getHeader("loginUser");
        User loginUser = JSON.parseObject(userJson, User.class);
        User user = userService.getById(loginUser.getId());
        // 密码清空不返回前端
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 更新个人资料（昵称、联系方式、宿舍地址）
     */
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody User user, HttpServletRequest request) {
        String userJson = request.getHeader("loginUser");
        User loginUser = JSON.parseObject(userJson, User.class);
        // 只能修改自己的资料，强制ID为当前登录用户
        user.setId(loginUser.getId());
        userService.updateProfile(user);
        return Result.success("更新成功");
    }
}