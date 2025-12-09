import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { FiHome, FiSearch, FiShoppingCart, FiUser } from 'react-icons/fi';
import { useCart } from '../contexts/CartContext';
import { useContext } from 'react';
import { AuthContext } from '../contexts/AuthContext';

const MobileNav = () => {
  const location = useLocation();
  const { items } = useCart();
  const { user } = useContext(AuthContext);
  
  const cartCount = items.length;
  const userRole = user?.role?.authority || user?.role;
  const isCustomer = userRole === 'ROLE_CUSTOMER';

  // Show on all pages for consistency

  const navItems = [
    {
      path: '/',
      icon: FiHome,
      label: 'Home',
      show: true
    },
    {
      path: '/restaurants',
      icon: FiSearch,
      label: 'Restaurants',
      show: true
    },
    {
      path: '/cart',
      icon: FiShoppingCart,
      label: 'Cart',
      show: user && isCustomer,
      badge: cartCount > 0 ? cartCount : null
    },
    {
      path: user ? (isCustomer ? '/orders' : userRole === 'ROLE_ADMIN' ? '/admin/dashboard' : '/restaurant/dashboard') : '/login',
      icon: FiUser,
      label: user ? (isCustomer ? 'Orders' : 'Dashboard') : 'Login',
      show: true
    }
  ];

  return (
    <div className="fixed bottom-0 left-0 right-0 sm:hidden bg-base-100 border-t border-base-300 z-50">
      <div className="flex justify-around items-center py-2">
        {navItems.filter(item => item.show).map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.path;
          
          return (
            <Link
              key={item.path}
              to={item.path}
              className={`flex flex-col items-center py-1 px-2 ${isActive ? 'text-primary' : 'text-neutral/70'}`}
            >
              <div className="relative">
                <Icon className="w-5 h-5" />
                {item.badge && (
                  <span className="absolute -top-2 -right-2 badge badge-xs badge-primary">
                    {item.badge}
                  </span>
                )}
              </div>
              <span className="text-xs mt-1">{item.label}</span>
            </Link>
          );
        })}
      </div>
    </div>
  );
};

export default MobileNav;