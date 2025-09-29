import { toast } from "react-toastify";
import axiosInstance from "./axiosConfig";

export async function fetchProductsByRestaurantId(restaurantId) {
  try {
    const res = await axiosInstance.get(
      `/products/by-restaurant/${restaurantId}`
    );
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during fetching products!"
    );
    return null;
  }
}

export async function createProduct(dto) {
  try {
    const res = await axiosInstance.post("/products/add", dto);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during fetching products!"
    );
  }
  return null;
}

export async function updateProduct(id, dto) {
  try {
    const res = await axiosInstance.put(`/products/${id}`, dto);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during fetching products!"
    );
  }
  return null;
}

export async function deleteProduct(id) {
  try {
    const res = await axiosInstance.delete(`/products/${id}`);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during fetching products!"
    );
  }
  return null;
}
