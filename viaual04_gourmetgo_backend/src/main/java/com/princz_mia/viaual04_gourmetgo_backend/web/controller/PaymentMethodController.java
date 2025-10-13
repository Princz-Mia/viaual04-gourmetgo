package com.princz_mia.viaual04_gourmetgo_backend.payment_method;

import com.princz_mia.viaual04_gourmetgo_backend.response.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.restaurant.RestaurantDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/payment")
public class PaymentMethodController {

    private final IPaymentMethodService paymentMethodService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllRestaurants() {
        List<PaymentMethodDto> list = paymentMethodService.getAllPaymentMethods();
        return ResponseEntity.ok(new ApiResponse("Success", list));
    }
}
