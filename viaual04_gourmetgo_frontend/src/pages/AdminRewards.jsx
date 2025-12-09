import React, { useState } from 'react';
import { FiGift, FiUsers, FiTrendingUp } from 'react-icons/fi';
import PromotionRewardModal from '../components/admin/PromotionRewardModal';
import PromotionManagement from '../components/admin/PromotionManagement';

const AdminRewards = () => {
  const [showPromotionModal, setShowPromotionModal] = useState(false);

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="mb-8 flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold mb-2">Reward Management</h1>
          <p className="text-neutral/60">Manage customer rewards and promotions</p>
        </div>
        <button 
          className="btn btn-outline btn-sm"
          onClick={() => window.location.reload()}
          title="Refresh data"
        >
          🔄 Refresh
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-base-100 p-6 rounded-lg border border-base-300">
          <div className="flex items-center space-x-3 mb-4">
            <FiGift className="text-primary text-2xl" />
            <h2 className="text-xl font-semibold">Send Promotions</h2>
          </div>
          <p className="text-neutral/60 mb-4">
            Send promotional reward points to specific customers or all active customers.
          </p>
          <button
            onClick={() => setShowPromotionModal(true)}
            className="btn btn-primary"
          >
            Send Promotion
          </button>
        </div>

        <div className="bg-base-100 p-6 rounded-lg border border-base-300">
          <div className="flex items-center space-x-3 mb-4">
            <FiUsers className="text-secondary text-2xl" />
            <h2 className="text-xl font-semibold">Order Compensation</h2>
          </div>
          <p className="text-neutral/60 mb-4">
            Compensation rewards are handled directly from order management pages when viewing specific orders.
          </p>
          <button
            onClick={() => window.location.href = '/company-management?tab=orders'}
            className="btn btn-outline"
          >
            Go to Orders
          </button>
        </div>

        <div className="bg-base-100 p-6 rounded-lg border border-base-300">
          <div className="flex items-center space-x-3 mb-4">
            <FiTrendingUp className="text-accent text-2xl" />
            <h2 className="text-xl font-semibold">Bonus Events</h2>
          </div>
          <p className="text-neutral/60 mb-4">
            Create Happy Hours and category-specific bonus events to boost engagement.
          </p>
        </div>
      </div>

      <div className="mt-8 bg-base-100 p-6 rounded-lg border border-base-300">
        <PromotionManagement />
      </div>

      <div className="mt-8 bg-base-100 p-6 rounded-lg border border-base-300">
        <h3 className="text-lg font-semibold mb-4">Reward System Rules</h3>
        <div className="space-y-2 text-sm text-neutral/70">
          <p>• Customers earn 3% reward points on delivered orders</p>
          <p>• Cancelled orders receive 100% refund as reward points</p>
          <p>• Compensation rewards cannot exceed the original order total</p>
          <p>• Customers can use points to reduce order costs (but not below $0)</p>
          <p>• All reward points must be positive values</p>
        </div>
      </div>

      <PromotionRewardModal
        isOpen={showPromotionModal}
        onClose={() => setShowPromotionModal(false)}
      />
    </div>
  );
};

export default AdminRewards;