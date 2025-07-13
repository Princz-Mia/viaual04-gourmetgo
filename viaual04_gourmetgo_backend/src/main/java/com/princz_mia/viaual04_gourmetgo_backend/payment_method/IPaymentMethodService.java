package com.princz_mia.viaual04_gourmetgo_backend.payment_method;

import java.util.List;

public interface IPaymentMethodService {
    List<PaymentMethodDto> getAllPaymentMethods();

    PaymentMethodDto convertPaymentMethodToDto(PaymentMethod paymentMethod);
}
