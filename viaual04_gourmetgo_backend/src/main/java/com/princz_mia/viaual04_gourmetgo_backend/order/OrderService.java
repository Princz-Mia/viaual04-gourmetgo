package com.princz_mia.viaual04_gourmetgo_backend.order;

import com.princz_mia.viaual04_gourmetgo_backend.address.Address;
import com.princz_mia.viaual04_gourmetgo_backend.address.AddressDto;
import com.princz_mia.viaual04_gourmetgo_backend.address.AddressRepository;
import com.princz_mia.viaual04_gourmetgo_backend.cart.Cart;
import com.princz_mia.viaual04_gourmetgo_backend.cart.CartService;
import com.princz_mia.viaual04_gourmetgo_backend.coupon.Coupon;
import com.princz_mia.viaual04_gourmetgo_backend.coupon.CouponRepository;
import com.princz_mia.viaual04_gourmetgo_backend.coupon.CouponService;
import com.princz_mia.viaual04_gourmetgo_backend.coupon.CouponType;
import com.princz_mia.viaual04_gourmetgo_backend.customer.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.customer.CustomerRepository;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.order_item.OrderItem;
import com.princz_mia.viaual04_gourmetgo_backend.order_status.OrderStatus;
import com.princz_mia.viaual04_gourmetgo_backend.payment_method.PaymentMethod;
import com.princz_mia.viaual04_gourmetgo_backend.payment_method.PaymentMethodRepository;
import com.princz_mia.viaual04_gourmetgo_backend.product.Product;
import com.princz_mia.viaual04_gourmetgo_backend.product.ProductRepository;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.Restaurant;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final CouponRepository couponRepository;
    private final CouponService couponService;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found!", HttpStatus.NOT_FOUND));
    }

    @Override
    public List<OrderDto> getCustomerOrders(UUID customerId) {
        List<Order> orders = orderRepository.findByCustomer_Id(customerId);
        return orders
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<OrderDto> getRestaurantOrders(UUID restaurantId) {
        List<Order> orders = orderRepository.findByRestaurant_Id(restaurantId);
        return orders
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional
    @Override
    public Order placeOrder(Customer customer, OrderDto req) {
        Cart cart = cartService.getCartByCustomerId(customer.getId());
        if (cart.getItems().isEmpty()) {
            throw new AppException("Cart is empty", HttpStatus.BAD_REQUEST);
        }

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

        // 2) Shipping cím (ha nem sameAsBilling)
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
            // ha sameAsBilling, akkor billingAddr-et használjuk
            Order.ShippingInformation si = new Order.ShippingInformation();
            si.setFullName(bi.getFullName());
            si.setPhoneNumber(bi.getPhoneNumber());
            si.setAddress(billingAddr);
            order.setShippingInformation(si);
        }

        PaymentMethod pm = paymentMethodRepository.findById(req.getPaymentMethod().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found", HttpStatus.BAD_REQUEST));
        order.setPaymentMethod(pm);

        List<OrderItem> orderItems = cart.getItems().stream().map(ci -> {
            Product p = ci.getProduct();
            if (p.getInventory() < ci.getQuantity()) {
                throw new AppException("Insufficient inventory for " + p.getName(), HttpStatus.BAD_REQUEST);
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
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found", HttpStatus.BAD_REQUEST));

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
        cartService.clearCart(cart.getId());

        return saved;
    }

    @Override
    public void updateStatus(UUID id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order was not found", HttpStatus.NOT_FOUND));


        order.setStatus(status);
        orderRepository.save(order);
    }

    @Override
    public boolean existsByCustomerIdAndRestaurantId(UUID customerId, UUID restaurantId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found", HttpStatus.NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found", HttpStatus.NOT_FOUND));

        return orderRepository.existsByCustomer_IdAndRestaurant_Id(customer.getId(), restaurant.getId());
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
