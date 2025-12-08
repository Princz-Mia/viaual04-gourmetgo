import React, { useState, useEffect } from 'react';
import { FiX, FiClock, FiStar } from 'react-icons/fi';

const HappyHourModal = ({ isOpen, onClose, happyHour }) => {
  const [timeLeft, setTimeLeft] = useState('');

  useEffect(() => {
    if (happyHour && isOpen) {
      const timer = setInterval(updateCountdown, 1000);
      return () => clearInterval(timer);
    }
  }, [happyHour, isOpen]);

  const updateCountdown = () => {
    if (!happyHour) return;
    
    const now = new Date();
    const endTime = new Date(happyHour.endTime);
    const diff = endTime - now;
    
    if (diff <= 0) {
      onClose();
      return;
    }
    
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);
    
    setTimeLeft(`${hours}h ${minutes}m ${seconds}s`);
  };

  if (!isOpen || !happyHour) return null;

  const bonusPercentage = (happyHour.bonusRate * 100).toFixed(0);

  return (
    <div className="modal modal-open">
      <div className="modal-box max-w-md relative bg-gradient-to-br from-primary/10 to-secondary/10 border-2 border-primary/20">
        <button
          onClick={onClose}
          className="btn btn-sm btn-circle absolute right-2 top-2"
        >
          <FiX />
        </button>

        <div className="text-center space-y-4">
          <div className="text-6xl animate-bounce">🎉</div>
          
          <h2 className="text-2xl font-bold text-primary">{happyHour.title}</h2>
          
          <div className="bg-gradient-to-r from-primary to-secondary text-white p-4 rounded-lg">
            <div className="flex items-center justify-center space-x-2 mb-2">
              <FiStar className="text-xl" />
              <span className="text-xl font-bold">+{bonusPercentage}% Extra Points!</span>
            </div>
            <p className="text-sm opacity-90">On all orders during this event</p>
          </div>

          <p className="text-neutral/70">{happyHour.description}</p>

          <div className="bg-base-200 p-4 rounded-lg">
            <div className="flex items-center justify-center space-x-2 mb-2">
              <FiClock className="text-primary" />
              <span className="font-semibold">Time Remaining:</span>
            </div>
            <div className="text-2xl font-mono font-bold text-primary">{timeLeft}</div>
          </div>

          <button
            onClick={onClose}
            className="btn btn-primary btn-wide"
          >
            Start Ordering! 🚀
          </button>
        </div>
      </div>
    </div>
  );
};

export default HappyHourModal;