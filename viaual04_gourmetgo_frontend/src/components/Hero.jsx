import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import heroImage from '../assets/images/hero_img.jpg';

const Hero = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const navigate = useNavigate();

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim() !== '') {
      navigate(`/restaurants/search?name=${encodeURIComponent(searchQuery)}`);
    }
  };

  return (
    <div
      className="hero h-auto"
      style={{
        backgroundImage: `url(${heroImage})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center'
      }}
    >
      {/* Optional overlay for better text readability */}
      <div className="hero-overlay bg-opacity-40 "></div>
      <div className="hero-content text-left text-neutral-content p-8 sm:p-16 lg:p-32 mb-48">
        <div className="max-w-4xl flex flex-col">
          {/* Marketing text container shifted to the right responsively */}
          <div className="mb-8 ml-0 sm:ml-8 md:ml-16 lg:ml-128 xl:ml-128 bg-black/50 text-red-50 p-4 rounded-2xl">
            <h1 className="mb-4 text-3xl font-bold">Discover GourmetGo</h1>
            <p className="text-lg">
              Find the best restaurants near you. Explore menus, reviews, and special offers that make dining an experience.
            </p>
          </div>
          {/* Search form container shifted to the left responsively */}
          <div className="mr-0 sm:mr-8 md:mr-16 lg:mr-64 xl:mr-96">
            <form onSubmit={handleSearch}>
              <div className="input-group">
                <label className="input input-bordered text-black focus:outline-none focus:ring-0" style={{ outline: 'none', boxShadow: 'none' }}>
                  <svg className="h-[1em] opacity-50" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                    <g strokeLinejoin="round" strokeLinecap="round" strokeWidth="2.5" fill="none" stroke="currentColor">
                      <circle cx="11" cy="11" r="8"></circle>
                      <path d="m21 21-4.3-4.3"></path>
                    </g>
                  </svg>
                  <input
                    type="text"
                    placeholder="Search restaurants..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                  />
                </label>
                <button type="submit" className="btn btn-primary ml-0.5">
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