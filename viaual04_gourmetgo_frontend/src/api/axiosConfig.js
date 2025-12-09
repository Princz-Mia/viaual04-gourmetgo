import axios from "axios";

const axiosInstance = axios.create({
  baseURL: "http://localhost:8080/api/v1",
  withCredentials: true,
});

// Function to get CSRF token from cookie
const getCsrfToken = () => {
  const cookies = document.cookie.split(';');
  for (let cookie of cookies) {
    const [name, value] = cookie.trim().split('=');
    if (name === 'XSRF-TOKEN' && value) {
      return value;
    }
  }
  return null;
};

// Add CSRF token to requests
axiosInstance.interceptors.request.use(
  (config) => {
    const csrfToken = getCsrfToken();
    if (csrfToken && ['post', 'put', 'delete', 'patch'].includes(config.method?.toLowerCase())) {
      config.headers['X-XSRF-TOKEN'] = csrfToken;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  
  failedQueue = [];
};

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    // Only try to refresh if this is an authenticated endpoint
    const isAuthEndpoint = originalRequest.url?.includes('/auth/');
    const isPublicEndpoint = originalRequest.url?.includes('/restaurants') ||
                            originalRequest.url?.includes('/promotions') ||
                            originalRequest.url?.includes('/products') ||
                            originalRequest.url?.includes('/reviews') ||
                            originalRequest.url?.includes('/menu') ||
                            originalRequest.url?.includes('/public/') ||
                            originalRequest.url?.includes('/customers/register');
    
    const isCartEndpoint = originalRequest.url?.includes('/carts') ||
                          originalRequest.url?.includes('/cartItems');
    
    // Skip token refresh for public endpoints or auth endpoints
    if (error.response?.status === 401 && !originalRequest._retry && !isAuthEndpoint && !isPublicEndpoint) {
      // For cart operations, redirect to login immediately
      if (isCartEndpoint) {
        window.location.href = '/login';
        return Promise.reject(error);
      }
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(() => {
          return axiosInstance(originalRequest);
        }).catch(err => {
          return Promise.reject(err);
        });
      }
      
      originalRequest._retry = true;
      isRefreshing = true;
      
      try {
        await axiosInstance.post('/auth/refresh');
        processQueue(null);
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }
    
    return Promise.reject(error);
  }
);

export default axiosInstance;
