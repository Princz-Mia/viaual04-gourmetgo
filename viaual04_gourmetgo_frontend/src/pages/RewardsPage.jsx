import React from 'react';
import RewardBalance from '../components/RewardBalance';
import RewardHistory from '../components/RewardHistory';

const RewardsPage = () => {
  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">My Rewards</h1>
        <p className="text-neutral/60">Earn points with every order and use them for discounts!</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-1">
          <div className="bg-base-100 p-6 rounded-lg border border-base-300">
            <h2 className="text-xl font-semibold mb-4">Current Balance</h2>
            <RewardBalance />
            
            <div className="mt-6 space-y-2 text-sm text-neutral/60">
              <p>• Earn 3% points on delivered orders</p>
              <p>• Get full refund as points for cancelled orders</p>
              <p>• Use points to reduce order costs</p>
            </div>
          </div>
        </div>

        <div className="lg:col-span-2">
          <div className="bg-base-100 p-6 rounded-lg border border-base-300">
            <RewardHistory />
          </div>
        </div>
      </div>
    </div>
  );
};

export default RewardsPage;