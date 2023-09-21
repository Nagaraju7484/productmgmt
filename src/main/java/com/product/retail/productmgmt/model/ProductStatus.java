package com.product.retail.productmgmt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
    public class ProductStatus {
    @Id
    private Long productId;
    private String status;
    @DateTimeFormat(pattern = "MM.dd.yyyy HH.mm.ss")
    private Date requestDate;
}
