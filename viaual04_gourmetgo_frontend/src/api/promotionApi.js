import api from './axiosConfig';

export const promotionApi = {
  getActiveHappyHour: () => 
    api.get('/promotions/happy-hour/active'),
  
  getActiveCategoryBonuses: () => 
    api.get('/promotions/category-bonuses/active'),
  
  createHappyHour: (happyHour) => 
    api.post('/promotions/happy-hour', happyHour),
  
  createCategoryBonus: (categoryBonus) => 
    api.post('/promotions/category-bonus', categoryBonus),
  
  getAllHappyHours: () => 
    api.get('/promotions/happy-hours'),
  
  getAllCategoryBonuses: () => 
    api.get('/promotions/category-bonuses'),
  
  deleteHappyHour: (id) => 
    api.delete(`/promotions/happy-hour/${id}`)
};