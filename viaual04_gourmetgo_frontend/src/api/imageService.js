import axiosInstance from './axiosConfig';

export const uploadRestaurantImage = async (file, restaurantId) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('restaurantId', restaurantId);
  
  const response = await axiosInstance.post('/images/restaurants/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  return response.data;
};

export const uploadProductImage = async (file, productId) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('productId', productId);
  
  const response = await axiosInstance.post('/images/products/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  return response.data;
};

export const updateImage = async (file, imageId) => {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await axiosInstance.put(`/images/${imageId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  return response.data;
};

export const getImageUrl = (imageId) => {
  return `${axiosInstance.defaults.baseURL}/images/download/${imageId}`;
};