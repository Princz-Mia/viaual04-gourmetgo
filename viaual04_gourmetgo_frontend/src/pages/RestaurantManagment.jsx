import React, { useState } from "react";
import ProductModal from "../components/ProductModal";

const initialRestaurant = {
  id: 1,
  name: "GourmetGo Bistro",
  hours: "10:00 AM - 10:00 PM",
  deliveryFee: 3.5,
  categories: ["Italian", "Bistro", "Dessert"],
};

const initialProducts = [
  { id: 1, name: "Grilled Chicken Sandwich", image: "https://source.unsplash.com/featured/?chicken,sandwich", description: "Tender grilled chicken...", price: 8.99, categories: ["Chicken", "Sandwich"] },
  { id: 2, name: "Classic Cheeseburger", image: "https://source.unsplash.com/featured/?cheeseburger", description: "Juicy beef patty...", price: 9.49, categories: ["Beef", "Burger"] },
];

const RestaurantManagement = () => {
  const [restaurant, setRestaurant] = useState(initialRestaurant);
  const [products, setProducts] = useState(initialProducts);
  const [showCreate, setShowCreate] = useState(false);
  const [showEdit, setShowEdit] = useState(false);
  const [current, setCurrent] = useState(null);
  const [showDelete, setShowDelete] = useState(false);

  const [selectedCategories, setSelectedCategories] = useState([]);
  const allCategories = [...new Set(products.flatMap(p => p.categories))];
  const filteredProducts = selectedCategories.length
    ? products.filter(p => p.categories.some(c => selectedCategories.includes(c)))
    : products;

  const [productModalOpen, setProductModalOpen] = useState(false);
  const [currentProduct, setCurrentProduct] = useState(null);

  const handleCardClick = product => {
    setCurrentProduct(product);
    setProductModalOpen(true);
  };

  const handleInfoChange = e => {
    const { name, value } = e.target;
    setRestaurant(prev => ({ ...prev, [name]: value }));
  };

  const openCreate = () => {
    setCurrent({ name: "", image: "", description: "", price: 0, categories: [] });
    setShowCreate(true);
  };
  const openEdit = p => { setCurrent({ ...p }); setShowEdit(true); };
  const openDelete = p => { setCurrent(p); setShowDelete(true); };

  const saveProduct = () => {
    if (current.id) {
      setProducts(ps => ps.map(p => p.id === current.id ? current : p));
    } else {
      setProducts(ps => [...ps, { ...current, id: Date.now() }]);
    }
    setShowCreate(false);
    setShowEdit(false);
  };
  const confirmDelete = () => {
    setProducts(ps => ps.filter(p => p.id !== current.id));
    setShowDelete(false);
  };

  const handleProdChange = e => {
    const { name, value } = e.target;
    if (name === "categories") {
      setCurrent(prev => ({ ...prev, categories: value.split(",").map(s => s.trim()) }));
    } else {
      setCurrent(prev => ({ ...prev, [name]: name === "price" ? parseFloat(value) : value }));
    }
  };

  const toggleCategory = cat => {
    setSelectedCategories(prev =>
      prev.includes(cat) ? prev.filter(c => c !== cat) : [...prev, cat]
    );
  };

  return (
    <div className="container mx-auto px-4 py-8 space-y-8">
      <section className="bg-white p-6 rounded-lg shadow">
        <h2 className="text-2xl font-bold mb-4">Edit Restaurant Info</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <input name="name" value={restaurant.name} onChange={handleInfoChange}
            className="input input-bordered" placeholder="Name" />
          <input name="hours" value={restaurant.hours} onChange={handleInfoChange}
            className="input input-bordered" placeholder="Hours" />
          <input name="deliveryFee" type="number" step="0.01"
            value={restaurant.deliveryFee} onChange={handleInfoChange}
            className="input input-bordered" placeholder="Delivery Fee" />
          <input name="categories" value={restaurant.categories.join(", ")}
            onChange={e => setRestaurant(prev => ({ ...prev, categories: e.target.value.split(",").map(s => s.trim()) }))}
            className="input input-bordered" placeholder="Categories (comma-separated)" />
        </div>
      </section>

      <section className="bg-white p-6 rounded-lg shadow space-y-4">
        <div className="flex flex-col md:flex-row md:justify-between md:items-center mb-4 gap-4">
          <h2 className="text-2xl font-bold">Manage Products</h2>
          <button className="btn btn-primary" onClick={openCreate}>New Product</button>
        </div>
        <div className="flex flex-col md:flex-row gap-6">
          <aside className="w-full md:w-1/4 bg-base-100 p-4 rounded-lg shadow-md">
            <h3 className="font-semibold mb-2">Filter by Type</h3>
            <div className="flex flex-col space-y-2">
              {allCategories.map((cat, idx) => (
                <label key={idx} className="flex items-center">
                  <input type="checkbox" className="checkbox checkbox-primary mr-2"
                    checked={selectedCategories.includes(cat)}
                    onChange={() => toggleCategory(cat)}
                  />
                  <span>{cat}</span>
                </label>
              ))}
            </div>
          </aside>

          <main className="flex-1 space-y-4">
            {filteredProducts.map(p => (
              <div key={p.id} className="flex items-start bg-white rounded-lg shadow p-4 gap-4">
                <div className="w-32 flex-shrink-0">
                  <img src={p.image} alt={p.name} className="w-full h-24 object-cover rounded" />
                </div>
                <div className="flex-1">
                  <h4 className="text-lg font-semibold mb-1">{p.name}</h4>
                  <p className="text-gray-600 mb-2 text-sm" style={{ display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                    {p.description}
                  </p>
                  <p className="font-bold text-blue-600">${p.price.toFixed(2)}</p>
                </div>
                <div className="flex flex-col space-y-2">
                  <button className="btn btn-outline btn-sm" onClick={() => openEdit(p)}>Edit</button>
                  <button className="btn btn-error btn-sm" onClick={() => openDelete(p)}>Delete</button>
                  <button className="btn btn-ghost btn-sm" onClick={() => handleCardClick(p)}>View</button>
                </div>
              </div>
            ))}
          </main>
        </div>
      </section>

      {(showCreate || showEdit) && (
        <div className="modal modal-open">
          <div className="modal-box max-w-xl">
            <h3 className="font-bold text-lg mb-4">{showCreate ? 'New' : 'Edit'} Product</h3>
            <div className="space-y-3">
              <input name="name" value={current.name} onChange={handleProdChange}
                className="input input-bordered w-full" placeholder="Name" />
              <input name="image" value={current.image} onChange={handleProdChange}
                className="input input-bordered w-full" placeholder="Image URL" />
              <textarea name="description" value={current.description} onChange={handleProdChange}
                className="textarea textarea-bordered w-full" placeholder="Description" rows={3} />
              <input name="price" type="number" step="0.01" value={current.price}
                onChange={handleProdChange} className="input input-bordered w-full" placeholder="Price" />
              <input name="categories" value={current.categories.join(", ")} onChange={handleProdChange}
                className="input input-bordered w-full" placeholder="Categories (comma-separated)" />
            </div>
            <div className="modal-action">
              <button className="btn" onClick={() => { setShowCreate(false); setShowEdit(false); }}>
                Cancel
              </button>
              <button className="btn btn-primary" onClick={saveProduct}>Save</button>
            </div>
          </div>
        </div>
      )}

      {showDelete && (
        <div className="modal modal-open">
          <div className="modal-box">
            <h3 className="font-bold text-lg mb-4">Delete Product?</h3>
            <p className="mb-6">Are you sure you want to delete "{current.name}"?</p>
            <div className="modal-action">
              <button className="btn" onClick={() => setShowDelete(false)}>Cancel</button>
              <button className="btn btn-error" onClick={confirmDelete}>Delete</button>
            </div>
          </div>
        </div>
      )}

      <ProductModal
        isOpen={productModalOpen}
        onClose={() => setProductModalOpen(false)}
        product={currentProduct}
      />
    </div>
  );
};

export default RestaurantManagement;