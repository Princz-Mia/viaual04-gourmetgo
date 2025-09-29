const TermsOfService = () => {
    return (
        <div className="container mx-auto px-4 py-10">
            <div className="bg-base-100 shadow-xl rounded-xl p-6 max-w-4xl mx-auto">
                <h1 className="text-3xl font-bold mb-4">Terms of Service</h1>
                <p className="mb-4">
                    By using our services, you agree to be bound by the following terms and conditions.
                </p>
                <h2 className="text-xl font-semibold mt-6 mb-2">Usage</h2>
                <p className="mb-4">
                    Our platform is intended for lawful and authorized use only. Misuse may result in suspension or termination.
                </p>
                <h2 className="text-xl font-semibold mt-6 mb-2">Account Responsibility</h2>
                <p className="mb-4">
                    You are responsible for maintaining the confidentiality of your account and password.
                </p>
                <h2 className="text-xl font-semibold mt-6 mb-2">Modifications</h2>
                <p className="mb-4">
                    We reserve the right to change these terms at any time. Continued use constitutes acceptance of those changes.
                </p>
                <p className="mt-8 text-sm text-gray-500">
                    Last updated: May 2025
                </p>
            </div>
        </div>
    );
};

export default TermsOfService;
