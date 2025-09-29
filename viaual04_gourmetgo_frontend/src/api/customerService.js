import { toast } from "react-toastify";
import axiosInstance from "./axiosConfig";

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
