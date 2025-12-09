import { toast } from "react-toastify";
import { decodeJwt } from "../utils/jwt";
import axiosInstance from "./axiosConfig";

/**
 * Fetch user profile using cookie-based authentication
 */
export async function fetchProfile() {
  try {
    const { data } = await axiosInstance.get('/users/profile');
    return data.data;
  } catch (error) {
    throw new Error('Failed to fetch user profile');
  }
}

/**
 * Lekérdezi az összes felhasználót (Admin, Customer, RestaurantAdmin).
 */
export async function getUsers() {
  try {
    const res = await axiosInstance.get("/users");
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during fetching users!"
    );
    return null;
  }
}

/**
 * Lock/Unlock egy felhasználót.
 * @param id felhasználó UUID
 * @param locked új lock státusz (true = locked)
 */
export async function lockUser(id, locked) {
  try {
    const res = await axiosInstance.put(`/users/${id}/lock`, { locked });
    const action = locked ? 'locked' : 'unlocked';
    toast.success(`User successfully ${action}`);
    return res.data.data;
  } catch (error) {
    const errorMessage = error.response?.data?.message || 
      `Failed to ${locked ? 'lock' : 'unlock'} user. Please try again.`;
    toast.error(errorMessage);
    throw error;
  }
}

/**
 * Töröl egy felhasználót.
 * @param id felhasználó UUID
 */
export async function deleteUser(id) {
  try {
    const res = await axiosInstance.delete(`/users/${id}`);
    toast.success('User successfully deleted');
    return res.data.data;
  } catch (error) {
    const errorMessage = error.response?.data?.message || 
      'Failed to delete user. Please check if user has active orders or try again later.';
    toast.error(errorMessage);
    throw error;
  }
}

export async function requestPasswordReset(email) {
  try {
    // építsd form‐urlencoded body-t
    const params = new URLSearchParams();
    params.append("email", email);

    // Content-Type: application/x-www-form-urlencoded
    const res = await axiosInstance.post("/users/forgot-password", params);
    return res.data.message;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during requesting password reset!"
    );
    return null;
  }
}

export async function resetPassword(key, data) {
  try {
    const res = await axiosInstance.post(
      `/users/reset-password/password?key=` + key,
      data
    );
    return res.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during reseting password!"
    );
    return null;
  }
}

/**
 * Frissíti a felhasználó nevét és emailét.
 * @param {{ fullName: string, email: string }} data
 */
export async function updateProfile(data) {
  try {
    const res = await axiosInstance.put(`/users/profile`, data);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message || "An error occurred updating profile!"
    );
    throw error;
  }
}
