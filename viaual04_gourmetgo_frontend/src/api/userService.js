import { toast } from "react-toastify";
import { decodeJwt } from "../utils/jwt";
import axiosInstance from "./axiosConfig";

/**
 * Fetch full profile by decoding the JWT for id+role,
 * then calling the appropriate endpoint.
 */
export async function fetchProfile() {
  const stored = JSON.parse(localStorage.getItem("user") || "{}");
  const { token } = stored;
  if (!token) throw new Error("No auth token found");

  const claims = decodeJwt(token);
  const id = claims.id;
  const role = Array.isArray(claims.role) ? claims.role[0] : claims.role;

  let url;
  switch (role.authority) {
    case "ROLE_CUSTOMER":
      url = `/customers/${id}`;
      break;
    case "ROLE_RESTAURANT":
      url = `/restaurants/${id}`;
      break;
    case "ROLE_ADMIN":
      url = `/admins/${id}`;
      break;
    default:
      throw new Error(`Unknown role: ${role}`);
  }

  const { data } = await axiosInstance.get(url);
  // include id, role, token in the returned object
  return { ...data.data, id, role, token };
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
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during locking user profile!"
    );
    return null;
  }
}

/**
 * Töröl egy felhasználót.
 * @param id felhasználó UUID
 */
export async function deleteUser(id) {
  try {
    const res = await axiosInstance.delete(`/users/${id}`);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message || "An error occurred during deleting user!"
    );
    return null;
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
