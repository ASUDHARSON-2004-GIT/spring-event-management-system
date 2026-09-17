package com.EventManagementSystem.dto;

import com.EventManagementSystem.model.PaymentMethod;

public class PaymentRequest {

    private PaymentMethod method;

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }
}
