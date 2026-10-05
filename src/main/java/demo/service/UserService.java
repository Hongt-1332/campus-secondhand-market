package demo.service;

import demo.common.PageResult;
import demo.entity.User;
import demo.mapper.UserMapper;
import demo.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    // 自定义管理员密码，可自行修改
    private static final String ADMIN_PWD = "123456";

    /**
     * 登录：新增 adminPwd 参数，校验管理员身份（保留你原有逻辑不变）
     */
    public User login(String username, String password, String adminPwd) {
        User user = userMapper.getByUsername(username);
        if (user == null) {
            return null;
        }
        // 校验普通账号密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        // 输入了管理员密码且匹配成功 → 标记为管理员
        if(adminPwd != null && ADMIN_PWD.equals(adminPwd)){
            user.setIsAdmin(1);
        }else{
            user.setIsAdmin(0);
        }
        user.setPassword(null);
        return user;
    }

    /**
     * 注册（默认普通用户，原有逻辑不变）
     */
    public int register(User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return -2; // 参数不完整
        }
        User exist = userMapper.getByUsername(user.getUsername());
        if (exist != null) {
            return -1;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsAdmin(0); // 注册默认普通用户
        // 如果昵称未填写，默认使用用户名
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            user.setNickname(user.getUsername());
        }
        return userMapper.insert(user);
    }

    // ===================== 新增 管理员管理用户的4个方法 =====================
    /**
     * 用户列表分页模糊查询
     * @param username 搜索用户名，传null查询全部
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     */
    public PageResult<User> list(String username, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        int offset = (pageNum - 1) * pageSize;
        List<User> list = userMapper.selectPage(username, offset, pageSize);
        Long total = userMapper.countTotal(username);
        return new PageResult<>(list, total, pageNum, pageSize);
    }

    /**
     * 根据用户ID查询单个用户（修复getById找不到符号报错）
     */
    public User getById(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 管理员新增用户（自动加密密码）
     */
    public int add(User user) {
        // 判断用户名是否重复
        User exist = userMapper.getByUsername(user.getUsername());
        if(exist != null){
            return -1; // 用户名已存在
        }
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.insert(user);
    }

    /**
     * 管理员修改用户信息（不修改密码，仅更新用户名、角色）
     */
    public int update(User user) {
        // 编辑时前端不传密码，不更新密码字段
        return userMapper.updateById(user);
    }

    /**
     * 更新个人资料（昵称、联系方式、宿舍地址）
     * 同步更新该用户所有商品的卖家昵称
     */
    public void updateProfile(User user) {
        System.out.println("更新用户资料，用户ID=" + user.getId() + ", nickname=" + user.getNickname());
        userMapper.updateProfile(user);
        // 如果修改了昵称，同步更新商品表中的卖家名称
        if (user.getNickname() != null && !user.getNickname().isEmpty()) {
            int rows = productMapper.updateSellerByUserId(user.getId(), user.getNickname());
            System.out.println("同步更新商品卖家昵称，影响行数=" + rows);
        } else {
            System.out.println("nickname为空，跳过同步更新商品");
        }
    }

    /**
     * 根据id删除用户
     */
    public int deleteById(Integer id) {
        return userMapper.deleteById(id);
    }
}