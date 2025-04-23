import React, { useState } from "react";
import RestaurantCard from "../components/RestaurantCard";

const mockRestaurants = [
    {
        id: 1,
        name: "La Bella Italia",
        image: "https://source.unsplash.com/featured/?italian,restaurant",
        hours: "11:00 - 23:00",
        rating: 4.5,
        categories: ["Italian", "Pasta", "Mediterranean"],
        isOpen: true,
        deliveryFee: 7.20,
        popularity: 200,
    },
    {
        id: 2,
        name: "Dragon Express",
        image: "https://source.unsplash.com/featured/?chinese,restaurant",
        hours: "10:00 - 22:00",
        rating: 4.2,
        categories: ["Chinese", "Asian", "Noodles"],
        isOpen: false,
        deliveryFee: 0,
        popularity: 150,
    },
    {
        id: 3,
        name: "Spice Route",
        image: "https://source.unsplash.com/featured/?indian,restaurant",
        hours: "09:00 - 21:00",
        rating: 4.8,
        categories: ["Indian", "Curry", "Vegan"],
        isOpen: true,
        deliveryFee: 3.70,
        popularity: 250,
    },
    {
        id: 4,
        name: "Taco Fiesta",
        image: "https://source.unsplash.com/featured/?mexican,restaurant",
        hours: "12:00 - 00:00",
        rating: 4.0,
        categories: ["Mexican", "Hamburger", "American"],
        isOpen: true,
        deliveryFee: 0,
        popularity: 180,
    },
    {
        id: 5,
        name: "Green Leaf",
        image: "https://source.unsplash.com/featured/?vegan,restaurant",
        hours: "10:00 - 20:00",
        rating: 4.6,
        categories: ["Vegan", "Healthy", "Organic"],
        isOpen: true,
        deliveryFee: 2.50,
        popularity: 220,
    },
    // További mock éttermek hozzáadhatók...
];

const Restaurants = () => {
    // Állapotok a kereséshez, szűréshez és rendezéshez
    const [search, setSearch] = useState("");
    const [selectedCategories, setSelectedCategories] = useState([]);
    const [openFilter, setOpenFilter] = useState(false);
    const [freeDeliveryFilter, setFreeDeliveryFilter] = useState(false);
    const [sortOption, setSortOption] = useState("Most Popular");
    const [filterModalOpen, setFilterModalOpen] = useState(false);

    // Összegyűjtjük az összes elérhető kategóriát a mock adatokból.
    const allCategories = [
        ...new Set(mockRestaurants.flatMap((r) => r.categories)),
    ];

    // Kategória kiválasztásának kezelése.
    const toggleCategory = (category) => {
        if (selectedCategories.includes(category)) {
            setSelectedCategories(selectedCategories.filter((c) => c !== category));
        } else {
            setSelectedCategories([...selectedCategories, category]);
        }
    };

    // Szűrés és rendezés logikája.
    const filterRestaurants = () => {
        let filtered = [...mockRestaurants];

        // Keresés étterem név alapján (kis- és nagybetű érzéketlen).
        if (search.trim() !== "") {
            filtered = filtered.filter((r) =>
                r.name.toLowerCase().includes(search.toLowerCase())
            );
        }

        // Konyhatípusok szerinti szűrés: ha van legalább egy kiválasztott kategória,
        // akkor az étteremnek tartalmaznia kell legalább egyet.
        if (selectedCategories.length > 0) {
            filtered = filtered.filter((r) =>
                selectedCategories.some((cat) => r.categories.includes(cat))
            );
        }

        // Nyitva van-e szűrés.
        if (openFilter) {
            filtered = filtered.filter((r) => r.isOpen === true);
        }

        // Ingyenes kiszállítás szűrés.
        if (freeDeliveryFilter) {
            filtered = filtered.filter((r) => r.deliveryFee.toFixed(2) == 0.00);
        }

        // Rendezés a választott opció alapján.
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

    // Szűrőpanel JSX, amelyet asztali sidebarban és mobil modalban használunk.
    const filterPanel = (
        <div className="bg-white p-6 space-y-4">
            <h2 className="text-xl font-semibold">Filters</h2>
            {/* Elérhetőség: Nyitva és ingyenes kiszállítás */}
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
            {/* Konyhatípusok (többszörös kiválasztás) */}
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

    return (
        <div className="container mx-auto px-4 py-8">
            <h1 className="text-3xl font-bold mb-6">Restaurants</h1>

            <div className="flex flex-col md:flex-row gap-6">
                {/* Asztali nézetben a bal oldali szűrőpanel */}
                <div className="hidden md:block md:w-1/4">
                    <div className="card bg-base-100 shadow-xl">{filterPanel}</div>
                </div>

                <div className="flex-1">
                    {/* Kereső és rendezési opciók */}
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
                        {/* Mobil nézetben szűrés gomb */}
                        <button
                            className="btn btn-primary block sm:hidden"
                            onClick={() => setFilterModalOpen(true)}
                        >
                            Filters
                        </button>
                    </div>

                    {/* Étterem kártyák rácsos elrendezése – kissé szorosabban */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                        {filteredRestaurants.map((restaurant) => (
                            <RestaurantCard
                                key={restaurant.id}
                                image={restaurant.image}
                                name={restaurant.name}
                                hours={restaurant.hours}
                                rating={restaurant.rating}
                                deliveryFee={restaurant.deliveryFee}
                                categories={restaurant.categories}
                                isOpen={restaurant.isOpen}
                            />
                        ))}
                    </div>
                </div>
            </div>

            {/* Mobil szűrő modal */}
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
