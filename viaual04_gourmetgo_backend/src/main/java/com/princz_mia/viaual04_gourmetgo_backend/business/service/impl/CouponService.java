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
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CouponDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService implements ICouponService
{

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher publisher;

    @Override
    public List<CouponDto> getAllCoupons() {
        return couponRepository.findAll().stream()
                .filter(coupon -> !coupon.isDeleted())
                .map(this::convertCouponToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Coupon createCoupon(CouponDto couponDto) {
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

                    return couponSaved;
                }).orElseThrow(() -> new AlreadyExistsException("Coupon already exists!", HttpStatus.BAD_REQUEST));
    }

    @Override
    public Coupon updateCoupon(UUID id, CouponDto couponDto) {
        return couponRepository.findById(id).map(existingCoupon -> {
            existingCoupon.setCode(couponDto.getCode());
            existingCoupon.setType(couponDto.getType());
            if(existingCoupon.getType().equals(CouponType.AMOUNT)) {
                existingCoupon.setValue(couponDto.getValue());
            } else {
                existingCoupon.setValue(new BigDecimal("0.00"));
            }
            existingCoupon.setExpirationDate(couponDto.getExpirationDate());
            return couponRepository.save(existingCoupon);
        }).orElseThrow(() -> new ResourceNotFoundException("Coupon not found!", HttpStatus.NOT_FOUND));
    }

    @Override
    public void deleteCoupon(UUID id) {
        couponRepository.findById(id).ifPresentOrElse(couponRepository::delete, () -> {
            throw new ResourceNotFoundException("Coupon not found", HttpStatus.NOT_FOUND);
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Coupon validateCoupon(UUID customerId, String code) {
        Coupon c = Optional.ofNullable(couponRepository.findByCode(code.toUpperCase()))
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found", HttpStatus.NOT_FOUND));

        if (c.getExpirationDate().isBefore(LocalDate.now())) {
            throw new AppException("Coupon expired", HttpStatus.BAD_REQUEST);
        }
        if (couponUsageRepository.existsByCoupon_CodeAndCustomer_Id(code.toUpperCase(), customerId)) {
            throw new AppException("Coupon already used by you", HttpStatus.BAD_REQUEST);
        }
        return c;
    }

    @Transactional
    @Override
    public void recordUsage(Coupon coupon, Customer customer) {
        couponUsageRepository.save(CouponUsage.builder()
                .coupon(coupon)
                .customer(customer)
                .build());
    }

    @Override
    public CouponDto convertCouponToDto(Coupon coupon) {
        return modelMapper.map(coupon, CouponDto.class);
    }
}