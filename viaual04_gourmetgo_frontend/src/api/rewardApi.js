import api from './axiosConfig';

export const rewardApi = {
  getRewardBalance: (customerId) => 
    api.get(`/rewards/balance/${customerId}`),
  
  getRewardHistory: (customerId) => 
    api.get(`/rewards/history/${customerId}`),
  
  addCompensationReward: (customerId, points, description, orderId) => 
    api.post('/rewards/compensation', null, {
      params: { customerId, points, description, orderId }
    }),
  
  addPromotionReward: (customerId, points, description) => 
    api.post('/rewards/promotion', null, {
      params: { customerId, points, description }
    }),
  
  addPromotionRewardToAll: (points, description) => 
    api.post('/rewards/promotion/all', null, {
      params: { points, description }
    })
};