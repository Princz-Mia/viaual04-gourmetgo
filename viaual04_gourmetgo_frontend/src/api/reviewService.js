import { toast } from "react-toastify";
import axiosInstance from "./axiosConfig";

export async function fetchReviewsByRestaurant(restaurantId) {
  try {
    const res = await axiosInstance.get(
      `/reviews/by-restaurant/${restaurantId}`
    );
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message ||
        "An error occurred during fetching reviews!"
    );
  }
  return null;
}

export async function canReviewRestaurant(restaurantId) {
  try {
    await axiosInstance.get(`/orders/has-ordered/${restaurantId}`);
    return true;
  } catch {
    return false;
  }
}

export async function addReview(restaurantId, rating, comment) {
  try {
    const res = await axiosInstance.post(`/reviews/add`, null, {
      params: { restaurantId: restaurantId, rating, comment },
    });
    return res.data.data;
  } catch (error) {
    toast.error(
      error.response?.data?.message || "An error occurred during adding review!"
    );
  }
  return null;
}

export async function deleteReview(restaurantId) {
  try {
    await axiosInstance.delete(`/reviews/delete/${restaurantId}`);
    return true;
  } catch (error) {
    toast.error(
      error.response?.data?.message || "An error occurred during deleting review!"
    );
    return false;
  }
}

export async function getMyReview(restaurantId) {
  try {
    const res = await axiosInstance.get(`/reviews/my-review/${restaurantId}`);
    return res.data.data;
  } catch (error) {
    return null;
  }
}
