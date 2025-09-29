import React, { useState, useEffect, useRef, useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FiShoppingCart } from "react-icons/fi";
import { AuthContext } from "../contexts/AuthContext";
import { useCart } from "../contexts/CartContext";

const Header = () => {
  const { user, logout } = useContext(AuthContext);
  const { items } = useCart();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [cartCount, setCartCount] = useState(0);
  const navigate = useNavigate();
  const dropdownRef = useRef();

  const isAdmin = user?.role.authority === 'ROLE_ADMIN';
  const isRestaurant = user?.role.authority === 'ROLE_RESTAURANT';
  const isCustomer = user?.role.authority === 'ROLE_CUSTOMER';

  useEffect(() => {
    if (isCustomer) {
      setCartCount(items.length);
    }
  }, [items]);

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
    logout();
    navigate('/login');
  };

  const displayName = user
    ? isRestaurant
      ? user.name
      : user.fullName
    : '';
  const initial = displayName ? displayName.charAt(0).toUpperCase() : 'U';

  return (
    <header className="bg-white shadow-md">
      <div className="container mx-auto px-4 flex items-center justify-between h-16">
        <div className="flex items-center space-x-6">
          <Link to="/" className="text-2xl font-bold text-gray-800">GourmetGo</Link>
          <Link to="/restaurants" className="text-gray-700 hover:text-gray-900">Restaurants</Link>
        </div>

        <div className="flex items-center space-x-4">
          {user && isCustomer && (
            <button
              onClick={() => navigate('/cart')}
              className="btn btn-ghost btn-circle p-0"  // p-0, hogy ne legyen plusz padding
            >
              <div className="indicator">
                <FiShoppingCart className="w-6 h-6 text-gray-800" />
                {cartCount > 0 && (
                  <span className="indicator-item badge badge-xs badge-primary">
                    {cartCount}
                  </span>
                )}
              </div>
            </button>
          )}

          {user ? (
            <div className="relative" ref={dropdownRef}>
              <button
                onClick={() => setDropdownOpen(open => !open)}
                className="flex items-center space-x-2 focus:outline-none"
              >
                <div className="w-8 h-8 rounded-full bg-gray-300 flex items-center justify-center text-gray-600">
                  {initial}
                </div>
                <span className="hidden sm:inline text-gray-800 font-medium">{displayName}</span>
              </button>

              {dropdownOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg z-20">
                  <Link
                    to={isRestaurant ? `/restaurant/${user.id}/management` : "/profile"}
                    className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
                    onClick={() => setDropdownOpen(false)}
                  >
                    {isRestaurant ? "Management Page" : "Profile"}
                  </Link>

                  <Link
                    to={isAdmin ? "/company-management" : "/orders"}
                    className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
                    onClick={() => setDropdownOpen(false)}
                  >
                    {isAdmin ? "Management Page" : "Orders"}
                  </Link>

                  <button
                    onClick={handleSignOut}
                    className="w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100"
                  >
                    Sign Out
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="flex space-x-4">
              <Link to="/login" className="text-gray-800 hover:text-gray-600">Login</Link>
              <Link to="/registration" className="text-gray-800 hover:text-gray-600">Register</Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
