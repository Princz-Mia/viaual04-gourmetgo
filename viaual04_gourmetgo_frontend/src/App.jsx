import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import Footer from './components/Footer';
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

const App = () => {
  return (
    <Router>
      <div className="flex flex-col min-h-screen">
        <Header />
        <main className="flex-grow bg-gray-100">
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

          </Routes>
        </main>
        <Footer />
      </div >
    </Router >
  );
};

export default App;
