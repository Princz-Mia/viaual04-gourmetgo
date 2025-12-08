import React, { useState, useEffect } from 'react';
import { promotionApi } from '../api/promotionApi';
import { FiClock, FiGift } from 'react-icons/fi';

const HappyHourBanner = ({ onModalOpen, happyHour }) => {
  const [timeLeft, setTimeLeft] = useState('');

  useEffect(() => {
    console.log('HappyHourBanner: happyHour prop changed:', happyHour);
    if (happyHour) {
      const timer = setInterval(updateCountdown, 1000);
      return () => clearInterval(timer);
    }
  }, [happyHour]);

  const updateCountdown = () => {
    if (!happyHour) return;
    
    const now = new Date();
    const endTime = new Date(happyHour.endTime);
    const diff = endTime - now;
    
    if (diff <= 0) {
      return;
    }
    
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((diff % (1000 * 60)) / 1000);
    
    setTimeLeft(`${hours}h ${minutes}m ${seconds}s`);
  };

  if (!happyHour) return null;

  return (
    <div 
      className="bg-gradient-to-r from-primary to-secondary text-white px-4 py-2 cursor-pointer hover:opacity-90 transition-opacity"
      onClick={() => onModalOpen(happyHour)}
    >
      <div className="max-w-6xl mx-auto flex items-center justify-center space-x-4">
        <FiGift className="text-xl animate-bounce" />
        <span className="font-medium">{happyHour.title}</span>
        <div className="flex items-center space-x-1">
          <FiClock className="text-sm" />
          <span className="text-sm font-mono">{timeLeft}</span>
        </div>
      </div>
    </div>
  );
};

export default HappyHourBanner;