import axios from "axios";
import { getUser } from "./authService";

const axiosInstance = axios.create({
  baseURL: "http://localhost:8080/api/v1",
});

axiosInstance.defaults.withCredentials = true;

axiosInstance.interceptors.request.use(
  (config) => {
    const user = getUser();
    const token = user?.token;
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default axiosInstance;
