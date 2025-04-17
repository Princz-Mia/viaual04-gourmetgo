import React from "react";
import OrderItem from "../components/OrderItem";

// Page listing past orders
const Orders = () => {
  // Mock past orders data
  const mockOrders = [
    {
      id: "a1b2c3d4-e5f6-7890-ab12-cd34ef56gh78",
      restaurant: "La Bella Italia",
      date: "2025-03-28",
      total: 45.75,
      status: "Delivered",
    },
    {
      id: "1234abcd-5678-ef90-1234-5678abcdef90",
      restaurant: "Dragon Express",
      date: "2025-04-02",
      total: 32.40,
      status: "In Progress",
    },
    {
      id: "abcd1234-ef56-7890-ab12-3456cdef7890",
      restaurant: "Spice Route",
      date: "2025-04-10",
      total: 58.20,
      status: "Cancelled",
    },
    // Add more orders as needed
  ];

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Your Past Orders</h1>
      <div className="space-y-4">
        {mockOrders.map((order) => (
          <OrderItem
            key={order.id}
            id={order.id}
            restaurant={order.restaurant}
            date={order.date}
            total={order.total}
            status={order.status}
          />
        ))}
      </div>
    </div>
  );
};

export default Orders;
