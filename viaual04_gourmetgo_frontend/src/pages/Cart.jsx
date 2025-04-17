
import React, { useState } from "react";
import CartItem from "../components/CartItem";
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { Link } from "react-router-dom";

// Mock cart data for testing
const initialCart = [
  { id: 1, name: "Grilled Chicken Sandwich", image: "https://source.unsplash.com/featured/?chicken,sandwich", price: 8.99, quantity: 2 },
  { id: 2, name: "Classic Cheeseburger", image: "https://source.unsplash.com/featured/?cheeseburger", price: 9.49, quantity: 1 },
  { id: 3, name: "Chocolate Lava Cake", image: "https://source.unsplash.com/featured/?chocolate,cake", price: 5.5, quantity: 3 },
];

const Cart = () => {
  const [cartItems, setCartItems] = useState(initialCart);
  const [couponCode, setCouponCode] = useState("");
  const [appliedCoupon, setAppliedCoupon] = useState("");
  const [discount, setDiscount] = useState(0);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [pendingCoupon, setPendingCoupon] = useState("");

  const deliveryFee = 3.5;

  // Update quantity or remove item
  const handleQuantityChange = (id, qty) => {
    setCartItems(items =>
      items
        .map(item => item.id === id ? { ...item, quantity: qty } : item)
        .filter(item => item.quantity > 0)
    );
  };

  const handleRemove = (id) => {
    setCartItems(items => items.filter(item => item.id !== id));
  };

  // Try apply or request confirmation
  const applyCoupon = () => {
    const code = couponCode.trim().toUpperCase();
    if (!code) return;

    if (appliedCoupon && appliedCoupon !== code) {
      setPendingCoupon(code);
      setShowConfirmModal(true);
      return;
    }

    // Direct apply
    confirmApply(code);
  };

  const confirmApply = (code) => {
    if (code === "SAVE10") {
      setAppliedCoupon(code);
      setDiscount(0.1);
      toast.success(`Coupon ${code} applied!`);
    } else {
      toast.error(`Coupon ${code} is invalid.`);
      // Keep existing coupon
    }
    setShowConfirmModal(false);
    setPendingCoupon("");
  };

  const cancelApply = () => {
    setCouponCode(appliedCoupon);
    setPendingCoupon("");
    setShowConfirmModal(false);
  };

  // Calculate totals
  const subTotal = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const discountAmount = subTotal * discount;
  const totalAfterDiscount = subTotal - discountAmount;
  const grandTotal = totalAfterDiscount + deliveryFee;

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Your Cart</h1>

      <div className="flex flex-col lg:flex-row gap-8">
        {/* Cart items list */}
        <div className="flex-1 space-y-4">
          {cartItems.map(item => (
            <CartItem
              key={item.id}
              image={item.image}
              name={item.name}
              price={item.price}
              quantity={item.quantity}
              onQuantityChange={qty => handleQuantityChange(item.id, qty)}
              onRemove={() => handleRemove(item.id)}
            />
          ))}
          {cartItems.length === 0 && (
            <p className="text-center text-gray-500">Your cart is empty.</p>
          )}
        </div>

        {/* Summary panel */}
        <aside className="w-full lg:w-1/3 bg-base-200 p-6 rounded-lg shadow-lg space-y-6">
          <h2 className="text-xl font-semibold">Order Summary</h2>
          <ul className="space-y-2">
            {cartItems.map(item => (
              <li key={item.id} className="flex justify-between">
                <span>{item.name} x {item.quantity}</span>
                <span>${(item.price * item.quantity).toFixed(2)}</span>
              </li>
            ))}
          </ul>

          <div className="flex font-medium justify-between">
            <span>Subtotal:</span>
            <span>${subTotal.toFixed(2)}</span>
          </div>

          <div className="flex justify-between">
            <span>Delivery Fee:</span>
            <span>${deliveryFee.toFixed(2)}</span>
          </div>

          {/* Coupon Code */}
          <div>
            <label className="block font-medium mb-2">Coupon Code</label>
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="Enter code"
                value={couponCode}
                onChange={e => setCouponCode(e.target.value)}
                className="input input-bordered flex-1"
              />
              <button className="btn btn-primary" onClick={applyCoupon}>
                Apply
              </button>
            </div>
            {appliedCoupon && (
              <p className="text-sm text-green-700 mt-2">Applied: {appliedCoupon}</p>
            )}
          </div>

          {/* Totals */}
          {discount > 0 && (
            <div className="flex font-medium justify-between text-green-600">
              <span>Discount:</span>
              <span>-${discountAmount.toFixed(2)}</span>
            </div>
          )}
          <div className="flex justify-between font-bold text-lg">
            <span>Total:</span>
            <span>${grandTotal.toFixed(2)}</span>
          </div>

          {/* Checkout button */}
          <Link to="/checkout">
            <button className="btn btn-accent w-full">Proceed to Checkout</button>
          </Link>

          {/* Optional: Delivery Instructions */}
          <div className="mt-6">
            <label className="block font-medium mb-2">Order Notes</label>
            <textarea
              className="textarea textarea-bordered w-full"
              placeholder="Add a note for the restaurant"
            />
          </div>
        </aside>
      </div>

      {/* Confirmation Modal */}
      {showConfirmModal && (
        <div className="modal modal-open">
          <div className="modal-box">
            <h3 className="font-bold text-lg mb-4">Replace Coupon?</h3>
            <p className="mb-6">
              A coupon <span className="font-semibold">{appliedCoupon}</span> is already applied.
              Replace with <span className="font-semibold">{pendingCoupon}</span>?
            </p>
            <div className="modal-action">
              <button className="btn" onClick={cancelApply}>Cancel</button>
              <button className="btn btn-primary" onClick={() => confirmApply(pendingCoupon)}>Confirm</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Cart;

