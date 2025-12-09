import React from "react";
import imageNotFound from "../assets/images/image_not_found.jpg";
import { getImageUrl } from "../api/imageService";

const ProductCard = ({
  image,
  productImage,
  name,
  description,
  price,
  categories = [],
  categoryBonus = null,
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
      className={`bg-base-100 shadow-md hover:shadow-xl rounded-2xl overflow-hidden flex flex-col w-full transition-all duration-300 border border-base-200 ${clickable ? 'cursor-pointer hover:-translate-y-1 hover:shadow-2xl active:scale-98' : ''}`}
    >
      {/* Image Section */}
      <div className="w-full h-48 flex-shrink-0 relative">
        {categoryBonus && (
          <div className="absolute top-2 right-2 bg-accent text-white px-2 py-1 rounded-full text-xs font-bold z-10">
            +{(categoryBonus.bonusRate * 100).toFixed(0)}% Bonus
          </div>
        )}
        <img
          src={
            productImage?.id
              ? getImageUrl(productImage.id)
              : image
              ? `http://localhost:8080${image}`
              : imageNotFound
          }
          alt={name}
          className="w-full h-full object-cover transition-transform duration-300 hover:scale-105"
          onError={(e) => {
            e.currentTarget.onerror = null;
            e.currentTarget.src = imageNotFound;
          }}
        />
      </div>

      {/* Content Section */}
      <div className="p-5 flex flex-col justify-between flex-1 min-h-[180px]">
        <div className="flex-1 space-y-3">
          <h3 className="text-lg font-bold text-neutral leading-tight line-clamp-2">
            {name}
          </h3>
          <p className="text-sm text-neutral/70 line-clamp-3 leading-relaxed">
            {description}
          </p>
        </div>

        {/* Price and Action Row */}
        <div className="flex items-center justify-between gap-4 pt-4 border-t border-base-200 mt-4">
          <span className="text-xl font-bold text-primary">
            ${price.toFixed(2)}
          </span>
          {showAddToCart && onAddToCart && (
            <button
              onClick={handleAddToCart}
              className="btn btn-primary btn-sm rounded-full px-4 flex-shrink-0 font-medium"
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