import { toast } from "react-toastify";
import axiosInstance from "./axiosConfig";

export async function fetchCart() {
  try {
    const res = await axiosInstance.get(`/carts/by-customer`);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message || "An error occurred during fetching cart!"
    );
    return null;
  }
}

export async function clearCart(cartId) {
  try {
    const res = await axiosInstance.delete(`/carts/${cartId}`);
    toast.success(res.data.message);
  } catch (error) {
    toast.error(
      error.response?.data?.message || "An error occurred during clearing cart!"
    );
  }
}

export async function addToCart(productId, quantity = 1) {
  try {
    const res = await axiosInstance.post("/cartItems/add", null, {
      params: { productId, quantity },
    });
    return res.data.data;
  } catch (error) {
    throw (
      error.response?.data?.message ||
      "An error occurred during adding item to cart!"
    );
  }
}

export async function updateCartItem(productId, quantity) {
  try {
    const res = await axiosInstance.put(
      `/cartItems/by-product/${productId}`,
      null,
      {
        params: { quantity },
      }
    );
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during updating item in cart!"
    );
    return null;
  }
}

export async function removeCartItem(productId) {
  try {
    const res = await axiosInstance.delete(
      `/cartItems/by-product/${productId}`
    );
    toast.success(res.data.nessage);
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during removing item from cart!"
    );
    return null;
  }
}
