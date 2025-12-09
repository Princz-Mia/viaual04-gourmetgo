import { useNavigate } from "react-router-dom";
import imageNotFound from "../assets/images/image_not_found.jpg";
import { getImageUrl } from "../api/imageService";

const RestaurantCard = ({
    id,
    image,
    logo,
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
        <article
            onClick={handleClick}
            className={`card bg-base-100 shadow-md hover:shadow-xl rounded-2xl overflow-hidden transition-all duration-300 border border-base-200 ${isOpen
                ? "cursor-pointer hover:-translate-y-1 hover:shadow-2xl active:scale-98"
                : "opacity-60 pointer-events-none"
                }`}
        >
            {/* Image */}
            <figure className="h-48 sm:h-52 overflow-hidden relative">
                <img 
                    src={
                        logo?.id
                            ? getImageUrl(logo.id)
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
                {!isOpen && (
                    <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
                        <span className="badge badge-error text-sm font-medium">Closed</span>
                    </div>
                )}
            </figure>
            
            {/* Content */}
            <div className="card-body p-5 gap-4">
                <div className="space-y-2">
                    <h3 className="card-title text-lg font-bold text-neutral leading-tight line-clamp-2">
                        {name}
                    </h3>
                    <p className="text-sm text-neutral/60 font-medium">{mainCategory}</p>
                </div>
                
                {/* Rating & Hours */}
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <div className="flex items-center gap-1 bg-amber-50 px-2 py-1 rounded-full">
                            <span className="text-amber-500">⭐</span>
                            <span className="text-sm font-semibold text-amber-700">{rating.toFixed(1)}</span>
                        </div>
                        <span className="text-sm text-neutral/60">{hours}</span>
                    </div>
                </div>
                
                {/* Delivery Info */}
                <div className="flex items-center justify-between pt-2 border-t border-base-200">
                    <span className="text-primary font-semibold text-sm">{deliveryInfo}</span>
                    <button className="btn btn-primary btn-sm rounded-full px-6">
                        View Menu
                    </button>
                </div>
            </div>
        </article>
    );
};

export default RestaurantCard;
