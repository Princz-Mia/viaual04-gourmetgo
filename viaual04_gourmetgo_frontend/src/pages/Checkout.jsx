// src/pages/Checkout.jsx
import React, { useState, useContext, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useCart } from "../contexts/CartContext";
import { AuthContext } from "../contexts/AuthContext";
import { placeOrder } from "../api/orderService";
import { toast } from "react-toastify";
import { fetchPaymentMethods } from "../api/paymentService";
import { rewardBalanceRef } from "../components/Header";
import { rewardApi } from "../api/rewardApi";

const Checkout = () => {
  const { user } = useContext(AuthContext);
  const { items, total, coupon, pointsToRedeem, setPoints, clear: clearCart } = useCart();
  const navigate = useNavigate();

  const [sameAsBilling, setSameAsBilling] = useState(true);
  const [billing, setBilling] = useState({
    fullName: "",
    phoneNumber: "",
    postalCode: "",
    city: "",
    unitNumber: "",
    addressLine: "",
    region: "",
  });
  const [shipping, setShipping] = useState({ ...billing });
  const [paymentMethods, setPaymentMethods] = useState([]);
  const [selectedPayment, setSelectedPayment] = useState(null);
  const [notes, setNotes] = useState("");
  const [userRewardBalance, setUserRewardBalance] = useState(0);


  useEffect(() => {
    // Redirect to cart if empty
    if (!items || items.length === 0) {
      toast.error("Your cart is empty. Please add items before checkout.");
      navigate("/cart");
      return;
    }

    (async () => {
      try {
        const [methods, rewardResponse] = await Promise.all([
          fetchPaymentMethods(),
          rewardApi.getRewardBalance(user.id)
        ]);
        setPaymentMethods(methods);
        const rewardData = rewardResponse.data?.data || rewardResponse.data || {};
        setUserRewardBalance(rewardData.balance || 0);
      } catch {
        toast.error("Failed to load payment methods");
      }
    })();
  }, [items, navigate]);

  const handleBillingChange = (e) => {
    const { name, value } = e.target;
    setBilling((prev) => ({ ...prev, [name]: value }));
  };

  const handleShippingChange = (e) => {
    const { name, value } = e.target;
    setShipping((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validation checks
    if (!items.length) {
      toast.error("Your cart is empty.");
      return;
    }
    if (!selectedPayment) {
      toast.error("Please select a payment method.");
      return;
    }
    if (pointsToRedeem < 0) {
      toast.error("Reward points cannot be negative.");
      return;
    }
    if (pointsToRedeem > userRewardBalance) {
      toast.error(`You only have ${userRewardBalance} reward points available.`);
      return;
    }
    if (coupon && (!coupon.code || !coupon.value)) {
      toast.error("Invalid coupon applied.");
      return;
    }

    const orderDto = {
      coupon: coupon ? coupon : null,
      orderNotes: notes,
      deliveryInstructions: notes,
      paymentMethod: { id: selectedPayment },
      billingInformation: {
        fullName: billing.fullName,
        phoneNumber: billing.phoneNumber,
        address: { ...billing },
      },
      shippingInformation: {
        fullName: sameAsBilling ? billing.fullName : shipping.fullName,
        phoneNumber: sameAsBilling ? billing.phoneNumber : shipping.phoneNumber,
        address: sameAsBilling ? { ...billing } : { ...shipping },
      },
      orderItems: items.map((it) => ({
        productId: it.id,
        quantity: it.quantity,
        unitPrice: it.price,
      })),
      totalAmount: total,
      restaurant: items[0].restaurant,
      pointsToRedeem: pointsToRedeem,
    };

    const created = await placeOrder(orderDto);
    if (created) {
      toast.success("Order placed successfully!");
      clearCart();
      // Clear points from cart context if points were used
      if (pointsToRedeem > 0) {
        setPoints(0);
      }
      // Always refresh reward balance after order (customer earns points)
      if (rewardBalanceRef?.current) {
        setTimeout(() => {
          rewardBalanceRef.current.refresh();
        }, 1000);
      }

      navigate("/orders");
    }
  };
  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex items-center mb-6">
        <button
          onClick={() => navigate('/cart')}
          className="mr-4 p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
        >
          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 className="text-3xl font-bold">Checkout</h1>
      </div>
      <form onSubmit={handleSubmit} className="space-y-8">
        {/* Billing */}
        <section className="bg-white p-6 rounded-lg shadow-lg">
          <h2 className="text-2xl font-semibold mb-4">Billing Information</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {[
              ["fullName", "Full Name"],
              ["phoneNumber", "Phone Number"],
              ["postalCode", "Postal Code"],
              ["region", "Region"],
              ["city", "City"],
              ["addressLine", "Address Line"],
              ["unitNumber", "Unit Number"],
            ].map(([field, placeholder]) => (
              <input
                key={field}
                name={field}
                value={billing[field]}
                onChange={handleBillingChange}
                placeholder={placeholder}
                className="input input-bordered w-full"
                required
              />
            ))}
          </div>
        </section>

        {/* Shipping */}
        <section className="bg-white p-6 rounded-lg shadow-lg">
          <h2 className="text-2xl font-semibold mb-4">Shipping Information</h2>
          <label className="flex items-center mb-4">
            <input
              type="checkbox"
              checked={sameAsBilling}
              onChange={() => setSameAsBilling((p) => !p)}
              className="checkbox checkbox-primary mr-2"
            />
            Shipping same as billing
          </label>
          {!sameAsBilling && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {[
                ["fullName", "Receiver Name"],
                ["phoneNumber", "Receiver Phone"],
                ["postalCode", "Postal Code"],
                ["city", "City"],
                ["streetNumber", "Street Number"],
                ["addressLine", "Address Line"],
                ["region", "Region"],
              ].map(([f, ph]) => (
                <input
                  key={f}
                  name={f}
                  value={shipping[f]}
                  onChange={handleShippingChange}
                  placeholder={ph}
                  className="input input-bordered w-full"
                  required
                />
              ))}
            </div>
          )}
        </section>

        {/* Payment */}
        <section className="bg-white p-6 rounded-lg shadow-lg">
          <h2 className="text-2xl font-semibold mb-4">Payment Method</h2>
          <div className="flex flex-col gap-2">
            {paymentMethods.map((m) => (
              <label key={m.id} className="flex items-center">
                <input
                  type="radio"
                  name="payment"
                  className="radio radio-primary mr-2"
                  value={m.id}
                  checked={selectedPayment === m.id}
                  onChange={() => setSelectedPayment(m.id)}
                />
                {m.name}
              </label>
            ))}
          </div>
        </section>



        {/* Notes */}
        <section className="bg-white p-6 rounded-lg shadow-lg">
          <h2 className="text-2xl font-semibold mb-4">Order Notes</h2>
          <textarea
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Anything else?"
            className="textarea textarea-bordered w-full"
            rows={4}
          />
        </section>

        {/* Order Summary */}
        <section className="bg-white p-6 rounded-lg shadow-lg">
          <h2 className="text-2xl font-semibold mb-4">Order Summary</h2>
          <div className="space-y-2">
            <div className="flex justify-between">
              <span>Subtotal:</span>
              <span>${total.toFixed(2)}</span>
            </div>
            {items[0]?.restaurant?.deliveryFee && (
              <div className="flex justify-between">
                <span>Delivery Fee:</span>
                <span>${parseFloat(items[0].restaurant.deliveryFee).toFixed(2)}</span>
              </div>
            )}
            {coupon && (
              <div className="flex justify-between text-green-600">
                <span>Coupon Discount ({coupon.code}):</span>
                <span>-${coupon.type === 'AMOUNT' ? coupon.value.toFixed(2) : (coupon.type === 'FREE_SHIP' && items[0]?.restaurant?.deliveryFee ? parseFloat(items[0].restaurant.deliveryFee).toFixed(2) : '0.00')}</span>
              </div>
            )}
            {pointsToRedeem > 0 && (
              <div className="flex justify-between text-blue-600">
                <span>Reward Points ({pointsToRedeem} pts):</span>
                <span>-${(pointsToRedeem * 0.10).toFixed(2)}</span>
              </div>
            )}
            <hr />
            <div className="flex justify-between font-bold text-lg">
              <span>Total:</span>
              <span>${(() => {
                let finalTotal = total;
                if (items[0]?.restaurant?.deliveryFee) {
                  finalTotal += parseFloat(items[0].restaurant.deliveryFee);
                }
                if (coupon) {
                  if (coupon.type === 'AMOUNT') {
                    finalTotal -= coupon.value;
                  } else if (coupon.type === 'FREE_SHIP' && items[0]?.restaurant?.deliveryFee) {
                    finalTotal -= parseFloat(items[0].restaurant.deliveryFee);
                  }
                }
                if (pointsToRedeem > 0) {
                  finalTotal -= (pointsToRedeem * 0.10);
                }
                return Math.max(0, finalTotal).toFixed(2);
              })()}</span>
            </div>
          </div>
        </section>

        {/* Submit */}
        <div className="text-center">
          <button type="submit" className="btn btn-accent btn-lg">
            Place Order
          </button>
        </div>
      </form>
    </div>
  );
};

export default Checkout;