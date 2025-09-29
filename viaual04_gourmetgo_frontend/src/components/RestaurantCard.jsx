import { useNavigate } from "react-router-dom";
import imageNotFound from "../assets/images/image_not_found.jpg"

const RestaurantCard = ({
    id,
    image,
    name,
    hours,
    rating,
    deliveryFee,
    categories,
    isOpen,
}) => {
    const navigate = useNavigate();

    const mainCategory = categories && categories.length > 0 ? categories[0] : "N/A";
    const deliveryInfo =
        deliveryFee.toFixed(2) === "0.00"
            ? "Free Delivery"
            : `Delivery Fee: $${deliveryFee.toFixed(2)}`;

    const handleClick = () => {
        if (isOpen) {
            navigate(`/restaurant/${id}`);
        }
    };

    return (
        <div
            onClick={handleClick}
            className={`card bg-white opacity-80 shadow-xl max-w-[350px] transform transition-transform duration-300 ${isOpen
                ? "cursor-pointer hover:opacity-100"
                : "opacity-25 pointer-events-none"
                }`}
        >
            <figure>
                <img src={
                    image
                        ? `http://localhost:8080${image}`
                        : imageNotFound
                } alt={name} className="w-full h-48 object-cover"
                    onError={(e) => {
                        e.currentTarget.onerror = null; // végtelen ciklus elkerülése
                        e.currentTarget.src = imageNotFound;
                    }} />
            </figure>
            <div className="p-4">
                <h3 className="text-xl font-bold mb-1">{name}</h3>
                <p className="text-sm text-gray-600 mb-1">Cuisine: {mainCategory}</p>
                <p className="text-sm text-gray-600 mb-1">Hours: {hours}</p>
                <div className="flex items-center mb-2">
                    <div className="flex items-center">
                        {Array.from({ length: 5 }).map((_, i) => {
                            let starType;
                            if (i < Math.floor(rating)) {
                                starType = "full";
                            } else if (i < rating) {
                                starType = "half";
                            } else {
                                starType = "empty";
                            }
                            return (
                                <svg
                                    key={i}
                                    xmlns="http://www.w3.org/2000/svg"
                                    className="w-5 h-5"
                                    viewBox="0 0 20 20"
                                    fill={starType === "empty" ? "gray" : "gold"}
                                >
                                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.952a1 1 0 00.95.69h4.17c.969 0 1.371 1.24.588 1.81l-3.375 2.455a1 1 0 00-.363 1.118l1.287 3.95c.3.92-.755 1.688-1.538 1.118l-3.375-2.455a1 1 0 00-1.175 0l-3.375 2.455c-.783.57-1.838-.197-1.538-1.118l1.287-3.95a1 1 0 00-.363-1.118L2.044 9.38c-.783-.57-.38-1.81.588-1.81h4.17a1 1 0 00.95-.69l1.286-3.952z" />
                                </svg>
                            );
                        })}
                    </div>
                    <span className="ml-2 text-sm text-gray-700">{rating.toFixed(1)}/5</span>
                </div>
                <p className="text-sm text-gray-600">{deliveryInfo}</p>
            </div>
        </div>
    );
};

export default RestaurantCard;
