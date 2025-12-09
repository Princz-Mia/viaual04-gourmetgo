import React from 'react';

const LoadingSpinner = ({ text = "Loading..." }) => {
    return (
        <div className="container mx-auto px-4 py-8">
            <div className="text-center">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mx-auto"></div>
                <p className="mt-4 text-gray-600">{text}</p>
            </div>
        </div>
    );
};

export default LoadingSpinner;