import React, { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../contexts/AuthContext';
import { rewardApi } from '../api/rewardApi';

const RewardRedemption = ({ orderTotal, onPointsChange }) => {
  const { user } = useContext(AuthContext);
  const [balance, setBalance] = useState(0);
  const [pointsToUse, setPointsToUse] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.id) {
      fetchBalance();
    }
  }, [user]);

  const fetchBalance = async () => {
    try {
      const response = await rewardApi.getRewardBalance(user.id);
      setBalance(response.data.balance);
    } catch (error) {
      console.error('Error fetching reward balance:', error);
    } finally {
      setLoading(false);
    }
  };

  const handlePointsChange = (value) => {
    setPointsToUse(value);
    onPointsChange(value);
  };

  const useMaxPoints = () => {
    const maxUsableDollars = Math.min(balance / 10, orderTotal);
    const maxUsablePoints = maxUsableDollars * 10;
    handlePointsChange(maxUsablePoints);
  };

  if (loading || balance <= 0) {
    return null;
  }

  const maxUsableDollars = Math.min(balance / 10, orderTotal);
  const maxUsable = maxUsableDollars * 10;

  return (
    <div>
      <div className="flex items-center justify-between mb-2">
        <label className="block font-medium">Use Reward Points</label>
        <span className="text-sm text-neutral/60">Available: {balance.toFixed(2)} pts</span>
      </div>
      
      <div className="flex gap-2">
        <input
          type="number"
          min="0"
          max={maxUsable}
          step="10"
          value={pointsToUse || ''}
          onChange={(e) => {
            const value = e.target.value === '' ? 0 : parseInt(e.target.value) || 0;
            handlePointsChange(value);
          }}
          className="input input-bordered flex-1"
          placeholder="Points to use (min 25 pts = $2.50)"
        />
        <button
          type="button"
          onClick={useMaxPoints}
          className="btn btn-outline"
        >
          Use Max
        </button>
      </div>
      
      {pointsToUse > 0 && (
        <div className="mt-2 text-success">
          Discount: ${(pointsToUse / 10).toFixed(2)} ({pointsToUse} points)
        </div>
      )}
      
      <div className="mt-2 text-xs text-neutral/60">
        Minimum redemption: $2.50 (25 points) • 10 points = $1.00
      </div>
    </div>
  );
};

export default RewardRedemption;