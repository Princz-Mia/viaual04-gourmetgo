package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.PaymentMethod;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.PaymentMethodDto;

import java.util.List;

public interface IPaymentMethodService {
    List<PaymentMethodDto> getAllPaymentMethods();

    PaymentMethodDto convertPaymentMethodToDto(PaymentMethod paymentMethod);
}