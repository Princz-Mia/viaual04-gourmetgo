package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICouponService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICartService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.IOrderService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.impl.RewardService;
import org.springframework.context.ApplicationEventPublisher;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.*;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.*;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ServiceException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ErrorType;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.AddressDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ICartService cartService;
    private final ModelMapper modelMapper;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final CouponRepository couponRepository;
    private final OrderProcessingService orderProcessingService;
    private final RewardService rewardService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<OrderDto> getAllOrders() {
        LoggingUtils.logMethodEntry(log, "getAllOrders");
        long startTime = System.currentTimeMillis();
        
        List<OrderDto> orders = orderRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        LoggingUtils.logBusinessEvent(log, "ALL_ORDERS_RETRIEVED", "count", orders.size());
        LoggingUtils.logPerformance(log, "getAllOrders", System.currentTimeMillis() - startTime);
        
        return orders;
    }

    @Override
    public OrderDto getOrder(UUID orderId) {
        LoggingUtils.logMethodEntry(log, "getOrder", "orderId", orderId);
        long startTime = System.currentTimeMillis();
        
        try {
            OrderDto order = orderRepository.findById(orderId)
                    .map(this::convertToDto)
                    .orElseThrow(() -> new ServiceException("Order not found!", ErrorType.RESOURCE_NOT_FOUND));
            
            LoggingUtils.logBusinessEvent(log, "ORDER_RETRIEVED", "orderId", orderId);
            LoggingUtils.logPerformance(log, "getOrder", System.currentTimeMillis() - startTime);
            
            return order;
        } catch (ServiceException e) {
            LoggingUtils.logError(log, "Order not found", e, "orderId", orderId);
            throw e;
        }
    }

    @Override
    public List<OrderDto> getCustomerOrders(UUID customerId) {
        LoggingUtils.logMethodEntry(log, "getCustomerOrders", "customerId", customerId);
        long startTime = System.currentTimeMillis();
        
        List<Order> orders = orderRepository.findByCustomer_Id(customerId);
        List<OrderDto> orderDtos = orders.stream()
                .map(this::convertToDto)
                .toList();
        
        LoggingUtils.logBusinessEvent(log, "CUSTOMER_ORDERS_RETRIEVED", "customerId", customerId, "count", orderDtos.size());
        LoggingUtils.logPerformance(log, "getCustomerOrders", System.currentTimeMillis() - startTime);
        
        return orderDtos;
    }

    @Override
    public List<OrderDto> getRestaurantOrders(UUID restaurantId) {
        LoggingUtils.logMethodEntry(log, "getRestaurantOrders", "restaurantId", restaurantId);
        long startTime = System.currentTimeMillis();
        
        List<Order> orders = orderRepository.findByRestaurant_Id(restaurantId);
        List<OrderDto> orderDtos = orders.stream()
                .map(this::convertToDto)
                .toList();
        
        LoggingUtils.logBusinessEvent(log, "RESTAURANT_ORDERS_RETRIEVED", "restaurantId", restaurantId, "count", orderDtos.size());
        LoggingUtils.logPerformance(log, "getRestaurantOrders", System.currentTimeMillis() - startTime);
        
        return orderDtos;
    }

    @Transactional
    @Override
    public Order placeOrder(Customer customer, OrderDto req) {
        LoggingUtils.logMethodEntry(log, "placeOrder", "customerId", customer.getId(), "restaurantId", req.getRestaurant().getId());
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Processing order placement for customer: {} at restaurant: {}", 
                customer.getId(), req.getRestaurant().getId());
            
            Cart cart = cartService.getCartByCustomerId(customer.getId());
            log.debug("Cart retrieved with {} items", cart.getItems().size());

            Order order = orderProcessingService.createOrderFromRequest(customer, cart, req);

            Order saved = orderRepository.save(order);
            log.info("Order saved with ID: {}", saved.getId());
            
            eventPublisher.publishEvent(saved);
            cartService.clearCart(cart.getId());
            log.debug("Cart cleared for customer: {}", customer.getId());
            
            LoggingUtils.logBusinessEvent(log, "ORDER_PLACED", "orderId", saved.getId(), "customerId", customer.getId(), "restaurantId", order.getRestaurant().getId(), "totalAmount", saved.getTotalAmount());
            LoggingUtils.logPerformance(log, "placeOrder", System.currentTimeMillis() - startTime);
            log.info("Order placement completed successfully for order: {}", saved.getId());
            
            return saved;
        } catch (Exception e) {
            LoggingUtils.logError(log, "Error placing order", e, "customerId", customer.getId(), "restaurantId", req.getRestaurant().getId());
            throw e;
        }
    }

    @Override
    public void updateStatus(UUID id, OrderStatus status) {
        LoggingUtils.logMethodEntry(log, "updateStatus", "orderId", id, "status", status);
        long startTime = System.currentTimeMillis();
        
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new ServiceException("Order was not found", ErrorType.RESOURCE_NOT_FOUND));

            OrderStatus previousStatus = order.getStatus();
            order.setStatus(status);
            Order saved = orderRepository.save(order);
            
            // Publish order event for real-time updates
            eventPublisher.publishEvent(saved);
            
            // Trigger revenue recalculation for cancelled orders
            if (status == OrderStatus.CANCELLED) {
                log.info("Order cancelled, revenue will be recalculated on next dashboard refresh");
            }
            
            // Handle reward processing based on status change
            if (status == OrderStatus.DELIVERED) {
                rewardService.awardPointsForOrder(order);
            } else if (status == OrderStatus.CANCELLED &&
                (previousStatus == OrderStatus.CONFIRMED || previousStatus == OrderStatus.PREPARING)) {
                rewardService.compensateFailedOrder(order);
            }
            
            LoggingUtils.logBusinessEvent(log, "ORDER_STATUS_UPDATED", "orderId", id, "newStatus", status);
            LoggingUtils.logPerformance(log, "updateStatus", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to update order status", e, "orderId", id, "status", status);
            throw e;
        }
    }

    @Override
    public boolean existsByCustomerIdAndRestaurantId(UUID customerId, UUID restaurantId) {
        LoggingUtils.logMethodEntry(log, "existsByCustomerIdAndRestaurantId", "customerId", customerId, "restaurantId", restaurantId);
        long startTime = System.currentTimeMillis();
        
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ServiceException("Customer not found", ErrorType.RESOURCE_NOT_FOUND));

            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new ServiceException("Restaurant not found", ErrorType.RESOURCE_NOT_FOUND));

            boolean exists = orderRepository.existsByCustomer_IdAndRestaurant_Id(customer.getId(), restaurant.getId());
            
            LoggingUtils.logBusinessEvent(log, "ORDER_EXISTENCE_CHECKED", "customerId", customerId, "restaurantId", restaurantId, "exists", exists);
            LoggingUtils.logPerformance(log, "existsByCustomerIdAndRestaurantId", System.currentTimeMillis() - startTime);
            
            return exists;
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to check order existence", e, "customerId", customerId, "restaurantId", restaurantId);
            throw e;
        }
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItemList) {
        return orderItemList
                .stream()
                .map(item -> item.getPrice()
                        .multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public OrderDto convertToDto(Order order) {
        OrderDto dto = modelMapper.map(order, OrderDto.class);
        dto.setUsedRewardPoints(order.getUsedRewardPoints());
        return dto;
    }
}