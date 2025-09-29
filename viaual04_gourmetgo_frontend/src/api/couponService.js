import { toast } from "react-toastify";
import axiosInstance from "./axiosConfig";

/**
 * Lekéri az összes kupont.
 */
export async function getCoupons() {
  try {
    const res = await axiosInstance.get("/coupons");
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during updating order status!"
    );
    return null;
  }
}

/**
 * Új kupon létrehozása.
 * @param coupon { code, type, value, expirationDate }
 */
export async function createCoupon(coupon) {
  try {
    const res = await axiosInstance.post("/coupons", coupon);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during updating order status!"
    );
    return null;
  }
}

/**
 * Kupon módosítása.
 * @param coupon { id, code, type, value, expires }
 */
export async function updateCoupon(coupon) {
  try {
    const res = await axiosInstance.put(`/coupons/${coupon.id}`, coupon);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during updating order status!"
    );
    return null;
  }
}

/**
 * Kupon törlése.
 * @param id kupon UUID
 */
export async function deleteCoupon(id) {
  try {
    const res = await axiosInstance.delete(`/coupons/${id}`);
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during updating order status!"
    );
    return null;
  }
}

/**
 * Validál egy kupont a backend segítségével.
 * @param {string} code A kuponkód (pl. "SAVE10")
 * @returns {Promise<CouponDto|null>} A visszakapott CouponDto, vagy null hibánál
 */
export async function validateCoupon(code) {
  try {
    const res = await axiosInstance.get(`/coupons/validate/${code}`);
    return res.data.data;
  } catch (error) {
    toast.error(error.response?.data?.message || "Invalid or expired coupon!");
  }
  return null;
}
