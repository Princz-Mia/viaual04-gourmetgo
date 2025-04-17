import React, { useState, useEffect, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FiShoppingCart } from "react-icons/fi"; // Using react-icons for the cart icon

const Header = () => {
  const [user, setUser] = useState(null);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [cartCount, setCartCount] = useState(0);
  const navigate = useNavigate();
  const dropdownRef = useRef();

  // Load user from localStorage
  useEffect(() => {
    const saved = localStorage.getItem('user');
    if (saved) setUser(JSON.parse(saved));
  }, []);

  // Load cart count from localStorage (assuming 'cart' key holds array)
  useEffect(() => {
    const cart = JSON.parse(localStorage.getItem('cart') || '[]');
    setCartCount(cart.length);
  }, []);

  // Close dropdown on outside click
  useEffect(() => {
    const onClick = e => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', onClick);
    return () => document.removeEventListener('mousedown', onClick);
  }, []);

  const handleSignOut = () => {
    localStorage.removeItem('user');
    setUser(null);
    navigate('/login');
  };

  return (
    <header className="bg-white shadow-md">
      <div className="container mx-auto px-4 flex items-center justify-between h-16">
        {/* Logo + Nav */}
        <div className="flex items-center space-x-6">
          <Link to="/" className="text-2xl font-bold text-gray-800">GourmetGo</Link>
          <Link to="/restaurants" className="text-gray-700 hover:text-gray-900">
            Restaurants
          </Link>
        </div>

        <div className="flex items-center space-x-4">
          {/* Cart Icon */}
          <button
            onClick={() => navigate('/cart')}
            className="relative btn btn-ghost btn-circle"
          >
            <FiShoppingCart className="w-6 h-6 text-gray-800" />
            {cartCount > 0 && (
              <span className="badge badge-sm badge-primary indicator-item">
                {cartCount}
              </span>
            )}
          </button>

          {/* User / Auth Links */}
          {user ? (
            <div className="relative" ref={dropdownRef}>
              <button
                onClick={() => setDropdownOpen(open => !open)}
                className="flex items-center focus:outline-none"
              >
                <div className="w-8 h-8 rounded-full bg-gray-300 flex items-center justify-center text-gray-600">
                  {user.fullName?.charAt(0).toUpperCase() || 'U'}
                </div>
              </button>
              {dropdownOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-20">
                  <Link to="/profile" className="block px-4 py-2 hover:bg-gray-100">Profile</Link>
                  <Link to="/orders" className="block px-4 py-2 hover:bg-gray-100">Orders</Link>
                  <Link to="/messages" className="block px-4 py-2 hover:bg-gray-100">Messages</Link>
                  <button
                    onClick={handleSignOut}
                    className="w-full text-left px-4 py-2 hover:bg-gray-100"
                  >
                    Sign Out
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="flex space-x-4">
              <Link to="/login" className="text-gray-800 hover:text-gray-600">Login</Link>
              <Link to="/register" className="text-gray-800 hover:text-gray-600">Register</Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
