// Component representing a single past order
const OrderItem = ({ id, restaurant, date, total, status }) => {
    // Determine badge style based on status
    const statusClass = {
        Delivered: "badge badge-success",
        "In Progress": "badge badge-warning",
        Cancelled: "badge badge-error",
        Pending: "badge badge-info",
    }[status] || "badge badge-neutral";

    return (
        <div className="bg-white shadow-lg rounded-lg p-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            {/* Order ID and Restaurant */}
            <div className="flex-1">
                <p className="text-sm text-gray-500">
                    Order ID: <span className="font-mono">{id}</span>
                </p>
                <h3 className="text-lg font-semibold text-gray-800">{restaurant}</h3>
            </div>

            {/* Date */}
            <div className="text-gray-600 w-32 flex-shrink-0 justify-items-center">
                <p className="text-sm">Date</p>
                <p className="font-medium">{new Date(date).toLocaleDateString()}</p>
            </div>

            {/* Total Amount */}
            <div className="text-gray-600 w-32 flex-shrink-0 justify-items-center">
                <p className="text-sm">Total</p>
                <p className="font-medium">${total.toFixed(2)}</p>
            </div>

            {/* Status Badge */}
            <div className="text-right w-24 flex-shrink-0">
                <span className={statusClass}>{status}</span>
            </div>
        </div>
    );
};

export default OrderItem;
