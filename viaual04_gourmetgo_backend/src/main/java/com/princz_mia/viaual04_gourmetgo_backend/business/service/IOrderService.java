package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Order;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.OrderStatus;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.OrderDto;

import java.util.List;
import java.util.UUID;

public interface IOrderService {

    OrderDto getOrder(UUID orderId);

    List<OrderDto> getCustomerOrders(UUID customerId);

    Order placeOrder(Customer cust, OrderDto req);

    OrderDto convertToDto(Order order);

    List<OrderDto> getAllOrders();

    void updateStatus(UUID id, OrderStatus status);

    boolean existsByCustomerIdAndRestaurantId(UUID customerId, UUID restaurantId);

    List<OrderDto> getRestaurantOrders(UUID restaurantId);
}