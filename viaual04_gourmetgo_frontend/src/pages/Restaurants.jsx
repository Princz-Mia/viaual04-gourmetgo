import React, { useEffect, useState } from "react";
import RestaurantCard from "../components/RestaurantCard";
import { fetchRestaurants } from "../api/restaurantService";
import { getTodayHours, isRestaurantOpenNow } from "../utils/isOpen";
import { toast } from "react-toastify";
import imageNotFound from '../assets/images/image_not_found.jpg';
import { useLocation } from "react-router-dom";
import LoadingSpinner from '../components/LoadingSpinner';

const Restaurants = () => {
    const [restaurants, setRestaurants] = useState([]);
    const [loading, setLoading] = useState(true);

    const [search, setSearch] = useState("");
    const [selectedCategories, setSelectedCategories] = useState([]);
    const [openFilter, setOpenFilter] = useState(false);
    const [freeDeliveryFilter, setFreeDeliveryFilter] = useState(false);
    const [sortOption, setSortOption] = useState("Most Popular");
    const [filterModalOpen, setFilterModalOpen] = useState(false);

    const location = useLocation();

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const searchParam = params.get("name");
        if (searchParam) {
            setSearch(searchParam);
        }
    }, [location.search]);

    useEffect(() => {
        const load = async () => {
            try {
                const data = await fetchRestaurants();
                setRestaurants(data);
            } catch (err) {
                toast.error("Failed to fetch restaurants", err);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, []);
    
    // Refresh restaurants when returning to this page
    useEffect(() => {
        const handleFocus = async () => {
            try {
                const data = await fetchRestaurants();
                setRestaurants(data);
            } catch (err) {
                console.error("Failed to refresh restaurants", err);
            }
        };
        
        window.addEventListener('focus', handleFocus);
        return () => window.removeEventListener('focus', handleFocus);
    }, []);

    const allCategories = [
        ...new Set(restaurants.flatMap((r) => r.categories.map(c => c.name))),
    ];

    const toggleCategory = (category) => {
        if (selectedCategories.includes(category)) {
            setSelectedCategories(selectedCategories.filter((c) => c !== category));
        } else {
            setSelectedCategories([...selectedCategories, category]);
        }
    };

    const filterRestaurants = () => {
        let filtered = [...restaurants];

        if (search.trim() !== "") {
            filtered = filtered.filter((r) =>
                r.name.toLowerCase().includes(search.toLowerCase())
            );
        }

        if (selectedCategories.length > 0) {
            filtered = filtered.filter((r) => {
                const names = r.categories.map(c => c.name);
                return selectedCategories.some(cat => names.includes(cat));
            });
        }

        if (openFilter) {
            filtered = filtered.filter((r) =>
                isRestaurantOpenNow(r.openingHours)
            );
        }

        if (freeDeliveryFilter) {
            filtered = filtered.filter((r) => r.deliveryFee === null || r.deliveryFee?.toFixed(2) == 0.00);
        }

        if (sortOption === "Most Popular") {
            filtered.sort((a, b) => b.popularity - a.popularity);
        } else if (sortOption === "Best Rated") {
            filtered.sort((a, b) => b.rating - a.rating);
        } else if (sortOption === "Alphabetical") {
            filtered.sort((a, b) => a.name.localeCompare(b.name));
        }

        return filtered;
    };

    const filteredRestaurants = filterRestaurants();

    const filterPanel = (
        <div className="bg-base-100 p-4 sm:p-6 space-y-4 sm:space-y-6">
            <h2 className="font-display text-lg sm:text-xl font-semibold text-neutral">Filters</h2>
            
            {/* Quick Filters */}
            <div className="space-y-3">
                <h3 className="font-medium text-neutral text-sm sm:text-base">Quick Filters</h3>
                <div className="flex flex-col gap-2 sm:gap-3">
                    <label className="flex items-center cursor-pointer">
                        <input
                            type="checkbox"
                            className="checkbox checkbox-primary checkbox-sm sm:checkbox-md mr-2 sm:mr-3"
                            checked={openFilter}
                            onChange={(e) => setOpenFilter(e.target.checked)}
                        />
                        <span className="text-neutral text-sm sm:text-base">Open Now</span>
                    </label>
                    <label className="flex items-center cursor-pointer">
                        <input
                            type="checkbox"
                            className="checkbox checkbox-primary checkbox-sm sm:checkbox-md mr-2 sm:mr-3"
                            checked={freeDeliveryFilter}
                            onChange={(e) => setFreeDeliveryFilter(e.target.checked)}
                        />
                        <span className="text-neutral text-sm sm:text-base">Free Delivery</span>
                    </label>
                </div>
            </div>
            
            {/* Cuisine Types */}
            <div>
                <h3 className="font-medium mb-2 sm:mb-3 text-neutral text-sm sm:text-base">Cuisine Types</h3>
                <div className="space-y-2 max-h-48 sm:max-h-64 overflow-y-auto">
                    {allCategories.map((category, index) => (
                        <label key={index} className="flex items-start cursor-pointer">
                            <input
                                type="checkbox"
                                className="checkbox checkbox-primary checkbox-sm mr-3 flex-shrink-0 mt-0.5"
                                checked={selectedCategories.includes(category)}
                                onChange={() => toggleCategory(category)}
                            />
                            <span className="text-neutral text-sm leading-relaxed break-words">{category}</span>
                        </label>
                    ))}
                </div>
            </div>
            
            {/* Clear Filters */}
            {(selectedCategories.length > 0 || openFilter || freeDeliveryFilter) && (
                <button
                    onClick={() => {
                        setSelectedCategories([]);
                        setOpenFilter(false);
                        setFreeDeliveryFilter(false);
                    }}
                    className="btn btn-outline btn-sm w-full text-xs sm:text-sm"
                >
                    Clear All Filters
                </button>
            )}
        </div>
    );

    if (loading) {
        return <LoadingSpinner text="Loading restaurants..." />;
    }

    return (
        <div className="max-w-6xl mx-auto px-3 sm:px-4 md:px-6 lg:px-8 py-3 sm:py-4 md:py-6">
            <h1 className="font-display text-2xl sm:text-3xl md:text-4xl font-semibold text-neutral mb-4 sm:mb-6">Restaurants</h1>

            <div className="flex flex-col lg:flex-row gap-4 sm:gap-6">
                {/* Desktop Sidebar Filter */}
                <div className="hidden lg:block lg:w-1/4">
                    <div className="card bg-base-100 shadow-md rounded-2xl sticky top-4">{filterPanel}</div>
                </div>

                {/* Main Content */}
                <div className="flex-1">
                    {/* Search and Controls - Mobile First */}
                    <div className="mb-4 sm:mb-6 space-y-3 sm:space-y-4">
                        {/* Search Bar */}
                        <div className="flex items-center gap-2 bg-base-100 shadow-sm rounded-full px-3 sm:px-4 py-2 sm:py-3">
                            <svg className="w-4 h-4 sm:w-5 sm:h-5 text-neutral/60 flex-shrink-0" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                                <circle cx="11" cy="11" r="8"></circle>
                                <path d="m21 21-4.3-4.3"></path>
                            </svg>
                            <input
                                type="text"
                                placeholder="Search restaurants..."
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                                className="flex-1 bg-transparent text-neutral placeholder-neutral/60 focus:outline-none text-sm sm:text-base"
                            />
                        </div>
                        
                        {/* Controls Row */}
                        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2 sm:gap-4">
                            <select
                                value={sortOption}
                                onChange={(e) => setSortOption(e.target.value)}
                                className="select select-bordered select-sm sm:select-md bg-base-100 w-full sm:max-w-xs text-sm sm:text-base"
                            >
                                <option value="Most Popular">Most Popular</option>
                                <option value="Best Rated">Best Rated</option>
                                <option value="Alphabetical">Alphabetical</option>
                            </select>
                            <button
                                className="btn btn-primary btn-sm sm:btn-md rounded-full lg:hidden w-full sm:w-auto"
                                onClick={() => setFilterModalOpen(true)}
                            >
                                📊 Filters
                            </button>
                        </div>
                    </div>

                    {/* Restaurant Grid - Mobile First */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-3 sm:gap-4 md:gap-6">
                        {filteredRestaurants.map((restaurant) => {
                            const todayInfo = getTodayHours(restaurant.openingHours);
                            const isOpen = isRestaurantOpenNow(restaurant.openingHours);

                            return (
                                <RestaurantCard
                                    key={restaurant.id}
                                    id={restaurant.id}
                                    image={restaurant.logo?.downloadUrl}
                                    logo={restaurant.logo}
                                    name={restaurant.name}
                                    hours={todayInfo?.label || "Closed today"}
                                    rating={restaurant.rating || 0}
                                    deliveryFee={parseFloat(restaurant.deliveryFee || 0.00)}
                                    categories={restaurant.categories.map(c => c.name)}
                                    isOpen={isOpen}
                                />
                            );
                        })}
                    </div>
                    
                    {/* Empty State */}
                    {filteredRestaurants.length === 0 && (
                        <div className="text-center py-8 sm:py-12">
                            <div className="text-4xl sm:text-6xl mb-4">🍽️</div>
                            <h3 className="text-lg sm:text-xl font-semibold text-neutral mb-2">No restaurants found</h3>
                            <p className="text-sm sm:text-base text-neutral/70">Try adjusting your search or filters</p>
                        </div>
                    )}
                </div>
            </div>

            {/* Mobile Filter Modal */}
            {filterModalOpen && (
                <div className="modal modal-open">
                    <div className="modal-box max-w-sm sm:max-w-md mx-4">
                        <div className="flex items-center justify-between mb-4">
                            <h3 className="font-bold text-lg sm:text-xl">Filters</h3>
                            <button 
                                className="btn btn-ghost btn-circle btn-sm"
                                onClick={() => setFilterModalOpen(false)}
                            >
                                ✕
                            </button>
                        </div>
                        {filterPanel}
                        <div className="modal-action pt-4">
                            <button 
                                className="btn btn-primary w-full" 
                                onClick={() => setFilterModalOpen(false)}
                            >
                                Apply Filters
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Restaurants;
