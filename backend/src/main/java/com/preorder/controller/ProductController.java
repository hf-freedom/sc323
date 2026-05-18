package com.preorder.controller;

import com.preorder.common.Result;
import com.preorder.entity.PreorderProduct;
import com.preorder.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Result<List<PreorderProduct>> getProducts(@RequestParam(required = false) Boolean active) {
        if (Boolean.TRUE.equals(active)) {
            return Result.success(productService.getActiveProducts());
        }
        return Result.success(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public Result<PreorderProduct> getProduct(@PathVariable Long id) {
        PreorderProduct product = productService.getProduct(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    @PostMapping
    public Result<PreorderProduct> createProduct(@RequestBody PreorderProduct product) {
        try {
            PreorderProduct created = productService.createProduct(product);
            return Result.success("创建成功", created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<PreorderProduct> updateProduct(@PathVariable Long id, @RequestBody PreorderProduct product) {
        try {
            PreorderProduct updated = productService.updateProduct(id, product);
            return Result.success("更新成功", updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
