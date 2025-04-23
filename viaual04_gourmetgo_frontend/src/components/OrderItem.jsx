import React from "react";

export default function OrderItem({ item }) {
    // item: { id, product: { name, image, price }, quantity }
    const { product, quantity } = item;
    const lineTotal = product.price * quantity;

    return (
        <div className="bg-white shadow rounded-lg p-4 flex items-center gap-4">
            <img
                src={product.image}
                alt={product.name}
                className="w-20 h-20 object-cover rounded"
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
