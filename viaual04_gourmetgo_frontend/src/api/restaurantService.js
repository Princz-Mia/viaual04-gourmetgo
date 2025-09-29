import { toast } from "react-toastify";
import axiosInstance from "./axiosConfig";

/**
 * Register a new restaurant on the backend.
 * Expects a FormData instance containing:
 * - name, phoneNumber, ownerName
 * - categoryNames (multiple entries)
 * - region, postalCode, city, addressLine, streetNumber
 * - openingHours (stringified JSON)
 * - logo (file)
 *
 * Returns the created RestaurantDto.
 */
export async function registerRestaurant(formData) {
  try {
    console.log(formData);
    const response = await axiosInstance.post(
      "/restaurants/register",
      formData
    );
    // The backend wraps the DTO in an ApiResponse { message, data }
    return response.data.data;
  } catch (error) {
    // You can inspect error.response.data for details
    toast.error(
      error.response?.data?.message ||
        "An error occurred during registering restaurant!"
    );
    return null;
  }
}

export const verify = async (key, data) => {
  try {
    const response = await axiosInstance.post(
      `/restaurants/verify/account?key=` + key,
      data
    );
    return response;
  } catch (error) {
    toast.error(
      error.response?.data?.message || "An error occurred during verification!"
    );
  }
  return null;
};

export const fetchRestaurant = async (restaurantId) => {
  try {
    const response = await axiosInstance.get(`/restaurants/${restaurantId}`);
    return response.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during fetching restaurant!"
    );
    return null;
  }
};

export async function fetchRestaurants() {
  try {
    const res = await axiosInstance.get("/restaurants");
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during fetching restaurants!"
    );
    return null;
  }
}

/**
 * (Opcionális) Lekéri a kategórialistát.
 */
export async function fetchCategories() {
  try {
    const response = await axiosInstance.get("/restaurant-categories");
    return response.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during fetching categories!"
    );
    return null;
  }
}

/**
 * Lekéri a jóváhagyásra váró éttermeket.
 */
export async function getPendingRestaurants() {
  try {
    const res = await axiosInstance.get("/restaurants/pending");
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during fetching pending restaurants!"
    );
    return null;
  }
}

/**
 * Jóváhagy egy éttermet.
 * @param id étterem UUID
 */
export async function approveRestaurant(id) {
  try {
    const res = await axiosInstance.post(`/restaurants/${id}/approve`);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during approving restaurant!"
    );
    return null;
  }
}

/**
 * Elutasít egy éttermet.
 * @param id étterem UUID
 */
export async function rejectRestaurant(id) {
  try {
    const res = await axiosInstance.post(`/restaurants/${id}/reject`);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during rejecting restaurant!"
    );
    return null;
  }
}

export async function updateRestaurant(id, data) {
  try {
    const res = await axiosInstance.put(`/restaurants/${id}`, data);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during update of restaurant!"
    );
    return null;
  }
}
