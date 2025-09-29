import React, { createContext, useReducer, useContext, useEffect } from 'react';
import {
    fetchCart as apiFetchCart,
    addToCart as apiAddToCart,
    updateCartItem as apiUpdateCartItem,
    removeCartItem as apiRemoveCartItem,
    clearCart as apiClearCart
} from '../api/cartService';
import { toast } from 'react-toastify';
import { AuthContext } from "./AuthContext";

const CartContext = createContext();

const initialState = {
    items: [],
    total: 0,
    loading: false,
    error: null,
    cartId: null,
    coupon: null
};

function cartReducer(state, action) {
    switch (action.type) {
        case 'FETCH_CART_START':
            return { ...state, loading: true, error: null };
        case 'FETCH_CART_SUCCESS':
            return {
                ...state,
                loading: false,
                items: action.payload.items,
                total: parseFloat(action.payload.total) || 0,
                cartId: action.payload.cartId
            };
        case 'FETCH_CART_CLEAR':
            return { ...state, loading: false, items: [], total: 0, cartId: null };
        case 'FETCH_CART_ERROR':
            return { ...state, loading: false, error: action.payload, items: [], total: 0, cartId: null };
        case 'APPLY_COUPON':
            return { ...state, coupon: action.payload };
        case 'CLEAR_COUPON':
            return { ...state, coupon: null };
        default:
            return state;
    }
}

export const CartProvider = ({ children }) => {
    const { user } = useContext(AuthContext);
    const [state, dispatch] = useReducer(cartReducer, initialState);

    const loadCart = async () => {
        dispatch({ type: 'FETCH_CART_START' });
        try {
            const cart = await apiFetchCart();
            console.log(cart);
            const items = cart.items.map(item => ({
                id: item.product.id,
                name: item.product.name,
                image: item.product.image?.downloadUrl,
                price: item.unitPrice,
                quantity: item.quantity,
                inventory: item.product.inventory,
                totalPrice: item.totalPrice,
                restaurant: item.product.restaurant
            }));
            dispatch({
                type: 'FETCH_CART_SUCCESS',
                payload: {
                    items,
                    total: cart.totalAmount,
                    cartId: cart.id
                }
            });
        } catch (e) {
            dispatch({ type: 'FETCH_CART_ERROR', payload: e.message || 'Failed to load cart' });
            toast.error(e.message || 'Failed to load cart');
        }
    };

    // React to user role changes
    useEffect(() => {
        if (user?.role.authority === 'ROLE_CUSTOMER') {
            loadCart();
        } else {
            // Clear cart for non-customers
            dispatch({ type: 'FETCH_CART_CLEAR' });
        }
    }, [user]);

    const addItem = async (productId, qty = 1) => {
        if (user?.role.authority !== 'ROLE_CUSTOMER') return;
        try {
            await apiAddToCart(productId, qty);
            await loadCart();
            toast.success('Item added to cart');
        } catch (e) {
            toast.error(e.message || 'Failed to add item to cart');
        }
    };

    const updateItem = async (productId, qty) => {
        if (user?.role.authority !== 'ROLE_CUSTOMER') return;
        try {
            await apiUpdateCartItem(productId, qty);
            await loadCart();
            toast.success('Cart updated');
        } catch (e) {
            toast.error(e.message || 'Failed to update cart');
        }
    };

    const removeItem = async (productId) => {
        if (user?.role.authority !== 'ROLE_CUSTOMER') return;
        try {
            await apiRemoveCartItem(productId);
            await loadCart();
            toast.success('Item removed from cart');
        } catch (e) {
            toast.error(e.message || 'Failed to remove item from cart');
        }
    };

    const clear = async () => {
        if (user?.role.authority !== 'ROLE_CUSTOMER' || !state.cartId) return;
        try {
            await apiClearCart(state.cartId);
            await loadCart();
            toast.success('Cart cleared');
        } catch (e) {
            toast.error(e.message || 'Failed to clear cart');
        }
    };

    const applyCoupon = (couponDto) => {
        dispatch({ type: 'APPLY_COUPON', payload: couponDto });
    };

    const clearCoupon = () => {
        dispatch({ type: 'CLEAR_COUPON' });
    };

    return (
        <CartContext.Provider
            value={{
                items: state.items,
                total: state.total,
                loading: state.loading,
                error: state.error,
                coupon: state.coupon,
                applyCoupon,
                clearCoupon,
                addItem,
                updateItem,
                removeItem,
                clear,
                reload: loadCart
            }}
        >
            {children}
        </CartContext.Provider>
    );
};

export const useCart = () => {
    const context = useContext(CartContext);
    if (!context) {
        throw new Error('useCart must be used within a CartProvider');
    }
    return context;
};