import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import heroImage from '../assets/images/hero_img.jpg';
import LocationMap from './LocationMap';
import MapErrorBoundary from './MapErrorBoundary';
import { getCurrentLocation, getAddressFromCoords } from '../services/locationService';

const Hero = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [userLocation, setUserLocation] = useState(null);
  const [mapCenter, setMapCenter] = useState();
  const [locationName, setLocationName] = useState('Getting location...');
  const navigate = useNavigate();

  useEffect(() => {
    loadUserLocation();
  }, []); // Runs on every component mount



  const loadUserLocation = async () => {
    try {
      const location = await getCurrentLocation();
      console.log('Setting user location:', location);
      setUserLocation(location);
      setMapCenter(location);
      const address = await getAddressFromCoords(location.lat, location.lng);
      console.log('Full address:', address);
      console.log('Address parts:', address.split(','));
      setLocationName(address || 'Your Location');
    } catch (error) {
      console.log('Location error, using fallback');
      const fallback = { lat: 47.4979, lng: 19.0402 };
      setUserLocation(fallback);
      setMapCenter(fallback);
      setLocationName('Budapest, Hungary');
    }
  };

  const handleMapMove = (lat, lng) => {
    setMapCenter({ lat, lng });
    // Don't update address when map moves
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim() !== '') {
      navigate(`/restaurants/search?name=${encodeURIComponent(searchQuery)}`);
    }
  };

  return (
    <div
      className="hero min-h-[60vh] sm:min-h-[70vh] lg:min-h-[80vh]"
      style={{
        backgroundImage: `url(${heroImage})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center'
      }}
    >
      <div className="hero-overlay bg-opacity-40"></div>
      <div className="hero-content text-center text-neutral-content px-4 py-8 sm:py-12 md:py-16">
        <div className="max-w-4xl mx-auto w-full">
          {/* Main Content Card - Mobile First */}
          <div className="mb-6 sm:mb-8 bg-neutral/80 backdrop-blur-sm text-white p-4 sm:p-6 md:p-8 rounded-xl sm:rounded-2xl">
            <h1 className="font-display text-2xl sm:text-3xl md:text-4xl lg:text-5xl font-semibold mb-3 sm:mb-4 text-white leading-tight">
              Craving something delicious?
            </h1>
            <p className="text-sm sm:text-base md:text-lg text-white/90 mb-4 sm:mb-6 max-w-2xl mx-auto">
              Discover amazing restaurants near you and get your favorite meals delivered fast
            </p>
            <div className="inline-flex items-center gap-2 bg-white/90 backdrop-blur-sm px-4 py-2 rounded-full shadow-lg mb-4 sm:mb-6">
              <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
              <span className="text-sm font-medium text-gray-700">{locationName}</span>
            </div>
            
            <div className="mb-4 sm:mb-6">
              {userLocation ? (
                <MapErrorBoundary>
                  <LocationMap 
                    center={mapCenter}
                    userLocation={userLocation}
                    onMapMove={handleMapMove}
                  />
                </MapErrorBoundary>
              ) : (
                <div className="w-full h-80 sm:h-96 bg-gradient-to-br from-gray-100 to-gray-200 rounded-3xl flex items-center justify-center">
                  <div className="text-center">
                    <div className="animate-spin w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full mx-auto mb-4"></div>
                    <p className="text-gray-600">Loading map...</p>
                  </div>
                </div>
              )}
            </div>
          </div>
          
          {/* Search Form - Mobile Optimized */}
          <div className="max-w-sm sm:max-w-md lg:max-w-lg mx-auto">
            <form onSubmit={handleSearch}>
              <div className="flex flex-col sm:flex-row items-center gap-2 sm:gap-2 bg-base-100 shadow-lg rounded-2xl p-3 sm:px-4 sm:py-3">
                <div className="flex items-center gap-2 w-full sm:flex-1">
                  <svg className="w-4 h-4 sm:w-5 sm:h-5 text-neutral/60 flex-shrink-0" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <circle cx="11" cy="11" r="8"></circle>
                    <path d="m21 21-4.3-4.3"></path>
                  </svg>
                  <input
                    type="text"
                    placeholder="Search dishes, restaurants..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="flex-1 bg-transparent text-neutral placeholder-neutral/60 focus:outline-none text-sm sm:text-base py-1 sm:py-0"
                  />
                </div>
                <button type="submit" className="btn btn-primary btn-sm sm:btn-md rounded-full w-full sm:w-auto text-xs sm:text-sm">
                  Search
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Hero;