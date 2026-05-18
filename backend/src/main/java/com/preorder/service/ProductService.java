package com.preorder.service;

import com.preorder.entity.PreorderProduct;
import com.preorder.store.InMemoryStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    public List<PreorderProduct> getAllProducts() {
        return InMemoryStore.getAllProducts();
    }

    public List<PreorderProduct> getActiveProducts() {
        return InMemoryStore.getAllProducts().stream()
                .filter(p -> p.getActive())
                .collect(java.util.stream.Collectors.toList());
    }

    public PreorderProduct getProduct(Long id) {
        return InMemoryStore.getProduct(id);
    }

    public PreorderProduct createProduct(PreorderProduct product) {
        product.setId(InMemoryStore.PRODUCT_ID_GENERATOR.getAndIncrement());
        product.setLockedStock(0);
        product.setSoldStock(0);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        InMemoryStore.saveProduct(product);
        return product;
    }

    public PreorderProduct updateProduct(Long id, PreorderProduct product) {
        PreorderProduct existing = InMemoryStore.getProduct(id);
        if (existing == null) {
            throw new RuntimeException("商品不存在");
        }
        product.setId(id);
        product.setLockedStock(existing.getLockedStock());
        product.setSoldStock(existing.getSoldStock());
        product.setCreateTime(existing.getCreateTime());
        product.setUpdateTime(LocalDateTime.now());
        InMemoryStore.saveProduct(product);
        return product;
    }

    public void deleteProduct(Long id) {
        PreorderProduct product = InMemoryStore.getProduct(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        product.setActive(false);
        product.setUpdateTime(LocalDateTime.now());
        InMemoryStore.saveProduct(product);
    }
}
