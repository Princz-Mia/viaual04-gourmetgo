import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import {
  fetchCustomerOrders,
  fetchRestaurantOrders,
  fetchAllOrders
} from '../api/orderService';
import OrderSummary from '../components/OrderSummary';
import { toast } from 'react-toastify';

export default function Orders() {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        let data = [];
        const userRole = user.role?.authority || user.role;
        if (userRole === 'ROLE_CUSTOMER') {
          data = await fetchCustomerOrders(user.id);
        } else if (userRole === 'ROLE_RESTAURANT') {
          data = await fetchRestaurantOrders(user.id);
        } else if (userRole === 'ROLE_ADMIN') {
          data = await fetchAllOrders();
        }
        setOrders(data);
      } catch (err) {
        toast.error(err.message || 'Failed to load orders');
      } finally {
        setLoading(false);
      }
    };
    if (user) load();
  }, [user]);

  if (loading) return <p className="text-center py-8">Loading orders...</p>;

  const userRole = user?.role?.authority || user?.role;
  const header =
    userRole === 'ROLE_CUSTOMER'
      ? 'Your Orders'
      : userRole === 'ROLE_RESTAURANT'
        ? 'Incoming Orders'
        : 'All Orders';

  return (
    <div className="container mx-auto px-4 py-8 space-y-4">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold">{header}</h1>
        <button
          onClick={() => window.location.reload()}
          className="btn btn-primary btn-sm"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
          Refresh
        </button>
      </div>
      {orders.length > 0 ? (
        orders.map(o => <OrderSummary key={o.id} order={o} />)
      ) : (
        <p className="text-center text-gray-500">No orders found.</p>
      )}
    </div>
  );
}