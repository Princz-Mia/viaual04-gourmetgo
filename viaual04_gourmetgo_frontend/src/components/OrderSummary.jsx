import React from "react";
import { Link } from "react-router-dom";

export default function OrderSummary({ order }) {
    const { id, restaurant, orderDate, totalAmount, status } = order;
    const statusClass = {
        Delivered: "badge badge-success text-right w-24",
        "In Progress": "badge badge-warning text-right w-24",
        Cancelled: "badge badge-error text-right w-24",
        Pending: "badge badge-info text-right w-24",
    }[status] || "badge badge-neutral text-right w-24";

    return (
        <Link to={`/orders/${id}`}>
            <div className="bg-white shadow-lg rounded-lg p-4 flex flex-col sm:flex-row items-center gap-4 hover:bg-base-100 transition">
                <div className="flex-1">
                    <p className="text-sm text-gray-500">
                        Order ID: <span className="font-mono">{id}</span>
                    </p>
                    <h3 className="text-lg font-semibold text-gray-800">
                        {restaurant}
                    </h3>
                </div>
                <div className="text-gray-600 w-32 text-center">
                    <p className="text-sm">Date</p>
                    <p className="font-medium">
                        {new Date(orderDate).toLocaleDateString()}
                    </p>
                </div>
                <div className="text-gray-600 w-32 text-center">
                    <p className="text-sm">Total</p>
                    <p className="font-medium">${totalAmount.toFixed(2)}</p>
                </div>
                <span className={statusClass}>{status}</span>
            </div>
        </Link>
    );
}
