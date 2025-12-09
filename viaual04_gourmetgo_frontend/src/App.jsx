import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import Footer from './components/Footer';
import websocketService from './services/websocketService';
import Login from './pages/Login';
import Register from './pages/Register';
import Home from './pages/Home';
import CustomerVerification from './pages/CustomerVerification';
import Restaurants from './pages/Restaurants';
import Restaurant from './pages/Restaurant';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import Orders from './pages/Orders';
import RestaurantManagement from './pages/RestaurantManagment';
import Order from './pages/Order';
import CompanyManagment from './pages/CompanyManagment';
import { PublicRoute } from './routes/PublicRoute';
import { PrivateRoute } from './routes/PrivateRoute';
import Profile from './pages/Profile';
import RestaurantRegister from './pages/RestaurantRegister';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import RestaurantVerification from './pages/RestaurantVerification';
import PrivacyPolicy from './pages/PrivacyPolicy';
import TermsOfService from './pages/TermsOfService';
import ContactUs from './pages/ContactUs';
import AdminChat from './pages/AdminChat';
import ChatTest from './pages/ChatTest';
import RewardsPage from './pages/RewardsPage';
import AdminDashboard from './pages/AdminDashboard';
import RestaurantDashboard from './pages/RestaurantDashboard';
import SystemHealth from './pages/SystemHealth';
import UserAnalytics from './pages/UserAnalytics';
import BusinessInsights from './pages/BusinessInsights';
import RestaurantMenuAnalytics from './pages/RestaurantMenuAnalytics';
import RestaurantCustomerAnalytics from './pages/RestaurantCustomerAnalytics';
import RestaurantBusinessTips from './pages/RestaurantBusinessTips';
import HappyHourBanner from './components/HappyHourBanner';
import HappyHourModal from './components/HappyHourModal';
import CustomerChatWidget from './components/chat/CustomerChatWidget';
import MobileNav from './components/MobileNav';

