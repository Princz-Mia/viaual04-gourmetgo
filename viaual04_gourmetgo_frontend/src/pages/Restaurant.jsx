import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import ProductCard from "../components/ProductCard";
import ProductModal from "../components/ProductModal";
import ReviewItem from "../components/ReviewItem";
import ReviewModal from "../components/ReviewModal";
import { fetchRestaurant } from "../api/restaurantService";
import { fetchProductsByRestaurantId } from "../api/productService";
import { fetchReviewsByRestaurant, canReviewRestaurant, addReview } from "../api/reviewService";
import { useCart } from "../contexts/CartContext";
import imageNotFound from "../assets/images/image_not_found.jpg"
import { getTodayHours, isRestaurantOpenNow } from "../utils/isOpen";

const Restaurant = () => {
  const { restaurantId } = useParams();
  const [restaurant, setRestaurant] = useState(null);
  const [products, setProducts] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [canReview, setCanReview] = useState(false);
  const [loading, setLoading] = useState(true);

  const [view, setView] = useState("products");
  const [selectedCategories, setSelectedCategories] = useState([]);

  const [productModalOpen, setProductModalOpen] = useState(false);
  const [currentProduct, setCurrentProduct] = useState(null);

  const [reviewModalOpen, setReviewModalOpen] = useState(false);
  const [reviewFilter, setReviewFilter] = useState(null);

  const { addItem } = useCart();

  useEffect(() => {
    const loadData = async () => {
      try {
        const [restData, productData, revs, allowed] = await Promise.all([
          fetchRestaurant(restaurantId),
          fetchProductsByRestaurantId(restaurantId),
          fetchReviewsByRestaurant(restaurantId),
          canReviewRestaurant(restaurantId)
        ]);
        setRestaurant(restData);
        setProducts(productData);
        setReviews(revs);
        setCanReview(allowed);
      } catch (err) {
        console.error("Failed to load data", err);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, [restaurantId]);

  const handleCardClick = (product) => {
    setCurrentProduct(product);
    setProductModalOpen(true);
  };

  const handleAddToCart = (productId) => {
    addItem(productId, 1);
  };

  const handleSubmitReview = async ({ rating, comment }) => {
    try {
      const newRev = await addReview(restaurantId, rating, comment);
      setReviews([newRev, ...reviews]);
    } catch (err) {
      console.error("Failed to submit review", err);
    } finally {
      setReviewModalOpen(false);
    }
  };

  // Category filter logic
  const allCategories = [...new Set(products?.map(p => p.category?.name))];
  const filteredProducts = selectedCategories.length
    ? products?.filter(p => selectedCategories.includes(p.category?.name))
    : products;

  // Review filter logic
  const filteredReviews = reviewFilter
    ? reviews.filter(r => r.ratingValue === reviewFilter)
    : reviews;
  const totalReviews = reviews.length;
  const ratingCounts = [5, 4, 3, 2, 1].map(r => ({
    rating: r,
    count: reviews.filter(rv => rv.ratingValue === r).length
  }));

  if (loading || !restaurant) return <p className="text-center py-8">Loading...</p>;

  const todayInfo = getTodayHours(restaurant.openingHours);
  const hoursLabel = todayInfo?.label || "Closed Today";
  const isOpen = isRestaurantOpenNow(restaurant.openingHours);

  return (
    <div className="container mx-auto px-4 py-8 space-y-8">
      <div className="flex flex-col md:flex-row items-center bg-white p-6 rounded-lg shadow mb-6">
        <img
          src={
            restaurant.logo?.downloadUrl
              ? `http://localhost:8080${restaurant.logo.downloadUrl}`
              : imageNotFound
          }
          alt={`${restaurant.name} logo`}
          className="w-24 h-24 rounded-full mr-6 mb-4 md:mb-0"
          onError={(e) => {
            e.currentTarget.onerror = null; // végtelen ciklus elkerülése
            e.currentTarget.src = imageNotFound;
          }}
        />
        <div>
          <h1 className="text-4xl font-bold">{restaurant.name}</h1>
          <p className="text-gray-600">Hours: {hoursLabel}</p>
          <p className={`mt-2 font-semibold ${isOpen ? "text-green-600" : "text-red-600"}`}>
            {isOpen ? "Open Now" : "Closed"}
          </p>
        </div>
      </div>

      <div className="flex gap-2 mb-4">
        <button
          className={`btn ${view === "products" ? "btn-primary" : "btn-outline"}`}
          onClick={() => setView("products")}
        >
          Menu
        </button>
        <button
          className={`btn ${view === "reviews" ? "btn-primary" : "btn-outline"}`}
          onClick={() => setView("reviews")}
        >
          Reviews
        </button>
      </div>

      <div className="flex flex-col md:flex-row gap-6">
        <aside className="md:w-1/4 bg-white p-6 rounded-lg shadow space-y-6">
          {view === "products" ? (
            <>
              <h2 className="text-xl font-semibold">Filter by Type</h2>
              <div className="flex flex-col space-y-2">
                {allCategories.map((cat, i) => (
                  <label key={i} className="flex items-center">
                    <input
                      type="checkbox"
                      className="checkbox checkbox-primary mr-2"
                      checked={selectedCategories.includes(cat)}
                      onChange={() =>
                        setSelectedCategories(prev =>
                          prev.includes(cat)
                            ? prev.filter(c => c !== cat)
                            : [...prev, cat]
                        )
                      }
                    />
                    <span>{cat}</span>
                  </label>
                ))}
              </div>
            </>
          ) : (
            <>
              <h2 className="text-xl font-semibold">Rating Breakdown</h2>
              {ratingCounts.map(({ rating, count }) => {
                const pct = totalReviews ? (count / totalReviews) * 100 : 0;
                return (
                  <div
                    key={rating}
                    className="cursor-pointer"
                    onClick={() => setReviewFilter(rating)}
                  >
                    <div className="flex justify-between">
                      <span>{rating} star{rating > 1 ? 's' : ''}</span>
                      <span>{count} ({pct.toFixed(0)}%)</span>
                    </div>
                    <div className="bg-gray-200 h-3 rounded">
                      <div
                        className="bg-blue-500 h-3 rounded"
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  </div>
                );
              })}
              {reviewFilter != null && (
                <button
                  className="btn btn-sm btn-outline mt-4 w-full"
                  onClick={() => setReviewFilter(null)}
                >
                  Show All
                </button>
              )}
            </>
          )}
        </aside>

        <main className="flex-1 space-y-6">
          {view === "products" ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredProducts?.map(p => (
                <ProductCard
                  key={p.id}
                  image={p.image?.downloadUrl}
                  name={p.name}
                  description={p.description}
                  price={p.price}
                  onAddToCart={() => handleAddToCart(p.id)}
                  onCardClick={() => handleCardClick(p)}
                  clickable
                />
              ))}
            </div>
          ) : (
            <>
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-2xl font-semibold">Reviews</h2>
                {canReview ? (
                  <button
                    className="btn btn-primary btn-sm"
                    onClick={() => setReviewModalOpen(true)}
                  >
                    Add Review
                  </button>
                ) : (
                  <p className="text-sm text-gray-500">
                    Only customers who ordered can review.
                  </p>
                )}
              </div>
              <div className="space-y-4">
                {filteredReviews.map(r => (
                  <ReviewItem key={r.id} {...r} />
                ))}
                {filteredReviews.length === 0 && (
                  <p className="text-center text-gray-500">No reviews to display.</p>
                )}
              </div>
              <ReviewModal
                isOpen={reviewModalOpen}
                onClose={() => setReviewModalOpen(false)}
                onSubmit={handleSubmitReview}
              />
            </>
          )}
          <ProductModal
            isOpen={productModalOpen}
            onClose={() => setProductModalOpen(false)}
            product={currentProduct}
          />
        </main>
      </div>
    </div>
  );
};

export default Restaurant;