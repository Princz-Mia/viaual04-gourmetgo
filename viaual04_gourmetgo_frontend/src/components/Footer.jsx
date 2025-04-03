import { Link } from 'react-router-dom';

const Footer = () => {
  return (
    <footer className="bg-gray-800 text-gray-200 py-6">
      <div className="min-w-screen mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row justify-between items-center">
        <p className="text-sm">
          © {new Date().getFullYear()} GourmetGo. All rights reserved.
        </p>
        <div className="flex space-x-4 mt-4 sm:mt-0">
          <Link to="/privacy" className="text-sm hover:underline">
            Privacy Policy
          </Link>
          <Link to="/terms" className="text-sm hover:underline">
            Terms of Service
          </Link>
          <Link to="/contact" className="text-sm hover:underline">
            Contact Us
          </Link>
        </div>
      </div>
    </footer>
  );
};

export default Footer;