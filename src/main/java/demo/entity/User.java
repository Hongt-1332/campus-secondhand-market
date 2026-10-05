package demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String contact;
    // 是否管理员 1=管理员 0=普通用户
    private Integer isAdmin;
    // 个人昵称
    private String nickname;
    // 联系方式（手机号/微信）
    private String address;
    // 宿舍地址
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    // 临时接收前端管理员密码，不存数据库
    private String adminPwd;

    public String getAdminPwd() {
        return adminPwd;
    }
    // getter/setter
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getContact() {
        return contact;
    }
    public void setContact(String contact) {
        this.contact = contact;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setAdminPwd(String adminPwd) {
        this.adminPwd = adminPwd;
    }
}