// src/pages/Checkout.jsx
import React, { useState, useContext, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useCart } from "../contexts/CartContext";
import { AuthContext } from "../contexts/AuthContext";
import { placeOrder } from "../api/orderService";
import { toast } from "react-toastify";
import { fetchPaymentMethods } from "../api/paymentService";

const Checkout = () => {
  const { user } = useContext(AuthContext);
  const { items, total, coupon, clear: clearCart } = useCart();
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

  useEffect(() => {
    (async () => {
      try {
        const methods = await fetchPaymentMethods();
        setPaymentMethods(methods);
      } catch {
        toast.error("Failed to load payment methods");
      }
    })();
  }, []);

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
    if (!items.length) {
      toast.error("Your cart is empty.");
      return;
    }
    if (!selectedPayment) {
      toast.error("Please select a payment method.");
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
    };

    const created = await placeOrder(orderDto);
    if (created) {
      toast.success("Order placed successfully!");
      clearCart();
      navigate("/orders");
    }
  };
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Checkout</h1>
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