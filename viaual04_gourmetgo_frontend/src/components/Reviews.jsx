import ReviewItem from "./ReviewItem";
import ReviewModal from "./ReviewModal";

const Reviews = ({ onBack }) => {
  const [showReviews, setShowReviews] = useState(false);
  const [reviews, setReviews] = useState([
    { id: 1, user: 'Alice', date: '2025-04-01', rating: 5, comment: 'Excellent food and service!' },
    { id: 2, user: 'Bob', date: '2025-03-20', rating: 2, comment: 'Too salty for my taste.' },
    { id: 3, user: 'Carol', date: '2025-04-10', rating: 4, comment: 'Great ambiance.' },
  ]);
  const [sortKey, setSortKey] = useState('newest');
  const [modalOpen, setModalOpen] = useState(false);

  // Sorting logic
  const sorted = [...reviews].sort((a, b) => {
    switch (sortKey) {
      case 'best': return b.rating - a.rating;
      case 'worst': return a.rating - b.rating;
      case 'oldest': return new Date(a.date) - new Date(b.date);
      case 'newest':
      default: return new Date(b.date) - new Date(a.date);
    }
  });

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold">Reviews</h2>
        <div className="flex gap-2">
          <select
            value={sortKey}
            onChange={(e) => setSortKey(e.target.value)}
            className="select select-bordered"
          >
            <option value="newest">Newest</option>
            <option value="oldest">Oldest</option>
            <option value="best">Best</option>
            <option value="worst">Worst</option>
          </select>
          <button className="btn btn-sm btn-primary" onClick={() => setModalOpen(true)}>
            Add Review
          </button>
        </div>
      </div>
      <div className="space-y-4">
        {sorted.map(r => (
          <ReviewItem key={r.id} {...r} />
        ))}
      </div>
      <ReviewModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        onSubmit={(rev) => setReviews([rev, ...reviews])}
      />
    </div >
  );
};

export default Reviews;