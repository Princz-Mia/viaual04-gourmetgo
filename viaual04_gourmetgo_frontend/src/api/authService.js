import axiosInstance from "./axiosConfig";
import { toast } from "react-toastify";

export const getUser = () =>
  localStorage.getItem("user")
    ? JSON.parse(localStorage.getItem("user"))
    : null;

export const login = async (loginRequest) => {
  try {
    const response = await axiosInstance.post("/auth/login", {
      emailAddress: loginRequest.email,
      password: loginRequest.password,
    });
    localStorage.setItem("user", JSON.stringify(response.data.data));
    return response;
  } catch (error) {
    toast.error(
      error.response?.data?.message || "An error occurred during login!"
    );
    return null;
  }
};

export const logout = () => {
  localStorage.removeItem("user");
};

export const registerCustomer = async (registerRequest) => {
  try {
    const response = await axiosInstance.post("/customers/register", {
      fullName: registerRequest.fullName,
      emailAddress: registerRequest.email,
      password: registerRequest.password,
    });
    console.log(response.data);
    return response;
  } catch (error) {
    toast.error(
      error.response?.data?.message || "An error occurred during registration!"
    );
  }
  return null;
};

export const verify = async (key) => {
  try {
    const response = await axiosInstance.get(
      `/customers/verify/account?key=` + key
    );
    return response;
  } catch (error) {
    toast.error(
      error.response?.data?.message || "An error occurred during verification!"
    );
  }
  return null;
};
