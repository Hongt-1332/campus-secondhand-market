package demo.service;

import demo.entity.Product;
import demo.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public List<Product> list(String name) {
        return productMapper.list(name);
    }

    public Product getById(Integer id) {
        return productMapper.getById(id);
    }

    public int add(Product product) {
        return productMapper.insert(product);
    }

    public int update(Product product) {
        return productMapper.update(product);
    }

    public int delete(Integer id) {
        return productMapper.deleteById(id);
    }
}