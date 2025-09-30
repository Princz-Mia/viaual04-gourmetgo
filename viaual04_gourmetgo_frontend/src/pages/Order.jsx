// src/pages/Order.jsx
import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import {
    fetchOrderById,
    updateOrderStatus
} from '../api/orderService';
import OrderItem from '../components/OrderItem';
import { toast } from 'react-toastify';
import { fetchRestaurant } from '../api/restaurantService';
import {
    OrderStatuses,
    StatusLabels,
    StatusClasses
} from '../utils/statusUtils';


export default function Order() {
    const { user } = useAuth();
    const { orderId } = useParams();
    const [order, setOrder] = useState(null);
    const [restaurant, setRestaurant] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            try {
                // 1. Rendelés betöltése
                const ord = await fetchOrderById(orderId);
                setOrder(ord);
                // 2. Étterem betöltése, ha rendelésben van restaurantId
                if (ord.restaurantId) {
                    const rest = await fetchRestaurant(ord.restaurantId);
                    setRestaurant(rest);
                }
            } catch (err) {
                toast.error(err.message || 'Failed to load order');
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [orderId]);

    const canUpdate =
        user.role.authority === 'ROLE_ADMIN' ||
        (user.role.authority === 'ROLE_RESTAURANT' && order?.restaurantId === user.restaurantId);

    const handleChangeStatus = async (e) => {
        const newStatus = e.target.value;
        try {
            await updateOrderStatus(order.id, newStatus);
            setOrder(prev => ({ ...prev, status: newStatus }));
            toast.success('Status updated');
        } catch (err) {
            toast.error(err.message || 'Update failed');
        }
    };

    if (loading || !order) return <p className="text-center py-8">Loading order...</p>;

    // Számoljuk az összegeket
    const itemsTotal = order.orderItems
        .reduce((sum, it) => sum + it.price * it.quantity, 0);
    const couponValue = order.coupon?.type === 'AMOUNT'
        ? order.coupon.value
        : 0;
    const deliveryFee = restaurant?.deliveryFee ?? 0;

    return (
        <div className="container mx-auto px-4 py-8 space-y-6">
            <Link to="/orders" className="btn btn-ghost">← Back to Orders</Link>

            {/* Fejléc */}
            <div className="bg-white p-6 rounded-lg shadow space-y-2">
                <h2 className="text-2xl font-bold">Order #{order.id}</h2>
                <p>Date: {new Date(order.orderDate).toLocaleString()}</p>

                {/* Étterem */}
                {restaurant && (
                    <p>
                        From:&nbsp;
                        <Link to={`/restaurants/${restaurant.id}`} className="font-medium text-blue-600 hover:underline">
                            {restaurant.name}
                        </Link>
                    </p>
                )}

                {/* Státusz */}
                <div className="flex items-center space-x-2">
                    <span>Status:</span>
                    {canUpdate ? (
                        <select
                            value={order.status}
                            onChange={handleChangeStatus}
                            className="select select-bordered select-sm"
                        >
                            {OrderStatuses.map(s => (
                                <option key={s} value={s}>{StatusLabels[s]}</option>
                            ))}
                        </select>
                    ) : (
                        <span className={StatusClasses[order.status]}>
                            {StatusLabels[order.status]}
                        </span>
                    )}
                </div>
            </div>

            {/* Billing & Shipping */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <section className="bg-white p-6 rounded-lg shadow">
                    <h3 className="font-semibold mb-2">Billing Information</h3>
                    <p><strong>Name:</strong> {order.billingInformation.fullName}</p>
                    <p><strong>Phone:</strong> {order.billingInformation.phoneNumber}</p>
                    <p><strong>Address:</strong> {[
                        order.billingInformation.address.streetNumber,
                        order.billingInformation.address.addressLine,
                        order.billingInformation.address.city,
                        order.billingInformation.address.postalCode,
                        order.billingInformation.address.region
                    ].filter(Boolean).join(', ')}</p>
                </section>
                <section className="bg-white p-6 rounded-lg shadow">
                    <h3 className="font-semibold mb-2">Shipping Information</h3>
                    <p><strong>Name:</strong> {order.shippingInformation.fullName}</p>
                    <p><strong>Phone:</strong> {order.shippingInformation.phoneNumber}</p>
                    <p><strong>Address:</strong> {[
                        order.shippingInformation.address.streetNumber,
                        order.shippingInformation.address.addressLine,
                        order.shippingInformation.address.city,
                        order.shippingInformation.address.postalCode,
                        order.shippingInformation.address.region
                    ].filter(Boolean).join(', ')}</p>
                </section>
            </div>

            {/* Kupon */}
            {order.coupon && (
                <div className="bg-green-50 border-l-4 border-green-400 p-4 rounded">
                    <p>
                        <strong>Coupon:</strong> {order.coupon.code} &mdash;{' '}
                        {order.coupon.type === 'FREE_SHIP'
                            ? 'Free Shipping'
                            : `$${order.coupon.value.toFixed(2)} off`}
                    </p>
                </div>
            )}

            {/* Tétel lista */}
            <div className="space-y-4">
                {order.orderItems.map(item => (
                    <OrderItem key={item.id} item={item} />
                ))}
            </div>

            {/* Összesítés */}
            <div className="bg-white p-6 rounded-lg shadow space-y-2 text-right">
                <p className="flex justify-between">
                    <span>Items Total:</span>
                    <span>${itemsTotal.toFixed(2)}</span>
                </p>
                {couponValue > 0 && (
                    <p className="flex justify-between text-green-600">
                        <span>Coupon Discount:</span>
                        <span>-${couponValue.toFixed(2)}</span>
                    </p>
                )}
                <p className="flex justify-between">
                    <span>Delivery Fee:</span>
                    <span>${deliveryFee.toFixed(2)}</span>
                </p>
                <hr />
                <p className="flex justify-between font-bold text-xl">
                    <span>Total Paid:</span>
                    <span>${order.totalAmount.toFixed(2)}</span>
                </p>
            </div>
        </div>
    );
}
