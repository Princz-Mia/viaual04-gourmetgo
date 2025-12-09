import { useState, useEffect } from "react";

const ReviewModal = ({ isOpen, onClose, onSubmit, existingReview }) => {
    const [rating, setRating] = useState(existingReview?.ratingValue || 5);
    const [comment, setComment] = useState(existingReview?.comment || "");

    const handleSubmit = () => {
        onSubmit({ rating, comment });
        if (!existingReview) {
            setRating(5);
            setComment("");
        }
    };

    // Update form when existingReview changes
    useEffect(() => {
        if (existingReview) {
            setRating(existingReview.ratingValue);
            setComment(existingReview.comment || "");
        } else {
            setRating(5);
            setComment("");
        }
    }, [existingReview]);

    if (!isOpen) return null;
    return (
        <div className="modal modal-open">
            <div className="modal-box max-w-lg">
                <h3 className="font-bold text-lg mb-4">{existingReview ? 'Edit Review' : 'Write a Review'}</h3>
                <div className="mb-4">
                    <label className="block mb-2">Rating</label>
                    <select
                        value={rating}
                        onChange={(e) => setRating(+e.target.value)}
                        className="select select-bordered w-full"
                    >
                        {[5, 4, 3, 2, 1].map(val => (
                            <option key={val} value={val}>{val} Star{val > 1 ? 's' : ''}</option>
                        ))}
                    </select>
                </div>
                <div className="mb-4">
                    <label className="block mb-2">Comment (optional)</label>
                    <textarea
                        rows={4}
                        className="textarea textarea-bordered w-full"
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                        placeholder="Share your experience..."
                    />
                </div>
                <div className="modal-action">
                    <button className="btn btn-outline" onClick={onClose}>Cancel</button>
                    <button className="btn btn-primary" onClick={handleSubmit}>{existingReview ? 'Update' : 'Submit'}</button>
                </div>
            </div>
        </div>
    );
};

export default ReviewModal;