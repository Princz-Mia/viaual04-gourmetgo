import React, { useState, useEffect } from 'react';
import Hero from '../components/Hero'
import JoinUsSection from '../components/JoinUsSection'
import HappyHourModal from '../components/HappyHourModal';
import { promotionApi } from '../api/promotionApi';

const Home = () => {
  const [showHappyHourModal, setShowHappyHourModal] = useState(false);
  const [happyHour, setHappyHour] = useState(null);

  useEffect(() => {
    checkForActiveHappyHour();
    
    // Set up WebSocket for real-time updates - exact AdminDashboard pattern
    import('../services/websocketService').then(({ default: websocketService }) => {
      websocketService.connect().then(() => {
        websocketService.subscribe('/topic/happy-hour', (message) => {
          if (message.type === 'HAPPY_HOUR_UPDATE') {
            if (message.isActive && message.happyHour) {
              setHappyHour(message.happyHour);
              setShowHappyHourModal(true);
            } else {
              setHappyHour(null);
              setShowHappyHourModal(false);
            }
          }
        });
      }).catch(console.error);
    });
    
    return () => {
      // Cleanup handled by websocket service
    };
  }, []);

  const checkForActiveHappyHour = async () => {
    try {
      const response = await promotionApi.getActiveHappyHour();
      if (response.data) {
        setHappyHour(response.data);
        setShowHappyHourModal(true);
      }
    } catch (error) {
      // No active happy hour
    }
  };

  return (
    <div>
      <Hero />
      <JoinUsSection />
      <HappyHourModal 
        isOpen={showHappyHourModal}
        onClose={() => setShowHappyHourModal(false)}
        happyHour={happyHour}
      />
    </div>
  )
}

export default Home