const App = () => {
  const [showHappyHourModal, setShowHappyHourModal] = useState(false);
  const [selectedHappyHour, setSelectedHappyHour] = useState(null);
  const [bannerHappyHour, setBannerHappyHour] = useState(null);

  useEffect(() => {
    console.log('App: useEffect running, setting up WebSocket');
    // Fetch initial Happy Hour state
    import('./api/promotionApi').then(({ promotionApi }) => {
      promotionApi.getActiveHappyHour().then(response => {
        console.log('App: Initial Happy Hour fetch:', response.data);
        setBannerHappyHour(response.data);
      }).catch(() => setBannerHappyHour(null));
    });

    // Connect to WebSocket and listen for Happy Hour updates
    const connectWebSocket = () => {
      setTimeout(() => {
        console.log('App: Attempting WebSocket connection');
        if (websocketService.isConnected()) {
          console.log('App: WebSocket already connected, subscribing to Happy Hour updates');
          websocketService.subscribe('/topic/happy-hour', (message) => {
            console.log('App: Received Happy Hour WebSocket message:', message);
            if (message.type === 'HAPPY_HOUR_UPDATE') {
              const happyHour = message.isActive ? message.happyHour : null;
              console.log('App: Setting banner happy hour:', happyHour);
              setBannerHappyHour(happyHour);
              
              if (message.isActive && message.happyHour) {
                console.log('App: Showing modal for new Happy Hour');
                setSelectedHappyHour(message.happyHour);
                setShowHappyHourModal(true);
              }
            }
          });
        } else {
          websocketService.connect().then(() => {
            console.log('App: WebSocket connected, subscribing to Happy Hour updates');
            websocketService.subscribe('/topic/happy-hour', (message) => {
              console.log('App: Received Happy Hour WebSocket message:', message);
              if (message.type === 'HAPPY_HOUR_UPDATE') {
                const happyHour = message.isActive ? message.happyHour : null;
                console.log('App: Setting banner happy hour:', happyHour);
                setBannerHappyHour(happyHour);
                
                if (message.isActive && message.happyHour) {
                  console.log('App: Showing modal for new Happy Hour');
                  setSelectedHappyHour(message.happyHour);
                  setShowHappyHourModal(true);
                }
              }
            });
          }).catch(err => {
            console.error('App: WebSocket connection failed:', err);
          });
        }
      }, 1000);
    };
    
    connectWebSocket();
  }, []);

  const handleHappyHourClick = (happyHour) => {
    setSelectedHappyHour(happyHour);
    setShowHappyHourModal(true);
  };

  return (
    <Router>
      <div className="flex flex-col min-h-screen">
        <HappyHourBanner 
          onModalOpen={handleHappyHourClick} 
          happyHour={bannerHappyHour}
        />
        <Header />
        <main className="flex-grow bg-base-200 min-h-0">
          <Routes>
            <Route
              path="/"
              element={
                <Home />
              }
            />

            <Route path="/privacy-policy" element={<PrivacyPolicy />} />
            <Route path="/terms-of-service" element={<TermsOfService />} />
            <Route path="/contact" element={<ContactUs />} />

            <Route
              path='/restaurants'
              element={
                <Restaurants />
              }
            />

            <Route path="/restaurants/search" element={<Restaurants />} />

            <Route
              path='/restaurant/:restaurantId'
              element={
                <Restaurant />
              }
            />

            <Route
              path="/restaurant-registration"
              element={
                <RestaurantRegister />
              }
            />

            <Route
              path="/login"
              element={
                <PublicRoute>
                  <Login />
                </PublicRoute>
              }
            />

            <Route
              path="/registration"
              element={
                <PublicRoute>
                  <Register />
                </PublicRoute>
              }
            />

            <Route
              path="/verify/customer"
              element={
                <PublicRoute>
                  <CustomerVerification />
                </PublicRoute>
              }
            />

            <Route
              path="/verify/restaurant"
              element={
                <PublicRoute>
                  <RestaurantVerification />
                </PublicRoute>
              }
            />

            <Route
              path="/forgot-password"
              element={
                <PublicRoute>
                  <ForgotPassword />
                </PublicRoute>
              }
            />

            <Route
              path="/reset-password/password"
              element={
                <PublicRoute>
                  <ResetPassword />
                </PublicRoute>
              }
            />

            <Route
              path="/cart"
              element={
                <PrivateRoute>
                  <Cart />
                </PrivateRoute>
              }
            />
            <Route
              path="/checkout"
              element={
                <PrivateRoute>
                  <Checkout />
                </PrivateRoute>
              }
            />
            <Route
              path="/profile"
              element={
                <PrivateRoute>
                  <Profile />
                </PrivateRoute>
              }
            />
            <Route
              path="/orders"
              element={
                <PrivateRoute>
                  <Orders />
                </PrivateRoute>
              }
            />
            <Route
              path="/orders/:orderId"
              element={
                <PrivateRoute>
                  <Order />
                </PrivateRoute>
              }
            />

            <Route
              path="/rewards"
              element={
                <PrivateRoute>
                  <RewardsPage />
                </PrivateRoute>
              }
            />

            <Route
              path='/restaurant/:restaurantId/management'
              element={
                <PrivateRoute>
                  <RestaurantManagement />
                </PrivateRoute>
              }
            />

            <Route
              path='/company-management'
              element={
                <PrivateRoute>
                  <CompanyManagment />
                </PrivateRoute>
              }
            />

            <Route
              path='/admin/chat'
              element={
                <PrivateRoute>
                  <AdminChat />
                </PrivateRoute>
              }
            />

            <Route
              path='/chat-test'
              element={
                <PrivateRoute>
                  <ChatTest />
                </PrivateRoute>
              }
            />

            <Route
              path='/admin/dashboard'
              element={
                <PrivateRoute>
                  <AdminDashboard />
                </PrivateRoute>
              }
            />

            <Route
              path='/restaurant/dashboard'
              element={
                <PrivateRoute>
                  <RestaurantDashboard />
                </PrivateRoute>
              }
            />

            <Route
              path='/admin/system-health'
              element={
                <PrivateRoute>
                  <SystemHealth />
                </PrivateRoute>
              }
            />

            <Route
              path='/admin/user-analytics'
              element={
                <PrivateRoute>
                  <UserAnalytics />
                </PrivateRoute>
              }
            />

            <Route
              path='/admin/business-insights'
              element={
                <PrivateRoute>
                  <BusinessInsights />
                </PrivateRoute>
              }
            />

            <Route
              path='/restaurant/menu-analytics'
              element={
                <PrivateRoute>
                  <RestaurantMenuAnalytics />
                </PrivateRoute>
              }
            />

            <Route
              path='/restaurant/customer-analytics'
              element={
                <PrivateRoute>
                  <RestaurantCustomerAnalytics />
                </PrivateRoute>
              }
            />

            <Route
              path='/restaurant/business-tips'
              element={
                <PrivateRoute>
                  <RestaurantBusinessTips />
                </PrivateRoute>
              }
            />

          </Routes>
        </main>
        <CustomerChatWidget />
        <HappyHourModal 
          isOpen={showHappyHourModal}
          onClose={() => setShowHappyHourModal(false)}
          happyHour={selectedHappyHour}
        />
        <MobileNav />
        <Footer />
      </div >
    </Router >
  );
};

export default App;
