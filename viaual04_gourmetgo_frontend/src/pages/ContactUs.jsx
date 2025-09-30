const ContactUs = () => {
    return (
        <div className="container mx-auto px-4 py-10">
            <div className="bg-base-100 shadow-xl rounded-xl p-6 max-w-3xl mx-auto">
                <h1 className="text-3xl font-bold mb-6">Contact Us</h1>
                <p className="mb-6">Have questions or feedback? We'd love to hear from you!</p>
                <form className="space-y-4">
                    <div>
                        <label className="label">
                            <span className="label-text">Your Name</span>
                        </label>
                        <input type="text" className="input input-bordered w-full" placeholder="John Doe" />
                    </div>
                    <div>
                        <label className="label">
                            <span className="label-text">Email Address</span>
                        </label>
                        <input type="email" className="input input-bordered w-full" placeholder="john@example.com" />
                    </div>
                    <div>
                        <label className="label">
                            <span className="label-text">Message</span>
                        </label>
                        <textarea className="textarea textarea-bordered w-full" rows={5} placeholder="Your message..." />
                    </div>
                    <button type="submit" className="btn btn-primary w-full">Send Message</button>
                </form>
            </div>
        </div>
    );
};

export default ContactUs;
