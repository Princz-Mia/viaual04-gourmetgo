import React, { useState, useEffect, useContext, forwardRef, useImperativeHandle } from 'react';
import { AuthContext } from '../contexts/AuthContext';
import { rewardApi } from '../api/rewardApi';
import { FiGift } from 'react-icons/fi';

const RewardBalance = forwardRef((props, ref) => {
  const { user } = useContext(AuthContext);
  const [balance, setBalance] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.id) {
      fetchBalance();
    }
  }, [user]);

  const fetchBalance = async () => {
    try {
      const response = await rewardApi.getRewardBalance(user.id);
      const data = response.data?.data || response.data || {};
      setBalance(data.balance || 0);
    } catch (error) {
      console.error('Error fetching reward balance:', error);
      setBalance(0); // Fallback to 0 if error
    } finally {
      setLoading(false);
    }
  };

  useImperativeHandle(ref, () => ({
    refresh: fetchBalance
  }));

  if (loading) {
    return <div className="loading loading-spinner loading-sm"></div>;
  }

  const userRole = user?.role?.authority || user?.role;
  if (!user || userRole !== 'ROLE_CUSTOMER') {
    return null;
  }

  return (
    <div className="flex items-center space-x-2 bg-accent/10 px-3 py-2 rounded-full">
      <FiGift className="text-primary" />
      <span className="text-sm font-medium">{balance.toFixed(2)} pts</span>
    </div>
  );
});

export default RewardBalance;