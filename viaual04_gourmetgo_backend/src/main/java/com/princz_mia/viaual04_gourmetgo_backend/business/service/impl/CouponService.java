package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICouponService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Coupon;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.CouponType;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.CouponUsage;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.CouponRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.CouponUsageRepository;
import com.princz_mia.viaual04_gourmetgo_backend.events.CouponEvent;
import com.princz_mia.viaual04_gourmetgo_backend.events.EventType;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ErrorType;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CouponDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService implements ICouponService
{

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher publisher;

    @Override
    public List<CouponDto> getAllCoupons() {
        LoggingUtils.logMethodEntry(log, "getAllCoupons");
        List<CouponDto> coupons = couponRepository.findAll().stream()
                .filter(coupon -> !coupon.isDeleted())
                .map(this::convertCouponToDto)
                .collect(Collectors.toList());
        LoggingUtils.logBusinessEvent(log, "COUPONS_RETRIEVED", "count", coupons.size());
        return coupons;
    }

    @Override
    public Coupon createCoupon(CouponDto couponDto) {
        LoggingUtils.logMethodEntry(log, "createCoupon", "code", couponDto.getCode(), "type", couponDto.getType());
        return Optional.of(couponDto)
                .filter(coupon -> !couponRepository.existsByCode(couponDto.getCode()))
                .map(dto -> {
                    Coupon coupon = new Coupon();
                    coupon.setCode(couponDto.getCode());
                    coupon.setType(couponDto.getType());
                    if(coupon.getType().equals(CouponType.AMOUNT)) {
                        coupon.setValue(couponDto.getValue());
                    } else {
                        coupon.setValue(new BigDecimal("0.00"));
                    }
                    coupon.setExpirationDate(couponDto.getExpirationDate());
                    Coupon couponSaved = couponRepository.save(coupon);

                    publisher.publishEvent(new CouponEvent(couponSaved, EventType.COUPON_PUBLISHED));
                    LoggingUtils.logBusinessEvent(log, "COUPON_CREATED", "couponId", couponSaved.getId(), "code", couponSaved.getCode());

                    return couponSaved;
                }).orElseThrow(() -> new AlreadyExistsException("Coupon already exists!"));
    }

    @Override
    public Coupon updateCoupon(UUID id, CouponDto couponDto) {
        LoggingUtils.logMethodEntry(log, "updateCoupon", "id", id, "code", couponDto.getCode());
        Coupon updatedCoupon = couponRepository.findById(id).map(existingCoupon -> {
            existingCoupon.setCode(couponDto.getCode());
            existingCoupon.setType(couponDto.getType());
            if(existingCoupon.getType().equals(CouponType.AMOUNT)) {
                existingCoupon.setValue(couponDto.getValue());
            } else {
                existingCoupon.setValue(new BigDecimal("0.00"));
            }
            existingCoupon.setExpirationDate(couponDto.getExpirationDate());
            return couponRepository.save(existingCoupon);
        }).orElseThrow(() -> new ResourceNotFoundException("Coupon not found!"));
        LoggingUtils.logBusinessEvent(log, "COUPON_UPDATED", "couponId", id, "code", updatedCoupon.getCode());
        return updatedCoupon;
    }

    @Override
    public void deleteCoupon(UUID id) {
        LoggingUtils.logMethodEntry(log, "deleteCoupon", "id", id);
        couponRepository.findById(id).ifPresentOrElse(coupon -> {
            couponRepository.delete(coupon);
            LoggingUtils.logBusinessEvent(log, "COUPON_DELETED", "couponId", id, "code", coupon.getCode());
        }, () -> {
            throw new ResourceNotFoundException("Coupon not found");
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Coupon validateCoupon(UUID customerId, String code) {
        LoggingUtils.logMethodEntry(log, "validateCoupon", "customerId", customerId, "code", code);
        Coupon c = Optional.ofNullable(couponRepository.findByCode(code.toUpperCase()))
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));

        if (c.getExpirationDate().isBefore(LocalDate.now())) {
            LoggingUtils.logError(log, "Coupon expired", null, "code", code, "customerId", customerId);
            throw new AppException("Coupon expired", ErrorType.BUSINESS_RULE_VIOLATION);
        }
        if (couponUsageRepository.existsByCoupon_CodeAndCustomer_Id(code.toUpperCase(), customerId)) {
            LoggingUtils.logError(log, "Coupon already used", null, "code", code, "customerId", customerId);
            throw new AppException("Coupon already used by you", ErrorType.BUSINESS_RULE_VIOLATION);
        }
        LoggingUtils.logBusinessEvent(log, "COUPON_VALIDATED", "couponId", c.getId(), "customerId", customerId);
        return c;
    }

    @Transactional
    @Override
    public void recordUsage(Coupon coupon, Customer customer) {
        LoggingUtils.logMethodEntry(log, "recordUsage", "couponId", coupon.getId(), "customerId", customer.getId());
        couponUsageRepository.save(CouponUsage.builder()
                .coupon(coupon)
                .customer(customer)
                .build());
        LoggingUtils.logBusinessEvent(log, "COUPON_USAGE_RECORDED", "couponId", coupon.getId(), "customerId", customer.getId());
    }

    @Override
    public CouponDto convertCouponToDto(Coupon coupon) {
        LoggingUtils.logMethodEntry(log, "convertCouponToDto", "couponId", coupon.getId());
        return modelMapper.map(coupon, CouponDto.class);
    }
}