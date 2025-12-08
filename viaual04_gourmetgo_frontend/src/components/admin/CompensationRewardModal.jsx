import React, { useState } from 'react';
import { rewardApi } from '../../api/rewardApi';
import { toast } from 'react-toastify';

const CompensationRewardModal = ({ isOpen, onClose, order }) => {
  const [points, setPoints] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!points || parseFloat(points) <= 0) {
      toast.error('Points must be positive');
      return;
    }
    
    if (!['CANCELLED', 'DELIVERED'].includes(order.status)) {
      toast.error('Only cancelled or delivered orders can be compensated');
      return;
    }
    
    const pointsValue = parseFloat(points);

    setLoading(true);
    try {
      if (!order.customerId) {
        toast.error('Customer ID not found');
        return;
      }
      
      await rewardApi.addCompensationReward(
        order.customerId, 
        pointsValue, 
        description || `Compensation for order #${order.id}`,
        order.id
      );
      toast.success('Compensation reward added successfully');
      onClose();
      setPoints('');
      setDescription('');
    } catch (error) {
      toast.error('Failed to add compensation reward');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen || !order) return null;

  return (
    <div className="modal modal-open">
      <div className="modal-box">
        <h3 className="font-bold text-lg mb-4">Add Compensation Reward</h3>
        
        <div className="mb-4">
          <p><strong>Order:</strong> #{order.id}</p>
          <p><strong>Customer:</strong> {order.billingInformation?.fullName || 'Unknown'}</p>
          <p><strong>Order Total:</strong> ${order.totalAmount?.toFixed(2) || '0.00'}</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
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
              placeholder="50 (= $5.00)"
              required
            />
            <div className="text-xs text-gray-500 mt-1">
              10 points = $1.00
            </div>
          </div>

          <div>
            <label className="label">
              <span className="label-text">Description</span>
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="textarea textarea-bordered w-full"
              placeholder="Reason for compensation (optional)"
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
              {loading ? 'Adding...' : 'Add Reward'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CompensationRewardModal;