import React, { useEffect } from "react";
import imageNotFound from "../assets/images/image_not_found.jpg"

export default function OrderItem({ item }) {
    const { product, quantity, price, } = item;
    const lineTotal = product.price * quantity;

    return (
        <div className="bg-white shadow rounded-lg p-4 flex items-center gap-4">
            <img
                src={
                    product.image
                        ? `http://localhost:8080${product.image}`
                        : imageNotFound
                }
                alt={product.name}
                className="w-20 h-20 object-cover rounded"
                onError={(e) => {
                    e.currentTarget.onerror = null; // végtelen ciklus elkerülése
                    e.currentTarget.src = imageNotFound;
                }}
            />
            <div className="flex-1">
                <h4 className="font-semibold">{product.name}</h4>
                <p className="text-sm text-gray-600">
                    Unit: ${product.price.toFixed(2)} × {quantity}
                </p>
            </div>
            <div className="font-bold">${lineTotal.toFixed(2)}</div>
        </div>
    );
}
