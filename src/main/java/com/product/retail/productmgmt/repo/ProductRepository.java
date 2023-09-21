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

    List<Product> findAllByStatusOrderByPostedDateDesc(ApprovalStatus status);

    List<Product> findAll(Specification<Product> spec);

    @Query(value = "SELECT p from Product p where p.name=:productName" +
            " and p.postedDate between :minPostedDate and :maxPostedDate " +
            "and p.price > :minPrice and p.price < :maxPrice")
    List<Product> retrieveSearch(
            String productName,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Date minPostedDate,
            Date maxPostedDate
    );
}
