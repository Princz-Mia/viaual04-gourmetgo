package com.princz_mia.viaual04_gourmetgo_backend.cart_item;

import java.util.UUID;

public interface ICartItemService {

    CartItemDto addItemToCart(UUID cartId, UUID productId, int quantity);

    void removeItemFromCart(UUID cartId, UUID productId);

    CartItemDto updateItemQuantity(UUID cartId, UUID productId, int quantity);

    CartItem getCartItem(UUID cartId, UUID productId);
}
