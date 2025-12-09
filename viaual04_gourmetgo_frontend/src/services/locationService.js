export const getCurrentLocation = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('Geolocation not supported'));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          lat: position.coords.latitude,
          lng: position.coords.longitude
        });
      },
      (error) => {
        console.warn('Geolocation error:', error.message);
        // Fallback to Budapest coordinates
        resolve({ lat: 47.4979, lng: 19.0402 });
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0 // Always get fresh location
      }
    );
  });
};

export const getAddressFromCoords = async (lat, lng) => {
  try {
    const response = await fetch(
      `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&addressdetails=1`
    );
    const data = await response.json();
    
    if (data.address) {
      const { city, town, village, suburb, neighbourhood, road, house_number } = data.address;
      const street = road ? (house_number ? `${road} ${house_number}` : road) : '';
      const location = city || town || village || 'Budapest';
      
      if (street && location) {
        return `${street}, ${location}`;
      } else if (street) {
        return street;
      } else {
        return location;
      }
    }
    
    return 'Budapest, Hungary';
  } catch (error) {
    return 'Budapest, Hungary';
  }
};