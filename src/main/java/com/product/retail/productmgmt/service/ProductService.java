package com.product.retail.productmgmt.service;

import com.product.retail.productmgmt.exceptions.ApprovalQueueNotFoundException;
import com.product.retail.productmgmt.exceptions.ProductNotFoundException;
import com.product.retail.productmgmt.model.ProductStatus;
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
        return productRepository.retrieveAllActiveProducts(ApprovalStatus.ACTIVE.name());
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

       Product newProduct = productRepository.saveAndFlush(product);

       updateProductStatus(newProduct);

        return newProduct;
    }

    private void updateProductStatus(Product product) {
         ProductStatus productStatus = new ProductStatus();
        productStatus.setRequestDate(Calendar.getInstance().getTime());
        if (product.getPrice().longValue() > 10000 ) {
            productStatus.setStatus(ApprovalStatus.PENDING_APPROVAL.name());
        }else {
            productStatus.setStatus(ApprovalStatus.ACTIVE.name());
        }
        product.setStatus(productStatus);
        productStatus.setProductId(product.getProductId());
        product.setStatus(productStatus);
        productRepository.save(product);

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
        Product existingProduct = productRepository.findById(product.getProductId()).orElseThrow(() -> new ProductNotFoundException(product.getProductId()));
        ProductStatus productStatus = new ProductStatus(product.getProductId(), ApprovalStatus.PENDING_APPROVAL.name(), Calendar.getInstance().getTime());
        // Validate the product price change
        if (product.getPrice().compareTo(existingProduct.getPrice().multiply(BigDecimal.valueOf(1.5))) > 0) {
            productStatus.setStatus(ApprovalStatus.PENDING_APPROVAL.name());

        }else {
            productStatus.setStatus(ApprovalStatus.ACTIVE.name());
        }
        product.setStatus(productStatus);

       return productRepository.save(product);
    }

    public void deleteProduct(Long productId) throws ProductNotFoundException {
        Product existingProduct = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        // Push the product to the approval queue
        ProductStatus productStatus = new ProductStatus(productId, ApprovalStatus.DELETED.name(), Calendar.getInstance().getTime());
        approvalQueueRepository.save(productStatus);

        // Delete the product from the database
        productRepository.deleteById(productId);
    }

    public List<ProductStatus> getProductApprovalQueue() {
        return approvalQueueRepository.findAllByStatusOrderByRequestDateDesc(ApprovalStatus.PENDING_APPROVAL.name());
    }

    public void approveProduct(Long approvalId) throws ProductNotFoundException, ApprovalQueueNotFoundException {
        // Get the approval queue record
        ProductStatus productStatus = approvalQueueRepository.findById(approvalId).orElseThrow(() -> new ApprovalQueueNotFoundException(approvalId));
        productStatus.setRequestDate(Calendar.getInstance().getTime());
        productStatus.setStatus(ApprovalStatus.ACTIVE.name());
        approvalQueueRepository.save(productStatus);

    }

    /**
     * update the status of product in approval queue as reject
     *
     * @param approvalId
     * @throws ApprovalQueueNotFoundException
     */
    public void rejectProduct(Long approvalId) throws ApprovalQueueNotFoundException {
        ProductStatus productStatus = approvalQueueRepository.findById(approvalId).orElseThrow(() -> new ApprovalQueueNotFoundException(approvalId));
        productStatus.setStatus(ApprovalStatus.REJECT.name());
        productStatus.setRequestDate(Calendar.getInstance().getTime());
        approvalQueueRepository.save(productStatus);
    }
}
