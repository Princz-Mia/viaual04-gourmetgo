import React, { useState } from 'react';
import { rewardApi } from '../../api/rewardApi';
import { customerService } from '../../api/customerService';
import { toast } from 'react-toastify';

const PromotionRewardModal = ({ isOpen, onClose }) => {
  const [targetType, setTargetType] = useState('email'); // 'email' or 'all'
  const [email, setEmail] = useState('');
  const [points, setPoints] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!points || parseFloat(points) <= 0) {
      toast.error('Points must be positive');
      return;
    }

    if (targetType === 'email' && !email) {
      toast.error('Email is required');
      return;
    }

    setLoading(true);
    try {
      if (targetType === 'email') {
        // Find customer by email first
        const customer = await customerService.findByEmail(email);
        await rewardApi.addPromotionReward(
          customer.id,
          parseFloat(points),
          description || 'Promotional reward'
        );
        toast.success('Promotion reward sent successfully');
      } else {
        // Send to all active customers
        await rewardApi.addPromotionRewardToAll(
          parseFloat(points),
          description || 'Promotional reward for all customers'
        );
        toast.success('Promotion rewards sent to all customers');
      }
      
      onClose();
      setEmail('');
      setPoints('');
      setDescription('');
    } catch (error) {
      toast.error('Failed to send promotion reward');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal modal-open">
      <div className="modal-box">
        <h3 className="font-bold text-lg mb-4">Send Promotion Reward</h3>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label">
              <span className="label-text">Target *</span>
            </label>
            <select
              value={targetType}
              onChange={(e) => setTargetType(e.target.value)}
              className="select select-bordered w-full"
            >
              <option value="email">Specific Customer (Email)</option>
              <option value="all">All Active Customers</option>
            </select>
          </div>

          {targetType === 'email' && (
            <div>
              <label className="label">
                <span className="label-text">Customer Email *</span>
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="input input-bordered w-full"
                placeholder="customer@example.com"
                required
              />
            </div>
          )}

          <div>
            <label className="label">
              <span className="label-text font-medium">Reward Points *</span>
            </label>
            <input
              type="number"
              step="1"
              min="1"
              value={points}
              onChange={(e) => setPoints(e.target.value)}
              className="input input-bordered w-full"
              placeholder="100 (= $10.00)"
              required
            />
            <div className="text-xs text-gray-500 mt-1">
              10 points = $1.00
            </div>
          </div>

          <div>
            <label className="label">
              <span className="label-text">Description *</span>
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="textarea textarea-bordered w-full"
              placeholder="Promotion description"
              required
            />
          </div>

          <div className="modal-action">
            <button
              type="button"
              onClick={onClose}
              className="btn btn-ghost"
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? 'Sending...' : 'Send Reward'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default PromotionRewardModal;