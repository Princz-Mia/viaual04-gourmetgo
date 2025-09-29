import axiosInstance from "./axiosConfig";
import { toast } from "react-toastify";

export async function fetchPaymentMethods() {
  try {
    const res = await axiosInstance.get("/payment");
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during fetching products!"
    );
    return null;
  }
}
