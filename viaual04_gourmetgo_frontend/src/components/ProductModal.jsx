import React from "react";

const ProductModal = ({ isOpen, onClose, product }) => {
  if (!isOpen || !product) return null;
  const { image, name, description, price } = product;

  return (
    <div className="modal modal-open">
      <div className="modal-box relative max-w-2xl p-0">
        <button
          onClick={onClose}
          className="btn btn-sm btn-circle absolute right-4 top-4"
        >
          ✕
        </button>

        <img
          src={image}
          alt={name}
          className="w-full h-64 object-cover rounded-t-lg"
        />

        <div className="p-6">
          <h3 className="text-2xl font-bold text-gray-800 mb-4">{name}</h3>
          <p className="text-gray-700 mb-6">{description}</p>
          <div className="text-xl font-semibold text-blue-600">
            ${price.toFixed(2)}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductModal;
