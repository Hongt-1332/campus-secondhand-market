package demo.mapper;

import demo.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;

public interface UserMapper {

    // 原有：登录查询用户名
    @Select("select id,username,password,is_admin as isAdmin,address from user where username=#{username}")
    User getByUsername(@Param("username") String username);

    // 原有：注册/新增用户共用插入
    @Insert("insert into user(username,password,is_admin,nickname,contact,address,create_time) values(#{username},#{password},#{isAdmin},#{nickname},#{contact},#{address},NOW())")
    int insert(User user);

    // 模糊查询用户列表（分页）
    @Select("<script>" +
            "select id,username,nickname,contact,address,is_admin as isAdmin,create_time as createTime from user " +
            "<where>" +
            "<if test='username != null and username != \"\"'>" +
            "username like concat('%',#{username},'%')" +
            "</if>" +
            "</where>" +
            "order by id desc " +
            "limit #{offset},#{pageSize}" +
            "</script>")
    List<User> selectPage(@Param("username") String username, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    // 查询总记录数
    @Select("<script>" +
            "select count(*) from user " +
            "<where>" +
            "<if test='username != null and username != \"\"'>" +
            "username like concat('%',#{username},'%')" +
            "</if>" +
            "</where>" +
            "</script>")
    Long countTotal(@Param("username") String username);
    @Select("select id,username,nickname,contact,address,is_admin as isAdmin from user where id = #{id}")
    User selectById(Integer id);
    // 新增2：修改用户（仅更新用户名、角色，不碰密码）
    @Update("update user set username=#{username},is_admin=#{isAdmin} where id=#{id}")
    int updateById(User user);
    @Update("UPDATE user SET nickname = #{nickname}, contact = #{contact}, address = #{address} WHERE id = #{id}")
    int updateProfile(User user);
    // 新增3：根据ID删除用户
    @Delete("delete from user where id=#{id}")
    int deleteById(@Param("id") Integer id);
}