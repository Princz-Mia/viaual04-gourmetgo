package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICouponService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICartService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.IOrderService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.*;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.*;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ErrorType;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
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
    private final ICouponService couponService;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;

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
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found!"));
            
            LoggingUtils.logBusinessEvent(log, "ORDER_RETRIEVED", "orderId", orderId);
            LoggingUtils.logPerformance(log, "getOrder", System.currentTimeMillis() - startTime);
            
            return order;
        } catch (ResourceNotFoundException e) {
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
            if (cart.getItems().isEmpty()) {
                log.warn("Order placement failed - empty cart for customer: {}", customer.getId());
                throw new AppException("Cart is empty", ErrorType.BUSINESS_RULE_VIOLATION);
            }
            
            log.debug("Cart validated with {} items", cart.getItems().size());

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDate.now());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderNotes(req.getOrderNotes());
        order.setDeliveryInstructions(req.getDeliveryInstructions());

        AddressDto billDto = req.getBillingInformation().getAddress();
        Address billingAddr = Address.builder()
                .unitNumber(billDto.getUnitNumber())
                .addressLine(billDto.getAddressLine())
                .city(billDto.getCity())
                .postalCode(billDto.getPostalCode())
                .region(billDto.getRegion())
                .build();
        addressRepository.save(billingAddr);

        Order.BillingInformation bi = new Order.BillingInformation();
        bi.setFullName(req.getBillingInformation().getFullName());
        bi.setPhoneNumber(req.getBillingInformation().getPhoneNumber());
        bi.setAddress(billingAddr);
        order.setBillingInformation(bi);

        Address shippingAddr;
        if (req.getShippingInformation() != null) {
            AddressDto shipDto = req.getShippingInformation().getAddress();
            shippingAddr = Address.builder()
                    .unitNumber(shipDto.getUnitNumber())
                    .addressLine(shipDto.getAddressLine())
                    .city(shipDto.getCity())
                    .postalCode(shipDto.getPostalCode())
                    .region(shipDto.getRegion())
                    .build();
            addressRepository.save(shippingAddr);

            Order.ShippingInformation si = new Order.ShippingInformation();
            si.setFullName(req.getShippingInformation().getFullName());
            si.setPhoneNumber(req.getShippingInformation().getPhoneNumber());
            si.setAddress(shippingAddr);
            order.setShippingInformation(si);
        } else {
            Order.ShippingInformation si = new Order.ShippingInformation();
            si.setFullName(bi.getFullName());
            si.setPhoneNumber(bi.getPhoneNumber());
            si.setAddress(billingAddr);
            order.setShippingInformation(si);
        }

        PaymentMethod pm = paymentMethodRepository.findById(req.getPaymentMethod().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
        order.setPaymentMethod(pm);

        List<OrderItem> orderItems = cart.getItems().stream().map(ci -> {
            Product p = ci.getProduct();
            if (p.getInventory() < ci.getQuantity()) {
                throw new AppException("Insufficient inventory for " + p.getName(), ErrorType.BUSINESS_RULE_VIOLATION);
            }
            p.setInventory(p.getInventory() - ci.getQuantity());
            productRepository.save(p);

            return OrderItem.builder()
                    .order(order)
                    .product(p)
                    .quantity(ci.getQuantity())
                    .price(ci.getUnitPrice())
                    .build();
        }).toList();
        order.setOrderItems(new HashSet<>(orderItems));

        BigDecimal itemsTotal = orderItems.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Restaurant restaurant = restaurantRepository.findById(req.getRestaurant().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Coupon coupon = null;
        if (req.getCoupon() != null && StringUtils.hasText(req.getCoupon().getCode())) {
            coupon = couponService.validateCoupon(customer.getId(), req.getCoupon().getCode());
        }

        if (coupon != null) {
            order.setCoupon(coupon);
        }

        BigDecimal finalTotal = itemsTotal.add(restaurant.getDeliveryFee());
        if (coupon != null) {
            if (coupon.getType() == CouponType.AMOUNT) {
                finalTotal = finalTotal.subtract(coupon.getValue());
            } else if (coupon.getType() == CouponType.FREE_SHIP) {
                finalTotal = finalTotal.subtract(restaurant.getDeliveryFee());
            }
            couponService.recordUsage(coupon, customer);
        }
        order.setTotalAmount(finalTotal.max(BigDecimal.ZERO));

        order.setRestaurant(restaurant);

        Order saved = orderRepository.save(order);
        log.info("Order saved with ID: {}", saved.getId());
        
        cartService.clearCart(cart.getId());
        log.debug("Cart cleared for customer: {}", customer.getId());
        
        LoggingUtils.logBusinessEvent(log, "ORDER_PLACED", "orderId", saved.getId(), "customerId", customer.getId(), "restaurantId", restaurant.getId(), "totalAmount", saved.getTotalAmount(), "itemCount", orderItems.size(), "couponUsed", coupon != null);
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
                    .orElseThrow(() -> new ResourceNotFoundException("Order was not found"));

            order.setStatus(status);
            orderRepository.save(order);
            
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
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

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
        return modelMapper.map(order, OrderDto.class);
    }
}