import { useState } from "react";
import ProductCard from "../components/ProductCard";
import ReviewItem from "../components/ReviewItem";
import ReviewModal from "../components/ReviewModal";
import ProductModal from "../components/ProductModal";

const mockRestaurant = {
  id: 1,
  name: "GourmetGo Bistro",
  logo: "https://source.unsplash.com/100x100/?bistro,logo",
  hours: "10:00 AM - 10:00 PM",
  isOpen: true,
  products: [
    {
      id: 1,
      name: "Grilled Chicken Sandwich",
      image: "https://source.unsplash.com/featured/?chicken,sandwich",
      description: "Tender grilled chicken breast with fresh lettuce, tomato, and aioli on a toasted brioche bun.",
      price: 8.99,
      categories: ["Chicken", "Sandwich"],
    },
    {
      id: 2,
      name: "Classic Cheeseburger",
      image: "https://source.unsplash.com/featured/?cheeseburger",
      description: "Juicy beef patty topped with cheddar cheese, pickles, onions, and our special sauce.",
      price: 9.49,
      categories: ["Beef", "Burger"],
    },
    {
      id: 3,
      name: "Caesar Salad",
      image: "https://source.unsplash.com/featured/?salad",
      description: "Crisp romaine lettuce tossed with Caesar dressing, croutons, and parmesan cheese.",
      price: 7.25,
      categories: ["Salad", "Vegetarian"],
    },
    {
      id: 4,
      name: "Veggie Wrap",
      image: "https://source.unsplash.com/featured/?wrap",
      description: "Grilled seasonal vegetables with hummus and feta cheese wrapped in a spinach tortilla.",
      price: 7.99,
      categories: ["Vegetarian", "Wrap"],
    },
    {
      id: 5,
      name: "Chocolate Lava Cake",
      image: "https://source.unsplash.com/featured/?chocolate,cake",
      description: "Warm chocolate cake with a gooey molten center, served with vanilla ice cream.",
      price: 5.50,
      categories: ["Dessert", "Chocolate"],
    },
  ],
};

const mockReviews = [
  { id: 1, user: "Alice", date: "2025-04-01", rating: 5, comment: "Excellent food and service!" },
  { id: 2, user: "Bob", date: "2025-03-20", rating: 2, comment: "Too salty for my taste." },
  { id: 3, user: "Carol", date: "2025-04-10", rating: 4, comment: "Great ambiance." },
];

const Restaurant = () => {
  const { name, logo, hours, isOpen, products } = mockRestaurant;
  const [view, setView] = useState("products");
  const [selectedCategories, setSelectedCategories] = useState([]);

  const [productModalOpen, setProductModalOpen] = useState(false);
  const [currentProduct, setCurrentProduct] = useState(null);

  const [reviews, setReviews] = useState(mockReviews);
  const [reviewFilter, setReviewFilter] = useState(null);
  const [reviewModalOpen, setReviewModalOpen] = useState(false);

  const handleCardClick = (product) => {
    setCurrentProduct(product);
    setProductModalOpen(true);
  };

  const allCategories = [...new Set(products.flatMap(p => p.categories))];
  const filteredProducts = selectedCategories.length
    ? products.filter(p => p.categories.some(c => selectedCategories.includes(c)))
    : products;

  const filteredReviews = reviewFilter
    ? reviews.filter(r => r.rating === reviewFilter)
    : reviews;
  const totalReviews = reviews.length;
  const ratingCounts = [5, 4, 3, 2, 1].map(r => ({
    rating: r,
    count: reviews.filter(rv => rv.rating === r).length
  }));

  return (
    <div className="container mx-auto px-4 py-8 space-y-8">
      <div className="flex flex-col md:flex-row items-center bg-white p-6 rounded-lg shadow mb-6">
        <img src={logo} alt={`${name} logo`} className="w-24 h-24 rounded-full mr-6 mb-4 md:mb-0" />
        <div>
          <h1 className="text-4xl font-bold">{name}</h1>
          <p className="text-gray-600">Hours: {hours}</p>
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
                      onChange={() => {
                        setSelectedCategories(prev =>
                          prev.includes(cat) ? prev.filter(c => c !== cat) : [...prev, cat]
                        );
                      }}
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
              {filteredProducts.map(p => (
                <ProductCard
                  key={p.id}
                  image={p.image}
                  name={p.name}
                  description={p.description}
                  price={p.price}
                  onAddToCart={() => { }}
                  onCardClick={() => { handleCardClick(p) }}
                  clickable={true}

                />
              ))}
            </div>
          ) : (
            <>
              <div className="flex justify-end">
                <button className="btn btn-primary" onClick={() => setReviewModalOpen(true)}>
                  Add Review
                </button>
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
                onSubmit={rev => setReviews([rev, ...reviews])}
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