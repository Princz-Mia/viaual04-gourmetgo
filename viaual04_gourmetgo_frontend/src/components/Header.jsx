import React, { useState, useEffect, useRef, useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FiShoppingCart, FiMenu, FiX } from "react-icons/fi";
import { FaComments } from "react-icons/fa";
import { AuthContext } from "../contexts/AuthContext";
import { useCart } from "../contexts/CartContext";
import { useChat } from "../contexts/ChatContext";
import RewardBalance from "./RewardBalance";
import gourmetGoLogo from "../assets/images/gourmetgo_logo.jpg";

// Export the ref for external access
export let rewardBalanceRef = null;

const Header = () => {
  const { user, logout } = useContext(AuthContext);
  const { items } = useCart();
  const { unreadCount } = useChat();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [cartCount, setCartCount] = useState(0);
  const navigate = useNavigate();
  const dropdownRef = useRef();
  const mobileMenuRef = useRef();
  const rewardBalanceRefLocal = useRef();
  
  // Set the global ref
  rewardBalanceRef = rewardBalanceRefLocal;

  const userRole = user?.role?.authority || user?.role;
  const isAdmin = userRole === 'ROLE_ADMIN';
  const isRestaurant = userRole === 'ROLE_RESTAURANT';
  const isCustomer = userRole === 'ROLE_CUSTOMER';

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
      if (mobileMenuRef.current && !mobileMenuRef.current.contains(e.target)) {
        setMobileMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', onClick);
    return () => document.removeEventListener('mousedown', onClick);
  }, []);

  const handleSignOut = () => {
    logout();
    navigate('/login');
  };

  const displayName = user?.fullName || '';
  const initial = displayName ? displayName.charAt(0).toUpperCase() : 'U';

  return (
    <>
      <header className="navbar bg-base-100 border-b border-base-300 shadow-sm px-3 sm:px-4">
        <div className="max-w-6xl mx-auto w-full flex items-center justify-between">
          {/* Mobile Menu Button */}
          <button
            className="btn btn-ghost btn-circle btn-sm sm:hidden"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          >
            {mobileMenuOpen ? <FiX className="w-5 h-5" /> : <FiMenu className="w-5 h-5" />}
          </button>

          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2">
            <img src={gourmetGoLogo} alt="GourmetGo" className="h-6 w-6 sm:h-8 sm:w-8" />
            <span className="font-display text-lg sm:text-xl font-semibold text-primary">GourmetGo</span>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden sm:flex items-center space-x-6">
            <Link to="/restaurants" className="text-neutral hover:text-primary transition-colors">Restaurants</Link>
            {user && isCustomer && <RewardBalance ref={rewardBalanceRefLocal} />}
          </div>

          {/* Right Actions */}
          <div className="flex items-center space-x-2">
            {user && isCustomer && (
              <button
                onClick={() => navigate('/cart')}
                className="btn btn-ghost btn-circle btn-sm sm:btn-md"
              >
                <div className="indicator">
                  <FiShoppingCart className="w-5 h-5 sm:w-6 sm:h-6 text-neutral" />
                  {cartCount > 0 && (
                    <span className="indicator-item badge badge-xs badge-primary">
                      {cartCount}
                    </span>
                  )}
                </div>
              </button>
            )}

            {user && isAdmin && (
              <button
                onClick={() => navigate('/admin/chat')}
                className="btn btn-ghost btn-circle btn-sm sm:btn-md relative"
              >
                <div className="indicator">
                  <FaComments className="w-5 h-5 sm:w-6 sm:h-6 text-neutral" />
                  {unreadCount > 0 && (
                    <span className="indicator-item badge badge-xs badge-error animate-pulse">
                      {unreadCount}
                    </span>
                  )}
                </div>
              </button>
            )}

            {user ? (
              <div className="hidden sm:block relative" ref={dropdownRef}>
                <button
                  onClick={() => setDropdownOpen(open => !open)}
                  className="flex items-center space-x-2 focus:outline-none hover:bg-base-200 rounded-lg px-2 py-1 transition-colors duration-200 group"
                >
                  <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary group-hover:bg-primary group-hover:text-white transition-colors duration-200">
                    {isAdmin ? (
                      <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M2.166 4.999A11.954 11.954 0 0010 1.944 11.954 11.954 0 0017.834 5c.11.65.166 1.32.166 2.001 0 5.225-3.34 9.67-8 11.317C5.34 16.67 2 12.225 2 7c0-.682.057-1.35.166-2.001zm11.541 3.708a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                      </svg>
                    ) : isRestaurant ? (
                      <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clipRule="evenodd" />
                      </svg>
                    ) : (
                      <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                      </svg>
                    )}
                  </div>
                  <span className="hidden md:inline text-neutral font-medium group-hover:text-primary transition-colors duration-200">{displayName}</span>
                  <svg className="w-4 h-4 text-neutral/60 group-hover:text-primary transition-colors duration-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                </button>

                {dropdownOpen && (
                  <div className="absolute right-0 mt-2 w-52 bg-base-100 border border-base-300 rounded-2xl shadow-lg z-20">
                    <Link
                      to={isRestaurant ? `/restaurant/${user.id}/management` : "/profile"}
                      className="block px-4 py-2 text-neutral hover:bg-base-200 transition-colors"
                      onClick={() => setDropdownOpen(false)}
                    >
                      {isRestaurant ? "Management" : "Profile"}
                    </Link>
                    {isCustomer && (
                      <Link
                        to="/rewards"
                        className="block px-4 py-2 text-neutral hover:bg-base-200 transition-colors"
                        onClick={() => setDropdownOpen(false)}
                      >
                        My Rewards
                      </Link>
                    )}
                    {isCustomer && (
                      <Link
                        to="/orders"
                        className="block px-4 py-2 text-neutral hover:bg-base-200 transition-colors"
                        onClick={() => setDropdownOpen(false)}
                      >
                        Orders
                      </Link>
                    )}
                    {isAdmin && (
                      <Link
                        to="/company-management"
                        className="block px-4 py-2 text-neutral hover:bg-base-200 transition-colors"
                        onClick={() => setDropdownOpen(false)}
                      >
                        Management
                      </Link>
                    )}
                    {isAdmin && (
                      <>
                        <Link
                          to="/admin/dashboard"
                          className="block px-4 py-2 text-neutral hover:bg-base-200 transition-colors"
                          onClick={() => setDropdownOpen(false)}
                        >
                          Dashboard
                        </Link>
                        <Link
                          to="/admin/chat"
                          className="block px-4 py-2 text-neutral hover:bg-base-200 transition-colors"
                          onClick={() => setDropdownOpen(false)}
                        >
                          Chat Support
                        </Link>
                      </>
                    )}
                    {isRestaurant && (
                      <Link
                        to="/restaurant/dashboard"
                        className="block px-4 py-2 text-neutral hover:bg-base-200 transition-colors"
                        onClick={() => setDropdownOpen(false)}
                      >
                        Dashboard
                      </Link>
                    )}
                    <button
                      onClick={handleSignOut}
                      className="w-full text-left px-4 py-2 text-neutral hover:bg-base-200 transition-colors"
                    >
                      Sign Out
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className="hidden sm:flex space-x-4">
                <Link to="/login" className="text-neutral hover:text-primary transition-colors">Login</Link>
                <Link to="/registration" className="btn btn-primary btn-sm rounded-full">Register</Link>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* Mobile Menu Overlay */}
      {mobileMenuOpen && (
        <div className="fixed inset-0 bg-black/50 z-40 sm:hidden" onClick={() => setMobileMenuOpen(false)} />
      )}

      {/* Mobile Menu Sidebar */}
      <div
        ref={mobileMenuRef}
        className={`fixed top-0 left-0 h-full w-64 bg-base-100 shadow-lg z-50 transform transition-transform duration-300 sm:hidden ${
          mobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="p-4 border-b border-base-300">
          <div className="flex items-center justify-between">
            <span className="font-display text-lg font-semibold text-primary">Menu</span>
            <button
              onClick={() => setMobileMenuOpen(false)}
              className="btn btn-ghost btn-circle btn-sm"
            >
              <FiX className="w-5 h-5" />
            </button>
          </div>
        </div>
        
        <div className="p-4 space-y-4">
          <Link
            to="/restaurants"
            className="block py-2 text-neutral hover:text-primary transition-colors"
            onClick={() => setMobileMenuOpen(false)}
          >
            Restaurants
          </Link>
          
          {user ? (
            <>
              {isCustomer && (
                <>
                  <div className="py-2">
                    <RewardBalance ref={rewardBalanceRefLocal} />
                  </div>
                  <Link
                    to="/rewards"
                    className="block py-2 text-neutral hover:text-primary transition-colors"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    My Rewards
                  </Link>
                </>
              )}
              
              <Link
                to={isRestaurant ? `/restaurant/${user.id}/management` : "/profile"}
                className="block py-2 text-neutral hover:text-primary transition-colors"
                onClick={() => setMobileMenuOpen(false)}
              >
                {isRestaurant ? "Management" : "Profile"}
              </Link>
              
              {isCustomer && (
                <Link
                  to="/orders"
                  className="block py-2 text-neutral hover:text-primary transition-colors"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Orders
                </Link>
              )}
              {isAdmin && (
                <Link
                  to="/company-management"
                  className="block py-2 text-neutral hover:text-primary transition-colors"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Management
                </Link>
              )}
              
              {isAdmin && (
                <>
                  <Link
                    to="/admin/dashboard"
                    className="block py-2 text-neutral hover:text-primary transition-colors"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    Dashboard
                  </Link>
                  <Link
                    to="/admin/chat"
                    className="block py-2 text-neutral hover:text-primary transition-colors"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    Chat Support
                  </Link>
                </>
              )}
              
              {isRestaurant && (
                <Link
                  to="/restaurant/dashboard"
                  className="block py-2 text-neutral hover:text-primary transition-colors"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  Dashboard
                </Link>
              )}
              
              <button
                onClick={() => {
                  handleSignOut();
                  setMobileMenuOpen(false);
                }}
                className="block w-full text-left py-2 text-neutral hover:text-primary transition-colors"
              >
                Sign Out
              </button>
            </>
          ) : (
            <>
              <Link
                to="/login"
                className="block py-2 text-neutral hover:text-primary transition-colors"
                onClick={() => setMobileMenuOpen(false)}
              >
                Login
              </Link>
              <Link
                to="/registration"
                className="block py-2 text-neutral hover:text-primary transition-colors"
                onClick={() => setMobileMenuOpen(false)}
              >
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </>
  );
};

export default Header;