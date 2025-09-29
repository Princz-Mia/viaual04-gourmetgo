const ReviewItem = ({ customer, createdAt, ratingValue, comment }) => {
    return (
        <div className="bg-white p-4 rounded-lg shadow flex flex-col sm:flex-row sm:items-start gap-4">
            {/* Star rating */}
            <div className="flex items-center gap-1">
                {Array.from({ length: 5 }).map((_, i) => (
                    <svg
                        key={i}
                        xmlns="http://www.w3.org/2000/svg"
                        className={`w-5 h-5 ${i < ratingValue ? 'text-yellow-400' : 'text-gray-300'}`}
                        viewBox="0 0 20 20"
                        fill="currentColor"
                    >
                        <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.952a1 1 0 00.95.69h4.17c.969 0
                1.371 1.24.588 1.81l-3.375 2.455a1 1 0 00-.363 1.118l1.287
                3.95c.3.92-.755 1.688-1.538 1.118l-3.375-2.455a1
                1 0 00-1.175 0l-3.375 2.455c-.783.57-1.838-.197-1.538-1.118l1.287-3.95a1
                1 0 00-.363-1.118L2.044 9.38c-.783-.57-.38-1.81.588-1.81h4.17a1
                1 0 00.95-.69l1.286-3.952z" />
                    </svg>
                ))}
            </div>
            <div className="flex-1">
                <div className="flex justify-between items-center">
                    <p className="font-semibold text-gray-800">{customer.fullName}</p>
                    <p className="text-sm text-gray-500">{new Date(createdAt).toLocaleDateString()}</p>
                </div>
                {comment && <p className="mt-2 text-gray-700">{comment}</p>}
            </div>
        </div>
    );
};

export default ReviewItem;
