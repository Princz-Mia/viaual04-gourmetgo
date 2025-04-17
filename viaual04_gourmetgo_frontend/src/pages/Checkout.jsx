import React, { useState } from "react";

const Checkout = () => {
  const [sameAsBilling, setSameAsBilling] = useState(true);
  const [billing, setBilling] = useState({ name: "", phone: "", address: "" });
  const [shipping, setShipping] = useState({ name: "", phone: "", address: "" });
  const [paymentMethod, setPaymentMethod] = useState("cod");
  const [notes, setNotes] = useState("");

  const handleBillingChange = (e) => {
    const { name, value } = e.target;
    setBilling((prev) => ({ ...prev, [name]: value }));
  };

  const handleShippingChange = (e) => {
    const { name, value } = e.target;
    setShipping((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Here you would send data to backend
    const orderData = {
      billing,
      shipping: sameAsBilling ? billing : shipping,
      paymentMethod,
      notes,
    };
    console.log("Order submitted:", orderData);
    alert("Order submitted! Check console for data.");
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Checkout</h1>
      <form onSubmit={handleSubmit} className="space-y-8">
        {/* Billing Information */}
        <section className="bg-white p-6 rounded-lg shadow-lg">
          <h2 className="text-2xl font-semibold mb-4">Billing Information</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <input
              type="text"
              name="name"
              placeholder="Full Name"
              value={billing.name}
              onChange={handleBillingChange}
              className="input input-bordered w-full"
              required
            />
            <input
              type="tel"
              name="phone"
              placeholder="Phone Number"
              value={billing.phone}
              onChange={handleBillingChange}
              className="input input-bordered w-full"
              required
            />
            <input
              type="text"
              name="postalCode"
              placeholder="Postal Code"
              value={billing.postalCode}
              onChange={handleBillingChange}
              className="input input-bordered w-full"
              required
            />
            <input
              type="text"
              name="city"
              placeholder="City"
              value={billing.city}
              onChange={handleBillingChange}
              className="input input-bordered w-full"
              required
            />
            <input
              type="text"
              name="street"
              placeholder="Street (with house number)"
              value={billing.street}
              onChange={handleBillingChange}
              className="input input-bordered w-full"
              required
            />
            <input
              type="text"
              name="doorFloor"
              placeholder="Door/Floor (optional)"
              value={billing.doorFloor}
              onChange={handleBillingChange}
              className="input input-bordered w-full"
            />
          </div>
        </section>

        {/* Shipping Details */}
        <section className="bg-white p-6 rounded-lg shadow-lg">
          <h2 className="text-2xl font-semibold mb-4">Shipping Information</h2>
          <div className="flex items-center mb-4">
            <input
              type="checkbox"
              id="same"
              checked={sameAsBilling}
              onChange={() => setSameAsBilling((prev) => !prev)}
              className="checkbox checkbox-primary mr-2"
            />
            <label htmlFor="same" className="font-medium">
              Shipping same as billing
            </label>
          </div>
          {!sameAsBilling && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <input
                type="text"
                name="name"
                placeholder="Receiver Name"
                value={shipping.name}
                onChange={handleShippingChange}
                className="input input-bordered w-full"
                required
              />
              <input
                type="tel"
                name="phone"
                placeholder="Receiver Phone"
                value={shipping.phone}
                onChange={handleShippingChange}
                className="input input-bordered w-full"
                required
              />
              <input
                type="text"
                name="postalCode"
                placeholder="Postal Code"
                value={shipping.postalCode}
                onChange={handleShippingChange}
                className="input input-bordered w-full"
                required
              />
              <input
                type="text"
                name="city"
                placeholder="City"
                value={shipping.city}
                onChange={handleShippingChange}
                className="input input-bordered w-full"
                required
              />
              <input
                type="text"
                name="street"
                placeholder="Street (with house number)"
                value={shipping.street}
                onChange={handleShippingChange}
                className="input input-bordered w-full"
                required
              />
              <input
                type="text"
                name="doorFloor"
                placeholder="Door/Floor (optional)"
                value={shipping.doorFloor}
                onChange={handleShippingChange}
                className="input input-bordered w-full"
              />
            </div>
          )}
        </section>

        {/* Payment Method */}
        <div className="bg-white p-6 rounded-lg shadow-lg">
          <h2 className="text-2xl font-semibold mb-4">Payment Method</h2>
          <div className="flex flex-col md:flex-row gap-4">
            <label className="flex items-center">
              <input
                type="radio"
                name="payment"
                value="cod"
                checked={paymentMethod === "cod"}
                onChange={(e) => setPaymentMethod(e.target.value)}
                className="radio radio-primary mr-2"
              />
              Cash on Delivery
            </label>
            <label className="flex items-center">
              <input
                type="radio"
                name="payment"
                value="card"
                checked={paymentMethod === "card"}
                onChange={(e) => setPaymentMethod(e.target.value)}
                className="radio radio-primary mr-2"
              />
              Credit Card
            </label>
            <label className="flex items-center">
              <input
                type="radio"
                name="payment"
                value="szep"
                checked={paymentMethod === "szep"}
                onChange={(e) => setPaymentMethod(e.target.value)}
                className="radio radio-primary mr-2"
              />
              SZÉP Card
            </label>
            <label className="flex items-center">
              <input
                type="radio"
                name="payment"
                value="paypal"
                checked={paymentMethod === "paypal"}
                onChange={(e) => setPaymentMethod(e.target.value)}
                className="radio radio-primary mr-2"
              />
              PayPal
            </label>
          </div>
        </div>

        {/* Order Notes */}
        <div className="bg-white p-6 rounded-lg shadow-lg">
          <h2 className="text-2xl font-semibold mb-4">Delivery Instructions</h2>
          <textarea
            placeholder="Any special instructions?"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            className="textarea textarea-bordered w-full"
            rows={4}
          />
        </div>

        {/* Submit Button */}
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
