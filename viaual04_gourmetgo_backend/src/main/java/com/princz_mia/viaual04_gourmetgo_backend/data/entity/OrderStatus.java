package com.princz_mia.viaual04_gourmetgo_backend.data.entity;

/**
 * Enum representing the various statuses an order can have
 * during its lifecycle in the GourmetGo application.
 */
public enum OrderStatus {

    // Order has been placed, but not processed yet.
    PENDING,

    // Restaurant has confirmed the order.
    CONFIRMED,

    // Order is currently being prepared by the restaurant.
    PREPARING,

    // Order is ready and waiting to be picked up by the customer or courier.
    READY_FOR_PICKUP,

    // Order is out for delivery to the customer.
    OUT_FOR_DELIVERY,

    // Order has been successfully delivered to the customer.
    DELIVERED,

    // Order has been cancelled either by the restaurant or the customer.
    CANCELLED
}
