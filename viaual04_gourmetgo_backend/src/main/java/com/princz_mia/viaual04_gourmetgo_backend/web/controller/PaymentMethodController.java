package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IPaymentMethodService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.PaymentMethodDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/payment")
@Slf4j
public class PaymentMethodController {

    private final IPaymentMethodService paymentMethodService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllPaymentMethods() {
        LoggingUtils.logMethodEntry(log, "getAllPaymentMethods");
        long startTime = System.currentTimeMillis();
        
        List<PaymentMethodDto> list = paymentMethodService.getAllPaymentMethods();
        
        LoggingUtils.logBusinessEvent(log, "PAYMENT_METHODS_RETRIEVED", "count", list.size());
        LoggingUtils.logPerformance(log, "getAllPaymentMethods", System.currentTimeMillis() - startTime);
        
        return ResponseEntity.ok(new ApiResponse("Success", list));
    }
}