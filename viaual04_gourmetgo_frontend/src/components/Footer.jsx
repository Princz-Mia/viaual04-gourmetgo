import { Link } from 'react-router-dom';

const Footer = () => {
  return (
    <footer className="bg-neutral text-neutral-content py-4 sm:py-6 pb-16 sm:pb-6">
      <div className="max-w-6xl mx-auto px-3 sm:px-4 md:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row justify-between items-center gap-3 sm:gap-4">
          <p className="text-xs sm:text-sm text-center sm:text-left">
            © {new Date().getFullYear()} GourmetGo. All rights reserved.
          </p>
          <div className="flex flex-wrap justify-center sm:justify-end gap-3 sm:gap-4">
            <Link 
              to="/privacy-policy" 
              className="text-xs sm:text-sm hover:text-primary transition-colors"
            >
              Privacy Policy
            </Link>
            <Link 
              to="/terms-of-service" 
              className="text-xs sm:text-sm hover:text-primary transition-colors"
            >
              Terms of Service
            </Link>
            <Link 
              to="/contact" 
              className="text-xs sm:text-sm hover:text-primary transition-colors"
            >
              Contact Us
            </Link>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;