import React, { useState, useEffect } from 'react';
import { promotionApi } from '../../api/promotionApi';
import { toast } from 'react-toastify';
import { FiPlus, FiClock, FiTag } from 'react-icons/fi';

const PromotionManagement = () => {
  const [happyHours, setHappyHours] = useState([]);
  const [categoryBonuses, setCategoryBonuses] = useState([]);
  const [showHappyHourModal, setShowHappyHourModal] = useState(false);
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [loading, setLoading] = useState(false);

  const [happyHourForm, setHappyHourForm] = useState({
    title: '',
    description: '',
    bonusRate: 0.02,
    startTime: '',
    endTime: ''
  });

  const [categoryForm, setCategoryForm] = useState({
    categoryName: '',
    bonusRate: 0.01,
    startTime: '',
    endTime: '',
    description: ''
  });

  useEffect(() => {
    fetchPromotions();
  }, []);

  const fetchPromotions = async () => {
    try {
      const [happyHourRes, categoryRes] = await Promise.all([
        promotionApi.getAllHappyHours(),
        promotionApi.getAllCategoryBonuses()
      ]);
      setHappyHours(happyHourRes.data);
      setCategoryBonuses(categoryRes.data);
    } catch (error) {
      toast.error('Failed to fetch promotions');
    }
  };

  const handleCreateHappyHour = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await promotionApi.createHappyHour(happyHourForm);
      toast.success('Happy Hour created successfully!');
      setShowHappyHourModal(false);
      setHappyHourForm({ title: '', description: '', bonusRate: 0.02, startTime: '', endTime: '' });
      fetchPromotions();
    } catch (error) {
      toast.error('Failed to create Happy Hour');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateCategoryBonus = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await promotionApi.createCategoryBonus(categoryForm);
      toast.success('Category bonus created successfully!');
      setShowCategoryModal(false);
      setCategoryForm({ categoryName: '', bonusRate: 0.01, startTime: '', endTime: '', description: '' });
      fetchPromotions();
    } catch (error) {
      toast.error('Failed to create category bonus');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteHappyHour = async (id) => {
    if (window.confirm('Are you sure you want to delete this Happy Hour?')) {
      try {
        await promotionApi.deleteHappyHour(id);
        toast.success('Happy Hour deleted successfully!');
        fetchPromotions();
      } catch (error) {
        toast.error('Failed to delete Happy Hour');
      }
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold">Promotion Management</h2>
        <div className="space-x-2">
          <button
            onClick={() => setShowHappyHourModal(true)}
            className="btn btn-primary btn-sm"
          >
            <FiPlus /> Happy Hour
          </button>
          <button
            onClick={() => setShowCategoryModal(true)}
            className="btn btn-secondary btn-sm"
          >
            <FiTag /> Category Bonus
          </button>
          <button
            onClick={fetchPromotions}
            className="btn btn-primary btn-sm"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            Refresh
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Happy Hours */}
        <div className="bg-base-100 p-4 rounded-lg">
          <h3 className="font-semibold mb-3 flex items-center">
            <FiClock className="mr-2" /> Happy Hours
          </h3>
          <div className="space-y-2">
            {happyHours.map(hh => (
              <div key={hh.id} className="p-3 bg-base-200 rounded flex justify-between items-start">
                <div className="flex-1">
                  <div className="font-medium">{hh.title}</div>
                  <div className="text-sm text-neutral/60">
                    +{(hh.bonusRate * 100).toFixed(0)}% bonus
                  </div>
                  <div className="text-xs">
                    {new Date(hh.startTime).toLocaleString()} - {new Date(hh.endTime).toLocaleString()}
                  </div>
                </div>
                <button
                  onClick={() => handleDeleteHappyHour(hh.id)}
                  className="btn btn-error btn-xs ml-2"
                  title="Delete Happy Hour"
                >
                  ×
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Category Bonuses */}
        <div className="bg-base-100 p-4 rounded-lg">
          <h3 className="font-semibold mb-3 flex items-center">
            <FiTag className="mr-2" /> Category Bonuses
          </h3>
          <div className="space-y-2">
            {categoryBonuses.map(cb => (
              <div key={cb.id} className="p-3 bg-base-200 rounded">
                <div className="font-medium">{cb.categoryName}</div>
                <div className="text-sm text-neutral/60">
                  +{(cb.bonusRate * 100).toFixed(0)}% bonus
                </div>
                <div className="text-xs">
                  {new Date(cb.startTime).toLocaleString()} - {new Date(cb.endTime).toLocaleString()}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Happy Hour Modal */}
      {showHappyHourModal && (
        <div className="modal modal-open">
          <div className="modal-box">
            <h3 className="font-bold text-lg mb-4">Create Happy Hour</h3>
            <form onSubmit={handleCreateHappyHour} className="space-y-4">
              <div>
                <label className="label">
                  <span className="label-text font-medium">Title *</span>
                </label>
                <input
                  type="text"
                  placeholder="Weekend Happy Hour"
                  value={happyHourForm.title}
                  onChange={(e) => setHappyHourForm({...happyHourForm, title: e.target.value})}
                  className="input input-bordered w-full"
                  required
                />
              </div>
              <div>
                <label className="label">
                  <span className="label-text font-medium">Description</span>
                </label>
                <textarea
                  placeholder="Enjoy extra rewards during our happy hour!"
                  value={happyHourForm.description}
                  onChange={(e) => setHappyHourForm({...happyHourForm, description: e.target.value})}
                  className="textarea textarea-bordered w-full"
                />
              </div>
              <div>
                <label className="label">
                  <span className="label-text font-medium">Bonus Rate (%) *</span>
                  <span className="label-text-alt text-neutral/60">Enter as whole number (e.g., 5 for 5%)</span>
                </label>
                <input
                  type="number"
                  step="1"
                  min="1"
                  max="10"
                  placeholder="5"
                  value={happyHourForm.bonusRate * 100}
                  onChange={(e) => setHappyHourForm({...happyHourForm, bonusRate: parseFloat(e.target.value) / 100})}
                  className="input input-bordered w-full"
                  required
                />
              </div>
              <div>
                <label className="label">
                  <span className="label-text font-medium">Start Date & Time *</span>
                </label>
                <input
                  type="datetime-local"
                  value={happyHourForm.startTime}
                  onChange={(e) => setHappyHourForm({...happyHourForm, startTime: e.target.value})}
                  className="input input-bordered w-full"
                  required
                />
              </div>
              <div>
                <label className="label">
                  <span className="label-text font-medium">End Date & Time *</span>
                </label>
                <input
                  type="datetime-local"
                  value={happyHourForm.endTime}
                  onChange={(e) => setHappyHourForm({...happyHourForm, endTime: e.target.value})}
                  className="input input-bordered w-full"
                  required
                />
              </div>
              <div className="modal-action">
                <button type="button" onClick={() => setShowHappyHourModal(false)} className="btn">Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? 'Creating...' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Category Bonus Modal */}
      {showCategoryModal && (
        <div className="modal modal-open">
          <div className="modal-box">
            <h3 className="font-bold text-lg mb-4">Create Category Bonus</h3>
            <form onSubmit={handleCreateCategoryBonus} className="space-y-4">
              <div>
                <label className="label">
                  <span className="label-text font-medium">Category Name *</span>
                </label>
                <input
                  type="text"
                  placeholder="Pizza"
                  value={categoryForm.categoryName}
                  onChange={(e) => setCategoryForm({...categoryForm, categoryName: e.target.value})}
                  className="input input-bordered w-full"
                  required
                />
              </div>
              <div>
                <label className="label">
                  <span className="label-text font-medium">Bonus Rate (%) *</span>
                  <span className="label-text-alt text-neutral/60">Enter as whole number (e.g., 3 for 3%)</span>
                </label>
                <input
                  type="number"
                  step="1"
                  min="1"
                  max="5"
                  placeholder="3"
                  value={categoryForm.bonusRate * 100}
                  onChange={(e) => setCategoryForm({...categoryForm, bonusRate: parseFloat(e.target.value) / 100})}
                  className="input input-bordered w-full"
                  required
                />
              </div>
              <div>
                <label className="label">
                  <span className="label-text font-medium">Start Date & Time *</span>
                </label>
                <input
                  type="datetime-local"
                  value={categoryForm.startTime}
                  onChange={(e) => setCategoryForm({...categoryForm, startTime: e.target.value})}
                  className="input input-bordered w-full"
                  required
                />
              </div>
              <div>
                <label className="label">
                  <span className="label-text font-medium">End Date & Time *</span>
                </label>
                <input
                  type="datetime-local"
                  value={categoryForm.endTime}
                  onChange={(e) => setCategoryForm({...categoryForm, endTime: e.target.value})}
                  className="input input-bordered w-full"
                  required
                />
              </div>
              <div>
                <label className="label">
                  <span className="label-text font-medium">Description</span>
                </label>
                <textarea
                  placeholder="Special bonus for this category"
                  value={categoryForm.description}
                  onChange={(e) => setCategoryForm({...categoryForm, description: e.target.value})}
                  className="textarea textarea-bordered w-full"
                />
              </div>
              <div className="modal-action">
                <button type="button" onClick={() => setShowCategoryModal(false)} className="btn">Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? 'Creating...' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default PromotionManagement;