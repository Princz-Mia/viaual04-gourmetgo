import { toast } from "react-toastify";
import axiosInstance from "./axiosConfig";

/**
 * Lekéri az összes rendelést.
 */
export async function fetchAllOrders() {
  try {
    const res = await axiosInstance.get("/orders");
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during fetching orders!"
    );
    return null;
  }
}

/**
 * Módosítja egy rendelés státuszát.
 * @param id rendelés azonosító
 * @param status új státusz (PENDING, CONFIRMED, stb.)
 */
export async function fetchCustomerOrders(customerId) {
  try {
    const res = await axiosInstance.get(`/orders/by-customer/${customerId}`);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during updating order status!"
    );
    return null;
  }
}

export async function fetchRestaurantOrders(restaurantId) {
  try {
    const res = await axiosInstance.get(
      `/orders/by-restaurant/${restaurantId}`
    );
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during updating order status!"
    );
    return null;
  }
}

export async function fetchOrderById(orderId) {
  try {
    const res = await axiosInstance.get(`/orders/by-id/${orderId}`);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during updating order status!"
    );
    return null;
  }
}

export async function updateOrderStatus(orderId, status) {
  try {
    const res = await axiosInstance.put(
      `/orders/${orderId}/status`,
      JSON.stringify(status),
      { headers: { "Content-Type": "application/json" } }
    );
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during updating order status!"
    );
    return null;
  }
}

export async function placeOrder(orderDto) {
  try {
    const res = await axiosInstance.post("/orders", orderDto);
    return res.data.data;
  } catch (error) {
    const msg =
      error.response?.data?.message ||
      error.message ||
      "An error occurred while placing order!";
    toast.error(msg);
  }
  return null;
}
