import axios from "axios";
import axiosInstance from "./axiosConfig";
import { toast } from "react-toastify";

let csrfToken = null;

export const getCsrfToken = async () => {
  if (!csrfToken) {
    try {
      const response = await axiosInstance.get("/auth/csrf");
      csrfToken = response.data.token;
      axiosInstance.defaults.headers.common['X-CSRF-TOKEN'] = csrfToken;
    } catch (error) {
      console.error("Failed to get CSRF token", error);
    }
  }
  return csrfToken;
};

export const login = async (loginRequest) => {
  try {
    const response = await axiosInstance.post("/auth/login", {
      emailAddress: loginRequest.email,
      password: loginRequest.password,
    });
    // Cookies are httpOnly, so they won't appear in document.cookie
    const { id } = response.data.data;
    return { id };
  } catch (error) {
    // Commented out login attempt tracking as it's not working properly
    // TODO: Implement proper login attempt limiting and tracking
    toast.error(
      error.response?.data?.message || "An error occurred during login!"
    );
    return null;
  }
};

export const logout = async () => {
  try {
    // Create a separate axios instance without CSRF for logout
    const logoutAxios = axios.create({
      baseURL: "http://localhost:8080/api/v1",
      withCredentials: true,
    });
    await logoutAxios.post("/auth/logout");
    csrfToken = null;
    delete axiosInstance.defaults.headers.common['X-CSRF-TOKEN'];
  } catch (error) {
    console.error("Logout error:", error);
  }
};

export const refreshToken = async () => {
  try {
    await axiosInstance.post("/auth/refresh");
    return true;
  } catch (error) {
    return false;
  }
};

// Deprecated: For backward compatibility only
// User data is now fetched from server, not stored locally
export const getUser = () => {
  console.warn('getUser() is deprecated. Use AuthContext or fetch user profile from server.');
  return null;
};

export const registerCustomer = async (registerRequest) => {
  try {
    await getCsrfToken();
    const response = await axiosInstance.post("/customers/register", {
      fullName: registerRequest.fullName,
      emailAddress: registerRequest.email,
      password: registerRequest.password,
    });
    return response;
  } catch (error) {
    toast.error(
      error.response?.data?.message || "An error occurred during registration!"
    );
  }
  return null;
};
