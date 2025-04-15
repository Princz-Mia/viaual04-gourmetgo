import { Link } from "react-router-dom";
import joinUsBg from "../assets/images/join_us_bg.jpg"

const JoinUsSection = () => {
  return (
    <section className="relative mt-12 mb-12 sm:mt-24 sm:mb-24 md:mt-36 md:mb-36">
      <div
        className="w-full h-64 md:h-80 bg-cover bg-center relative"
      >
        <div className="w-full h-full flex items-center justify-center relative"
          style={{
            backgroundImage: `url(${joinUsBg})`,
            backgroundSize: 'cover',
            backgroundPosition: 'center'
          }}>
          <div className="bg-white rounded-lg shadow-2xl p-6 sm:p-8 w-72 h-72 sm:w-80 sm:h-80 md:w-96 md:h-96
                          lg:absolute lg:top-1/2 lg:right-[10%] lg:transform lg:-translate-y-1/2">
            <div className="flex flex-col h-full justify-center">
              <h2 className="text-2xl sm:text-3xl md:text-4xl font-bold text-center text-gray-800 mb-4">
                Join GourmetGo Today!
              </h2>
              <p className="text-xs sm:text-sm md:text-base text-center text-gray-600 mb-6 flex-grow">
                Expand your restaurant’s reach and boost your growth by partnering with GourmetGo.
                Benefit from innovative tools, extensive marketing, and dedicated support designed
                specifically for restaurants and startups. Elevate your business and join our thriving
                community!
              </p>
              <div className="flex justify-center">
                <Link to="/restaurant-registration">
                  <button className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded-full transition duration-300">
                    Register Your Restaurant
                  </button>
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default JoinUsSection;
