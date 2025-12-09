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
    coupon: null,
    pointsToRedeem: 0
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
            return { ...state, loading: false, items: [], total: 0, cartId: null, pointsToRedeem: 0 };
        case 'FETCH_CART_ERROR':
            return { ...state, loading: false, error: action.payload, items: [], total: 0, cartId: null, pointsToRedeem: 0 };
        case 'APPLY_COUPON':
            return { ...state, coupon: action.payload };
        case 'CLEAR_COUPON':
            return { ...state, coupon: null };
        case 'SET_POINTS':
            return { ...state, pointsToRedeem: action.payload };
        case 'CLEAR_POINTS':
            return { ...state, pointsToRedeem: 0 };
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
        const userRole = user?.role?.authority || user?.role;
        if (userRole === 'ROLE_CUSTOMER') {
            loadCart();
        } else {
            // Clear cart for non-customers
            dispatch({ type: 'FETCH_CART_CLEAR' });
        }
    }, [user]);

    const addItem = async (productId, qty = 1) => {
        const userRole = user?.role?.authority || user?.role;
        if (!user || userRole !== 'ROLE_CUSTOMER') {
            toast.info('Please log in to add items to cart');
            window.location.href = '/login';
            return;
        }
        try {
            await apiAddToCart(productId, qty);
            await loadCart();
            toast.success('Item added to cart');
        } catch (e) {
            toast.error(e.message || 'Failed to add item to cart');
        }
    };

    const updateItem = async (productId, qty) => {
        const userRole = user?.role?.authority || user?.role;
        if (userRole !== 'ROLE_CUSTOMER') return;
        try {
            await apiUpdateCartItem(productId, qty);
            await loadCart();
            toast.success('Cart updated');
        } catch (e) {
            toast.error(e.message || 'Failed to update cart');
        }
    };

    const removeItem = async (productId) => {
        const userRole = user?.role?.authority || user?.role;
        if (userRole !== 'ROLE_CUSTOMER') return;
        try {
            await apiRemoveCartItem(productId);
            await loadCart();
            toast.success('Item removed from cart');
        } catch (e) {
            toast.error(e.message || 'Failed to remove item from cart');
        }
    };

    const clear = async () => {
        const userRole = user?.role?.authority || user?.role;
        if (userRole !== 'ROLE_CUSTOMER' || !state.cartId) return;
        try {
            await apiClearCart(state.cartId);
            dispatch({ type: 'FETCH_CART_CLEAR' });
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

    const setPoints = (points) => {
        dispatch({ type: 'SET_POINTS', payload: points });
    };

    const clearPoints = () => {
        dispatch({ type: 'CLEAR_POINTS' });
    };

    return (
        <CartContext.Provider
            value={{
                items: state.items,
                total: state.total,
                loading: state.loading,
                error: state.error,
                coupon: state.coupon,
                pointsToRedeem: state.pointsToRedeem,
                applyCoupon,
                clearCoupon,
                setPoints,
                clearPoints,
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