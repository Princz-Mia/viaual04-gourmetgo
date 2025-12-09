import { toast } from "react-toastify";
import axiosInstance from "./axiosConfig";

export const verify = async (key) => {
  try {
    const response = await axiosInstance.get(
      `/customers/verify/account?key=` + key
    );
    return response.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message || "An error occurred during verification!"
    );
    throw error;
  }
};

export const findByEmail = async (email) => {
  try {
    const response = await axiosInstance.get(`/customers/email/${email}`);
    return response.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message || "Customer not found!"
    );
    throw error;
  }
};

export const customerService = {
  verify,
  findByEmail
};
