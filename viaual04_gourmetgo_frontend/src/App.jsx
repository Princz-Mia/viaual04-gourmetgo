import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import Footer from './components/Footer';
import Login from './pages/Login';
import Register from './pages/Register';
import GuestRoute from './routes/GuestRoute';
import Home from './pages/Home';
import Verification from './pages/Verification';

const App = () => {
  return (
    <Router>
      <div className="flex flex-col min-h-screen">
        <Header />
        <main className="flex-grow">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route
              path="/login"
              element={
                <GuestRoute>
                  <Login />
                </GuestRoute>
              }
            />
            <Route
              path="/register"
              element={
                <GuestRoute>
                  <Register />
                </GuestRoute>
              }
            />
            <Route
              path="/verify/account"
              element={
                <GuestRoute>
                  <Verification />
                </GuestRoute>
              }
            />
            {/* Add other routes here (e.g., Profile, Orders, Messages) */}
          </Routes>
        </main>
        <Footer />
      </div>
    </Router>
  );
};

export default App;
