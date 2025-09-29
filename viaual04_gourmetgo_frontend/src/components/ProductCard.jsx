import React from "react";
import imageNotFound from "../assets/images/image_not_found.jpg"

const ProductCard = ({
  image,
  name,
  description,
  price,
  showAddToCart = true,
  onAddToCart,
  clickable = true,
  onCardClick,
}) => {
  const handleCardClick = () => {
    if (clickable && onCardClick) {
      onCardClick();
    }
  };

  const handleAddToCart = (e) => {
    e.stopPropagation();
    onAddToCart && onAddToCart();
  };

  return (
    <div
      onClick={handleCardClick}
      className={`bg-white shadow-lg rounded-lg overflow-hidden flex flex-col sm:flex-row w-full ${clickable ? 'cursor-pointer' : ''}`}
    >
      <div className="sm:w-1/3 w-full h-48 sm:h-auto flex-shrink-0">
        <img
          src={
            image
              ? `http://localhost:8080${image}`
              : imageNotFound
          }
          alt={name}
          className="w-full h-full object-cover"
          onError={(e) => {
            e.currentTarget.onerror = null; // végtelen ciklus elkerülése
            e.currentTarget.src = imageNotFound;
          }}
        />
      </div>

      <div className="p-4 flex flex-col justify-between flex-1">
        <div>
          <h3 className="text-xl font-semibold text-gray-800 mb-2">{name}</h3>
          <p
            className="text-gray-600 mb-4"
            style={{
              display: "-webkit-box",
              WebkitLineClamp: 2,
              WebkitBoxOrient: "vertical",
              overflow: "hidden",
              textOverflow: "ellipsis",
            }}
          >
            {description}
          </p>
        </div>

        <div className="flex items-center justify-between">
          <span className="text-lg font-bold text-blue-600">
            ${price.toFixed(2)}
          </span>
          {showAddToCart && onAddToCart && (
            <button
              onClick={handleAddToCart}
              className="btn btn-primary btn-sm"
            >
              Add to Cart
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProductCard;