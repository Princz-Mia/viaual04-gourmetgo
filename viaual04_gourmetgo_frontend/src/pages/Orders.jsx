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
        if (user.role.authority === 'ROLE_CUSTOMER') {
          data = await fetchCustomerOrders(user.id);
        } else if (user.role.authority === 'ROLE_RESTAURANT') {
          data = await fetchRestaurantOrders(user.id);
        } else if (user.role.authority === 'ROLE_ADMIN') {
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

  const header =
    user.role.authority === 'ROLE_CUSTOMER'
      ? 'Your Orders'
      : user.role.authority === 'ROLE_RESTAURANT'
        ? 'Incoming Orders'
        : 'All Orders';

  return (
    <div className="container mx-auto px-4 py-8 space-y-4">
      <h1 className="text-3xl font-bold">{header}</h1>
      {orders.length > 0 ? (
        orders.map(o => <OrderSummary key={o.id} order={o} />)
      ) : (
        <p className="text-center text-gray-500">No orders found.</p>
      )}
    </div>
  );
}