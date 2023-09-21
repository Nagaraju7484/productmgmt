package com.product.retail.productmgmt.repo;

import com.product.retail.productmgmt.model.ApprovalQueue;
import com.product.retail.productmgmt.model.ApprovalStatus;
import com.product.retail.productmgmt.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalQueueRepository extends JpaRepository<ApprovalQueue, Long> {
    List<ApprovalQueue> findAllByStatusOrderByPostedDateDesc(ApprovalStatus status);
}
