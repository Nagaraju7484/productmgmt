package com.product.retail.productmgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.product.retail.productmgmt.model.ApprovalStatus;
import com.product.retail.productmgmt.model.Product;
import com.product.retail.productmgmt.model.ProductStatus;
import com.product.retail.productmgmt.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Test
    public void shouldCreateProductSuccessfully() throws Exception {
        // Create a test product object.
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(BigDecimal.TEN);

        // Create a mock HTTP request.
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(product));

        // Perform the request and get the response.
        MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

        // Assert that the response status code is 201 Created.
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        // Assert that the response body contains the created product.
        Product createdProduct = new ObjectMapper().readValue(response.getContentAsString(), Product.class);
        assertEquals(product.getName(), createdProduct.getName());
        assertEquals(ApprovalStatus.ACTIVE.name(), createdProduct.getStatus().getStatus());
    }

}
