package com.product.retail.productmgmt.service;

import com.product.retail.productmgmt.exceptions.ProductNotFoundException;
import com.product.retail.productmgmt.model.ApprovalStatus;
import com.product.retail.productmgmt.model.Product;
import com.product.retail.productmgmt.model.ProductStatus;
import com.product.retail.productmgmt.repo.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    public void shouldCreateProductSuccessfullyAndMarkItAsActive() {
        // Create a test product object.
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("20120.2"));

        // Mock the behavior of the ProductRepository.
        when(productRepository.saveAndFlush(product)).thenReturn(product);

        // Call the createProduct() method.
        Product createdProduct = productService.createProduct(product);

        // Assert that the product's status is set to PENDING_APPROVAL as price is greater then 10000
        assertEquals("PENDING_APPROVAL", createdProduct.getStatus().getStatus());
    }

    @Test
    public void shouldUpdateProductStatusToPendingApprovalWhenPriceIncreaseIsMoreThan50Percent() throws ProductNotFoundException {
        // Given
        Product existingProduct = getMockProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        when(productRepository.save(productCaptor.capture())).thenReturn(productCaptor.capture());

        Product updatedProduct = getMockProduct();
        updatedProduct.setPrice(new BigDecimal("151"));

        productService.updateProduct(updatedProduct);

        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getStatus().getStatus()).isEqualTo(ApprovalStatus.PENDING_APPROVAL.name());
    }

    @Test
    public void shouldUpdateProductStatusToActiveWhenPriceIncreaseIsLessThan50Percent() throws ProductNotFoundException {
        // Given
        Product existingProduct = getMockProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        when(productRepository.save(productCaptor.capture())).thenReturn(productCaptor.capture());

        Product updatedProduct = getMockProduct();
        updatedProduct.setPrice(new BigDecimal("120"));

        productService.updateProduct(updatedProduct);
        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getStatus().getStatus()).isEqualTo(ApprovalStatus.ACTIVE.name());
    }


    private Product getMockProduct() {
        Product existingProduct = new Product();
        existingProduct.setProductId(1L);
        existingProduct.setName("Product 1");
        existingProduct.setPrice(new BigDecimal("100"));
        ProductStatus productStatus = new ProductStatus();
        productStatus.setProductId(existingProduct.getProductId());
        productStatus.setRequestDate(Calendar.getInstance().getTime());
        productStatus.setStatus(ApprovalStatus.ACTIVE.name());
        existingProduct.setStatus(productStatus);
        return existingProduct;
    }
}
