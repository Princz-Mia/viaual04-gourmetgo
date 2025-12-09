package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICouponService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.*;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.*;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ServiceException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ErrorType;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.AddressDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProcessingService {

    private final ProductRepository productRepository;
    private final RestaurantRepository restaurantRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ICouponService couponService;
    private final RewardService rewardService;

    public Order createOrderFromRequest(Customer customer, Cart cart, OrderDto request) {
        validateOrderRequest(cart, request);
        
        Order order = buildBaseOrder(customer, request);
        setBillingInformation(order, request);
        setShippingInformation(order, request);
        setPaymentMethod(order, request);
        setOrderItems(order, cart);
        calculateTotalAmount(order, request);
        
        return order;
    }

    private void validateOrderRequest(Cart cart, OrderDto request) {
        if (cart.getItems().isEmpty()) {
            throw new ServiceException("Cart is empty", ErrorType.BUSINESS_RULE_VIOLATION);
        }
        if (request.getRestaurant() == null || request.getRestaurant().getId() == null) {
            throw new ServiceException("Restaurant is required", ErrorType.VALIDATION_ERROR);
        }
    }

    private Order buildBaseOrder(Customer customer, OrderDto request) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderNotes(request.getOrderNotes());
        order.setDeliveryInstructions(request.getDeliveryInstructions());
        
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurant().getId())
                .orElseThrow(() -> new ServiceException("Restaurant not found", ErrorType.RESOURCE_NOT_FOUND));
        order.setRestaurant(restaurant);
        
        return order;
    }

    private void setBillingInformation(Order order, OrderDto request) {
        AddressDto billDto = request.getBillingInformation().getAddress();
        Address billingAddr = createAddress(billDto);
        addressRepository.save(billingAddr);

        Order.BillingInformation bi = new Order.BillingInformation();
        bi.setFullName(request.getBillingInformation().getFullName());
        bi.setPhoneNumber(request.getBillingInformation().getPhoneNumber());
        bi.setAddress(billingAddr);
        order.setBillingInformation(bi);
    }

    private void setShippingInformation(Order order, OrderDto request) {
        Address shippingAddr;
        Order.ShippingInformation si = new Order.ShippingInformation();
        
        if (request.getShippingInformation() != null) {
            AddressDto shipDto = request.getShippingInformation().getAddress();
            shippingAddr = createAddress(shipDto);
            addressRepository.save(shippingAddr);
            si.setFullName(request.getShippingInformation().getFullName());
            si.setPhoneNumber(request.getShippingInformation().getPhoneNumber());
        } else {
            shippingAddr = order.getBillingInformation().getAddress();
            si.setFullName(order.getBillingInformation().getFullName());
            si.setPhoneNumber(order.getBillingInformation().getPhoneNumber());
        }
        
        si.setAddress(shippingAddr);
        order.setShippingInformation(si);
    }

    private Address createAddress(AddressDto dto) {
        return Address.builder()
                .unitNumber(dto.getUnitNumber())
                .addressLine(dto.getAddressLine())
                .city(dto.getCity())
                .postalCode(dto.getPostalCode())
                .region(dto.getRegion())
                .build();
    }

    private void setPaymentMethod(Order order, OrderDto request) {
        PaymentMethod pm = paymentMethodRepository.findById(request.getPaymentMethod().getId())
                .orElseThrow(() -> new ServiceException("Payment method not found", ErrorType.RESOURCE_NOT_FOUND));
        order.setPaymentMethod(pm);
    }

    private void setOrderItems(Order order, Cart cart) {
        List<OrderItem> orderItems = cart.getItems().stream().map(ci -> {
            Product p = ci.getProduct();
            if (p.getInventory() < ci.getQuantity()) {
                throw new ServiceException("Insufficient inventory for " + p.getName(), ErrorType.BUSINESS_RULE_VIOLATION);
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
    }

    private void calculateTotalAmount(Order order, OrderDto request) {
        BigDecimal itemsTotal = order.getOrderItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalTotal = itemsTotal.add(order.getRestaurant().getDeliveryFee());
        
        // Apply coupon if present
        if (request.getCoupon() != null && StringUtils.hasText(request.getCoupon().getCode())) {
            Coupon coupon = couponService.validateCoupon(order.getCustomer().getId(), request.getCoupon().getCode());
            order.setCoupon(coupon);
            finalTotal = applyCouponDiscount(finalTotal, coupon, order.getRestaurant().getDeliveryFee());
            couponService.recordUsage(coupon, order.getCustomer());
        }
        
        // Apply points redemption if requested
        if (request.getPointsToRedeem() != null && request.getPointsToRedeem().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = rewardService.useRewardPoints(order.getCustomer().getId(), request.getPointsToRedeem(), order);
            order.setUsedRewardPoints(request.getPointsToRedeem());
            finalTotal = finalTotal.subtract(discount);
        }
        
        order.setTotalAmount(finalTotal.max(BigDecimal.ZERO));
    }

    private BigDecimal applyCouponDiscount(BigDecimal total, Coupon coupon, BigDecimal deliveryFee) {
        if (coupon.getType() == CouponType.AMOUNT) {
            return total.subtract(coupon.getValue());
        } else if (coupon.getType() == CouponType.FREE_SHIP) {
            return total.subtract(deliveryFee);
        }
        return total;
    }
}