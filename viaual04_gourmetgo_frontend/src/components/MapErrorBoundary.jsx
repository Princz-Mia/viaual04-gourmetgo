import React from 'react';

class MapErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Map error:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="w-full h-80 sm:h-96 bg-gradient-to-br from-gray-100 to-gray-200 rounded-3xl flex items-center justify-center">
          <div className="text-center p-8">
            <div className="text-4xl mb-4">🗺️</div>
            <h3 className="text-lg font-semibold text-gray-700 mb-2">Map Unavailable</h3>
            <p className="text-sm text-gray-500">Showing restaurants in Budapest area</p>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default MapErrorBoundary;