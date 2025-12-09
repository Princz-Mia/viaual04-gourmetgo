import React, { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../contexts/AuthContext';
import { rewardApi } from '../api/rewardApi';
import { FiPlus, FiMinus } from 'react-icons/fi';

const RewardHistory = () => {
  const { user } = useContext(AuthContext);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.id) {
      fetchHistory();
    }
  }, [user]);

  const fetchHistory = async () => {
    try {
      const response = await rewardApi.getRewardHistory(user.id);
      const data = response.data?.data || response.data || [];
      setHistory(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error fetching reward history:', error);
      setHistory([]);
    } finally {
      setLoading(false);
    }
  };

  const getTransactionIcon = (type, points) => {
    if (points > 0) {
      return <FiPlus className="text-success" />;
    }
    return <FiMinus className="text-error" />;
  };

  const getTransactionColor = (points) => {
    return points > 0 ? 'text-success' : 'text-error';
  };

  if (loading) {
    return <div className="loading loading-spinner"></div>;
  }

  return (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold">Reward History</h3>
      
      {!Array.isArray(history) || history.length === 0 ? (
        <p className="text-neutral/60">No reward transactions yet.</p>
      ) : (
        <div className="space-y-2">
          {history.map((transaction) => (
            <div key={transaction.id} className="flex items-center justify-between p-3 bg-base-200 rounded-lg">
              <div className="flex items-center space-x-3">
                {getTransactionIcon(transaction.type, transaction.points)}
                <div>
                  <p className="font-medium">{transaction.description}</p>
                  <p className="text-sm text-neutral/60">
                    {new Date(transaction.createdAt).toLocaleDateString()}
                  </p>
                </div>
              </div>
              <span className={`font-semibold ${getTransactionColor(transaction.points)}`}>
                {transaction.points > 0 ? '+' : ''}{transaction.points.toFixed(2)} pts
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default RewardHistory;