package demo.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Cart {
    private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer num;
    private LocalDateTime createTime;
    // 关联商品实体，必须存在
    private Product product;
}