import React from "react";
import imageNotFound from "../assets/images/image_not_found.jpg"

const CartItem = ({
    image,
    name,
    price,
    quantity,
    onQuantityChange,
    onRemove,
}) => {
    const handleDecrease = () => {
        const newQty = quantity - 1;
        if (newQty <= 0) {
            onRemove();
        } else {
            onQuantityChange(newQty);
        }
    };

    const handleIncrease = () => {
        onQuantityChange(quantity + 1);
    };

    const totalPrice = (price * quantity).toFixed(2);

    return (
        <div className="bg-white shadow-lg rounded-lg p-4 flex flex-col sm:flex-row items-center gap-4">
            {/* Product Image */}
            <img
                src={
                    image
                        ? `http://localhost:8080${image}`
                        : imageNotFound
                }
                alt={name}
                className="w-24 h-24 object-cover rounded flex-shrink-0"
                onError={(e) => {
                    e.currentTarget.onerror = null; // végtelen ciklus elkerülése
                    e.currentTarget.src = imageNotFound;
                }}
            />

            {/* Name and Quantity Controls */}
            <div className="flex-1 flex flex-col sm:flex-row sm:items-center sm:justify-between w-full">
                <h4 className="text-lg font-semibold text-gray-800 mb-2 sm:mb-0">
                    {name}
                </h4>

                <div className="flex items-center gap-2">
                    <button
                        onClick={handleDecrease}
                        className="btn btn-sm btn-outline"
                    >
                        −
                    </button>
                    <span className="px-3 py-1 border rounded min-w-[2rem] text-center">
                        {quantity}
                    </span>
                    <button
                        onClick={handleIncrease}
                        className="btn btn-sm btn-outline"
                    >
                        +
                    </button>
                </div>
            </div>

            {/* Total Price */}
            <div className="w-24 flex-shrink-0 text-right text-lg font-bold text-gray-900">
                ${totalPrice}
            </div>

            {/* Remove Button */}
            <button
                onClick={onRemove}
                className="btn btn-error btn-sm btn-circle flex-shrink-0"
            >
                ×
            </button>
        </div>
    );
};

export default CartItem;
