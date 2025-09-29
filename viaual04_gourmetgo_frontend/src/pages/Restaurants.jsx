import React, { useEffect, useState } from "react";
import RestaurantCard from "../components/RestaurantCard";
import { fetchRestaurants } from "../api/restaurantService";
import { getTodayHours, isRestaurantOpenNow } from "../utils/isOpen";
import { toast } from "react-toastify";
import imageNotFound from '../assets/images/image_not_found.jpg';
import { useLocation } from "react-router-dom";

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
        <div className="bg-white p-6 space-y-4">
            <h2 className="text-xl font-semibold">Filters</h2>
            <div className="flex flex-col gap-2">
                <label className="flex items-center">
                    <input
                        type="checkbox"
                        className="checkbox checkbox-primary mr-2"
                        checked={openFilter}
                        onChange={(e) => setOpenFilter(e.target.checked)}
                    />
                    <span>Open Now</span>
                </label>
                <label className="flex items-center">
                    <input
                        type="checkbox"
                        className="checkbox checkbox-primary mr-2"
                        checked={freeDeliveryFilter}
                        onChange={(e) => setFreeDeliveryFilter(e.target.checked)}
                    />
                    <span>Free Delivery</span>
                </label>
            </div>
            <div>
                <label className="block font-medium mb-1">Cuisine Types:</label>
                <div className="grid grid-cols-2 gap-4">
                    {allCategories.map((category, index) => (
                        <label key={index} className="flex items-center">
                            <input
                                type="checkbox"
                                className="checkbox checkbox-primary mr-2"
                                checked={selectedCategories.includes(category)}
                                onChange={() => toggleCategory(category)}
                            />
                            <span>{category}</span>
                        </label>
                    ))}
                </div>
            </div>
        </div>
    );

    if (loading) return <p className="text-center">Loading restaurants...</p>;

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold mb-6">Restaurants</h1>

            <div className="flex flex-col md:flex-row gap-6">
                <div className="hidden md:block md:w-1/4">
                    <div className="card bg-base-100 shadow-xl">{filterPanel}</div>
                </div>

                <div className="flex-1">
                    <div className="mb-6 flex flex-col sm:flex-row items-center gap-4">
                        <input
                            type="text"
                            placeholder="Search by restaurant name..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            className="input input-bordered input-primary bg-white w-full sm:max-w-md"
                        />
                        <select
                            value={sortOption}
                            onChange={(e) => setSortOption(e.target.value)}
                            className="bg-white select select-bordered select-primary w-full sm:max-w-xs"
                        >
                            <option value="Most Popular">Most Popular</option>
                            <option value="Best Rated">Best Rated</option>
                            <option value="Alphabetical">Alphabetical</option>
                        </select>
                        <button
                            className="btn btn-primary block sm:hidden"
                            onClick={() => setFilterModalOpen(true)}
                        >
                            Filters
                        </button>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                        {filteredRestaurants.map((restaurant) => {
                            const todayInfo = getTodayHours(restaurant.openingHours);
                            const isOpen = isRestaurantOpenNow(restaurant.openingHours);

                            return (
                                <RestaurantCard
                                    key={restaurant.id}
                                    id={restaurant.id}
                                    image={restaurant.logo?.downloadUrl}
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
                </div>
            </div>

            {filterModalOpen && (
                <div className="modal modal-open">
                    <div className="modal-box">
                        <h3 className="font-bold text-lg mb-4">Filters</h3>
                        {filterPanel}
                        <div className="modal-action">
                            <button className="btn" onClick={() => setFilterModalOpen(false)}>
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Restaurants;
