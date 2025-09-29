import React from "react";
import { Link } from "react-router-dom";
import {
    OrderStatuses,
    StatusLabels,
    StatusClasses
} from '../utils/statusUtils';

export default function OrderSummary({ order }) {
    const { id, restaurant, orderDate, totalAmount, status } = order;

    return (
        <Link to={`/orders/${id}`}>
            <div className="bg-white shadow-lg rounded-lg p-4 flex flex-col sm:flex-row items-center gap-4 hover:bg-base-100 transition">
                <div className="flex-1">
                    <p className="text-sm text-gray-500">
                        Order ID: <span className="font-mono">{id}</span>
                    </p>
                    <h3 className="text-lg font-semibold text-gray-800">
                        {restaurant.name}
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
                <span className={StatusClasses[status]}>
                    {StatusLabels[status]}
                </span>
            </div>
        </Link>
    );
}
