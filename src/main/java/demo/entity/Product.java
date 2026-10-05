package demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Product {
    private Integer id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String seller;
    private String contact;
    private Integer status;
    private String image;
    // 新增：发布者用户ID，用于后端强制匹配当前登录用户昵称
    private Integer userId;
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    // 配送方式：1-自取，2-送到宿舍楼下
    private Integer deliveryType;
    public Integer getDeliveryType() {
        return deliveryType;
    }
    public void setDeliveryType(Integer deliveryType) {
        this.deliveryType = deliveryType;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}