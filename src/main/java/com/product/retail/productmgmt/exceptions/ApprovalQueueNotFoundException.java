package com.product.retail.productmgmt.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApprovalQueueNotFoundException extends Throwable{
    public ApprovalQueueNotFoundException(Throwable t){
        super(t);
    }

    public ApprovalQueueNotFoundException(Long approvalId) {
        log.error("Approval id Not found: {}", approvalId);
    }
}
