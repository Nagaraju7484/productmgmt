package com.product.retail.productmgmt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String name;
    private BigDecimal price;
    @DateTimeFormat(pattern = "MM.dd.yyyy HH.mm.ss")
    private Date postedDate;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "productId")
    private ProductStatus status;
}
