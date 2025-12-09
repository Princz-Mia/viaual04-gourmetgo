import React, { useEffect, useState, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet';
import { useNavigate } from 'react-router-dom';
import L from 'leaflet';
import { fetchRestaurants } from '../api/restaurantService';

// Custom user location icon with modern design
const userIcon = new L.DivIcon({
  className: 'user-location-marker',
  html: `
    <div class="relative">
      <div class="w-6 h-6 bg-blue-500 border-4 border-white rounded-full shadow-xl"></div>
      <div class="absolute inset-0 w-6 h-6 bg-blue-400 rounded-full animate-ping opacity-75"></div>
    </div>
  `,
  iconSize: [24, 24],
  iconAnchor: [12, 12]
});

// Modern restaurant marker with hover tooltip
const createRestaurantIcon = (restaurant) => new L.DivIcon({
  className: 'restaurant-marker',
  html: `
    <div class="relative group">
      <div class="w-10 h-10 bg-gradient-to-br from-red-500 to-red-600 border-3 border-white rounded-2xl shadow-xl flex items-center justify-center transform hover:scale-110 transition-transform cursor-pointer">
        <span class="text-white text-lg">🍽️</span>
      </div>
      <div class="absolute bottom-12 left-1/2 transform -translate-x-1/2 bg-black/90 text-white px-3 py-2 rounded-lg text-sm whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-[1000]">
        <div class="font-semibold">${restaurant.name}</div>
        <div class="text-xs">⭐ ${restaurant.rating?.toFixed(1) || 'New'} • ${restaurant.deliveryFee > 0 ? '$' + restaurant.deliveryFee : 'Free delivery'}</div>
        <div class="absolute top-full left-1/2 transform -translate-x-1/2 border-4 border-transparent border-t-black/90"></div>
      </div>
    </div>
  `,
  iconSize: [40, 40],
  iconAnchor: [20, 20],
  popupAnchor: [0, -20]
});

const MapEvents = ({ onMapMove }) => {
  useMapEvents({
    moveend: (e) => {
      const center = e.target.getCenter();
      onMapMove(center.lat, center.lng);
    }
  });
  return null;
};

const LocationMap = ({ center, userLocation, onMapMove }) => {
  const [restaurants, setRestaurants] = useState([]);
  const navigate = useNavigate();
  const mapRef = useRef();

  useEffect(() => {
    loadRestaurants();
  }, []);

  useEffect(() => {
    if (mapRef.current && userLocation.lat && userLocation.lng) {
      mapRef.current.setView([userLocation.lat, userLocation.lng], 16);
    }
  }, [userLocation.lat, userLocation.lng]);

  const loadRestaurants = async () => {
    try {
      const data = await fetchRestaurants();
      const restaurantsWithCoords = data
        .filter(restaurant => restaurant.latitude && restaurant.longitude)
        .map(restaurant => ({
          ...restaurant,
          lat: restaurant.latitude,
          lng: restaurant.longitude
        }));
      setRestaurants(restaurantsWithCoords);
    } catch (error) {
      console.error('Failed to load restaurants:', error);
    }
  };

  const handleRestaurantClick = (restaurantId) => {
    navigate(`/restaurant/${restaurantId}`);
  };

  const handleMyLocationClick = () => {
    if (mapRef.current) {
      mapRef.current.setView([userLocation.lat, userLocation.lng], 15);
    }
  };

  return (
    <div className="relative">
      {/* Map Container with Modern Styling */}
      <div className="relative rounded-3xl overflow-hidden shadow-2xl border border-gray-200 bg-gradient-to-br from-blue-50 to-indigo-100">
        <MapContainer
          center={[userLocation.lat, userLocation.lng]}
          zoom={16}
          className="w-full h-80 sm:h-96"
          zoomControl={false}
          attributionControl={false}
          ref={mapRef}
          key={`${userLocation.lat}-${userLocation.lng}`}
        >
          <TileLayer
            url="https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png"
          />
          <MapEvents onMapMove={onMapMove} />
          
          {/* User location marker */}
          <Marker position={[userLocation.lat, userLocation.lng]} icon={userIcon}>
            <Popup className="modern-popup">
              <div class="bg-white rounded-xl p-4 shadow-lg border-0">
                <div class="flex items-center gap-3">
                  <div class="w-3 h-3 bg-blue-500 rounded-full"></div>
                  <div>
                    <div class="font-semibold text-gray-800">Your Location</div>
                    <div class="text-sm text-gray-500">Current position</div>
                  </div>
                </div>
              </div>
            </Popup>
          </Marker>

          {/* Restaurant markers */}
          {restaurants.map((restaurant) => (
            <Marker
              key={restaurant.id}
              position={[restaurant.lat, restaurant.lng]}
              icon={createRestaurantIcon(restaurant)}
              eventHandlers={{
                click: () => handleRestaurantClick(restaurant.id)
              }}
            >
              <Popup className="modern-popup">
                <div class="bg-white rounded-xl overflow-hidden shadow-lg border-0 min-w-[280px]">
                  <div class="bg-gradient-to-r from-red-500 to-red-600 p-4 text-white">
                    <h3 class="font-bold text-lg mb-1">{restaurant.name}</h3>
                    <div class="flex items-center gap-2">
                      <span class="bg-white/20 px-2 py-1 rounded-full text-xs">
                        ⭐ {restaurant.rating?.toFixed(1) || 'New'}
                      </span>
                      <span class="text-xs opacity-90">
                        {restaurant.categories?.[0]?.name || 'Restaurant'}
                      </span>
                    </div>
                  </div>
                  <div class="p-4">
                    <div class="flex items-center justify-between mb-3">
                      <span class="text-sm text-gray-600">Delivery Fee</span>
                      <span class="font-semibold text-green-600">
                        {restaurant.deliveryFee > 0 ? `$${restaurant.deliveryFee}` : 'Free'}
                      </span>
                    </div>
                    <button
                      onClick={() => handleRestaurantClick(restaurant.id)}
                      class="w-full bg-gradient-to-r from-red-500 to-red-600 text-white py-3 rounded-xl font-semibold hover:from-red-600 hover:to-red-700 transition-all transform hover:scale-105"
                    >
                      View Menu →
                    </button>
                  </div>
                </div>
              </Popup>
            </Marker>
          ))}
        </MapContainer>
        
        {/* Modern Control Panel */}
        <div className="absolute top-4 right-4 z-[1000]">
          <div className="bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl border border-gray-200 p-2">
            <button 
              className="block w-10 h-10 rounded-xl bg-white hover:bg-gray-50 border border-gray-200 shadow-sm mb-1 flex items-center justify-center font-bold text-gray-700 hover:text-gray-900 transition-all"
              onClick={() => mapRef.current?.zoomIn()}
            >
              +
            </button>
            <button 
              className="block w-10 h-10 rounded-xl bg-white hover:bg-gray-50 border border-gray-200 shadow-sm flex items-center justify-center font-bold text-gray-700 hover:text-gray-900 transition-all"
              onClick={() => mapRef.current?.zoomOut()}
            >
              −
            </button>
          </div>
        </div>

        {/* My Location Button */}
        <div className="absolute bottom-4 right-4 z-[1000]">
          <button 
            onClick={handleMyLocationClick}
            className="bg-white/90 backdrop-blur-sm hover:bg-white border border-gray-200 shadow-xl rounded-2xl p-3 transition-all hover:scale-105"
          >
            <svg className="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
            </svg>
          </button>
        </div>

        {/* Restaurant Count Badge */}
        <div className="absolute top-4 left-4 z-[1000]">
          <div className="bg-white/90 backdrop-blur-sm rounded-full px-4 py-2 shadow-xl border border-gray-200">
            <span className="text-sm font-semibold text-gray-700">
              {restaurants.filter(restaurant => {
                const distance = Math.sqrt(
                  Math.pow(restaurant.lat - userLocation.lat, 2) + 
                  Math.pow(restaurant.lng - userLocation.lng, 2)
                );
                return distance <= 0.05;
              }).length} restaurants nearby
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LocationMap;