package com.product.retail.productmgmt.service;

import com.product.retail.productmgmt.exceptions.ApprovalQueueNotFoundException;
import com.product.retail.productmgmt.exceptions.ProductNotFoundException;
import com.product.retail.productmgmt.model.ApprovalQueue;
import com.product.retail.productmgmt.model.ApprovalStatus;
import com.product.retail.productmgmt.model.Product;
import com.product.retail.productmgmt.repo.ApprovalQueueRepository;
import com.product.retail.productmgmt.repo.ProductRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    private final ApprovalQueueRepository approvalQueueRepository;

    public ProductService(ProductRepository productRepository, ApprovalQueueRepository approvalQueueRepository) {
        this.productRepository = productRepository;
        this.approvalQueueRepository = approvalQueueRepository;
    }

    public List<Product> listActiveProducts() {
        return productRepository.findAllByStatusOrderByPostedDateDesc(ApprovalStatus.ACTIVE);
    }

    public List<Product> searchProducts(String productName, BigDecimal minPrice, BigDecimal maxPrice, Date minPostedDate, Date maxPostedDate) {
        Specification<Product> spec = filterProducts(
                productName, minPrice, maxPrice, minPostedDate, maxPostedDate);

        return productRepository.findAll(spec);
    }

    public static Specification<Product> filterProducts(
            String productName,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Date minPostedDate,
            Date maxPostedDate) {

        return (Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Add predicates for optional parameters
            if (productName != null && !productName.isEmpty()) {
                predicates.add(builder.like(root.get("name"), "%" + productName + "%"));
            }

            if (minPrice != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (minPostedDate != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("postedDate"), minPostedDate));
            }

            if (maxPostedDate != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("postedDate"), maxPostedDate));
            }

            // Combine predicates using AND operator
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Creates product
     *
     * @param product - input
     * @return Product - newly created with id
     */
    public Product createProduct(Product product) {
        // verify the product price if it exceeds 10000$ amount
        if (product.getPrice().compareTo(BigDecimal.valueOf(10000)) > 0) {
            // Push the product to the approval queue
            ApprovalQueue approvalQueue = new ApprovalQueue(product.getId(), ApprovalStatus.ACTIVE, Calendar.getInstance().getTime());
            approvalQueueRepository.save(approvalQueue);

            // Return the product with a status of "PENDING_APPROVAL"
            product.setStatus(ApprovalStatus.PENDING_APPROVAL);
            return product;
        }

        // Save the product to the database
        return productRepository.save(product);
    }

    /**
     * update product
     *
     * @param product
     * @return
     * @throws ProductNotFoundException
     */
    public Product updateProduct(Product product) throws ProductNotFoundException {
        // Get the existing product from the database
        Product existingProduct = productRepository.findById(product.getId()).orElseThrow(() -> new ProductNotFoundException(product.getId()));

        // Validate the product price change
        if (product.getPrice().compareTo(existingProduct.getPrice().multiply(BigDecimal.valueOf(1.5))) > 0) {
            // Push the product to the approval queue
            ApprovalQueue approvalQueue = new ApprovalQueue(product.getId(), ApprovalStatus.ACTIVE, Calendar.getInstance().getTime());
            approvalQueueRepository.save(approvalQueue);
        }else{
            // Return the product with a status of "PENDING_APPROVAL"
            product.setStatus(ApprovalStatus.PENDING_APPROVAL);
            // Update the product in the database
            productRepository.save(product);
            return product;
        }


        return product;
    }

    public void deleteProduct(Long productId) {
        // Push the product to the approval queue
        ApprovalQueue approvalQueue = new ApprovalQueue(productId, ApprovalStatus.PENDING_APPROVAL, Calendar.getInstance().getTime());
        approvalQueueRepository.save(approvalQueue);

        // Delete the product from the database
        productRepository.deleteById(productId);
    }

    public List<ApprovalQueue> getProductApprovalQueue() {
        return approvalQueueRepository.findAllByStatusOrderByRequestDateAsc(ApprovalStatus.PENDING_APPROVAL);
    }

    public void approveProduct(Long approvalId) throws ProductNotFoundException, ApprovalQueueNotFoundException {
        // Get the approval queue record
        ApprovalQueue approvalQueue = approvalQueueRepository.findById(approvalId).orElseThrow(() -> new ApprovalQueueNotFoundException(approvalId));

        // Get the product from the database
        Product product = productRepository.findById(approvalQueue.getProductId()).orElseThrow(() -> new ProductNotFoundException(approvalQueue.getProductId()));

        // Update the product status
        product.setStatus(ApprovalStatus.ACTIVE);
        productRepository.save(product);

        // Delete the approval queue record
        approvalQueueRepository.deleteById(approvalId);
    }

    /**
     * update the status of product in approval queue as reject
     *
     * @param approvalId
     * @throws ApprovalQueueNotFoundException
     */
    public void rejectProduct(Long approvalId) throws ApprovalQueueNotFoundException {
        ApprovalQueue approvalQueue = approvalQueueRepository.findById(approvalId).orElseThrow(() -> new ApprovalQueueNotFoundException(approvalId));
        approvalQueue.setStatus(ApprovalStatus.REJECT);
        approvalQueueRepository.save(approvalQueue);
    }
}
