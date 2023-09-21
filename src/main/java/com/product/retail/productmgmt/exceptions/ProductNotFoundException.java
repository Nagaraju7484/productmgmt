package com.product.retail.productmgmt.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;


@Slf4j
public class ProductNotFoundException extends Throwable {
    public ProductNotFoundException(Throwable t){
        super(t);
    }

    public ProductNotFoundException(Long productId) {
        log.error("Product Not Found : {} ", productId);
    }
}
