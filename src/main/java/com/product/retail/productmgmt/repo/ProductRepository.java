package com.product.retail.productmgmt.repo;

import com.product.retail.productmgmt.model.ApprovalStatus;
import com.product.retail.productmgmt.model.Product;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     *
     * @param status
     * @return
     */
    @Query(value = "select p from Product p, ProductStatus s where p.productId = s.productId and s.status=:status")
    List<Product> retrieveAllActiveProducts(String status);

    List<Product> findAll(Specification<Product> spec);
}
