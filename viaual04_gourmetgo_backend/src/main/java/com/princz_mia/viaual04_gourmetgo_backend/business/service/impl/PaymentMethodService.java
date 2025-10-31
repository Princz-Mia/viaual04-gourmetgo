package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IPaymentMethodService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.PaymentMethod;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.PaymentMethodRepository;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.PaymentMethodDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodService implements IPaymentMethodService
{

    private final PaymentMethodRepository paymentMethodRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<PaymentMethodDto> getAllPaymentMethods() {
        LoggingUtils.logMethodEntry(log, "getAllPaymentMethods");
        List<PaymentMethodDto> paymentMethods = paymentMethodRepository.findAll().stream()
                .map(this::convertPaymentMethodToDto)
                .collect(Collectors.toList());
        LoggingUtils.logBusinessEvent(log, "PAYMENT_METHODS_RETRIEVED", "count", paymentMethods.size());
        return paymentMethods;
    }

    @Override
    public PaymentMethodDto convertPaymentMethodToDto(PaymentMethod paymentMethod) {
        LoggingUtils.logMethodEntry(log, "convertPaymentMethodToDto", "paymentMethodId", paymentMethod.getId());
        return modelMapper.map(paymentMethod, PaymentMethodDto.class);
    }
}