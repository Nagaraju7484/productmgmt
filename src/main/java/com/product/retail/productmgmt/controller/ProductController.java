package com.product.retail.productmgmt.controller;

import com.product.retail.productmgmt.exceptions.ApprovalQueueNotFoundException;
import com.product.retail.productmgmt.exceptions.ProductNotFoundException;
import com.product.retail.productmgmt.model.ProductStatus;
import com.product.retail.productmgmt.model.Product;
import com.product.retail.productmgmt.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get the list of active products sorted by the latest first.
     *
     * @return List<Product> - active product list
     */
    @GetMapping
    public List<Product> listActiveProducts() {
        return productService.listActiveProducts();
    }

    @GetMapping("/search")
    public List<Product> searchProducts(
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "minPostedDate", required = false) Date minPostedDate,
            @RequestParam(value = "maxPostedDate", required = false) Date maxPostedDate
    ) {
        return productService.searchProducts(productName, minPrice, maxPrice, minPostedDate, maxPostedDate);
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @PutMapping("/{productId}")
    public Product updateProduct(@PathVariable Long productId, @RequestBody Product product) throws ProductNotFoundException {
        product.setProductId(productId);
        return productService.updateProduct(product);
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable Long productId) throws ProductNotFoundException {
        productService.deleteProduct(productId);
    }

    @GetMapping("/approval-queue")
    public List<ProductStatus> getProductApprovalQueue() {
        return productService.getProductApprovalQueue();
    }

    @PutMapping("/approval-queue/{approvalId}/approve")
    public void approveProduct(@PathVariable Long approvalId) throws ProductNotFoundException, ApprovalQueueNotFoundException {
        productService.approveProduct(approvalId);
    }

    @PutMapping("/approval-queue/{approvalId}/reject")
    public void rejectProduct(@PathVariable Long approvalId) throws ApprovalQueueNotFoundException {
        productService.rejectProduct(approvalId);
    }
}
