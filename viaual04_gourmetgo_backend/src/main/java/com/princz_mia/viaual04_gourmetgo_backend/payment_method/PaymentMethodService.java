package com.princz_mia.viaual04_gourmetgo_backend.payment_method;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodService implements IPaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<PaymentMethodDto> getAllPaymentMethods() {
        return paymentMethodRepository.findAll().stream()
                .map(this::convertPaymentMethodToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentMethodDto convertPaymentMethodToDto(PaymentMethod paymentMethod) {
        return modelMapper.map(paymentMethod, PaymentMethodDto.class);
    }
}
