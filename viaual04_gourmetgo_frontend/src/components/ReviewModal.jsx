import { useState } from "react";

const ReviewModal = ({ isOpen, onClose, onSubmit }) => {
    const [rating, setRating] = useState(5);
    const [comment, setComment] = useState("");

    const handleSubmit = () => {
        onSubmit({ id: Date.now(), user: 'You', date: new Date(), rating, comment });
        onClose();
        setRating(5);
        setComment("");
    };

    if (!isOpen) return null;
    return (
        <div className="modal modal-open">
            <div className="modal-box max-w-lg">
                <h3 className="font-bold text-lg mb-4">Write a Review</h3>
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
                    <button className="btn btn-primary" onClick={handleSubmit}>Submit</button>
                </div>
            </div>
        </div>
    );
};

export default ReviewModal;