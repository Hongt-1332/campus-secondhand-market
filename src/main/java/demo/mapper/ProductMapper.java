package demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import demo.entity.Product;
import java.util.List;

@Mapper
public interface ProductMapper {
    // 列表查询（支持名称模糊搜索）
    List<Product> list(String name);

    // 根据ID查询
    Product getById(Integer id);

    // 新增商品
    int insert(Product product);

    // 修改商品
    int update(Product product);

    // 删除商品
    int deleteById(Integer id);

    // 新增：查询所有上架在售商品，给智能客服读取商品清单
    List<Product> selectOnSaleList();

    // 根据用户ID更新所有商品的卖家昵称
    int updateSellerByUserId(@Param("userId") Integer userId, @Param("seller") String seller);
}