# 🍽️ GourmetGo - Food Ordering Platform

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19.0.0-blue.svg)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue.svg)](https://www.postgresql.org/)

A comprehensive full-stack food ordering web application developed as a BSc Computer Engineering thesis project at Budapest University of Technology and Economics (BME). GourmetGo provides a complete ecosystem for customers, restaurants, and administrators with advanced features including real-time chat, analytics dashboards, reward systems, and promotional campaigns.

## 🎯 Project Overview

GourmetGo is an enterprise-grade food delivery platform that demonstrates modern software engineering practices and full-stack development capabilities. The application serves three distinct user roles with specialized interfaces and functionality.

### 🏆 Key Achievements
- **Academic Excellence**: Developed as a university thesis project showcasing advanced software engineering concepts
- **Production-Ready**: Implements industry-standard security, monitoring, and scalability patterns
- **Comprehensive Feature Set**: 50+ distinct features across customer, restaurant, and admin domains
- **Modern Architecture**: Monolithic-first design with microservices-ready structure for future scalability

## ✨ Features

### 👥 Customer Experience
- **Account Management**: Registration, verification, profile management with secure authentication
- **Restaurant Discovery**: Search and filtering capabilities
- **Menu Browsing**: Interactive product catalogs with detailed descriptions and images
- **Shopping Cart**: Persistent cart with real-time updates and item customization
- **Order Management**: Complete order lifecycle from placement to delivery tracking
- **Reward System**: Points earning, category bonuses, and redemption capabilities
- **Real-time Chat**: Customer support integration with live messaging
- **Reviews & Ratings**: Restaurant and product feedback system

### 🏪 Restaurant Management
- **Business Dashboard**: Comprehensive analytics with revenue, orders, and customer insights
- **Menu Management**: Product catalog administration with category organization
- **Order Processing**: Real-time order management with status updates
- **Customer Analytics**: Detailed customer behavior and preference analysis
- **Promotional Tools**: Happy hour campaigns and discount management
- **Performance Metrics**: Business intelligence with actionable insights
- **Report Generation**: Automated PDF reports for business analysis

### 🔧 Administrative Control
- **System Monitoring**: Real-time health checks and performance metrics
- **User Management**: Customer and restaurant account administration
- **Analytics Dashboard**: Platform-wide statistics and business intelligence
- **Promotion Management**: System-wide promotional campaigns and rewards
- **Content Moderation**: Review and content management capabilities
- **Financial Oversight**: Transaction monitoring and revenue analytics

## 🏗️ Technical Architecture

### Backend (Spring Boot)
```
├── 🔐 Security Layer
│   ├── JWT Authentication with refresh tokens
│   ├── Role-based access control (RBAC)
│   ├── Session management with Redis
│   └── CORS and security headers
├── 🌐 Web Layer
│   ├── RESTful API controllers
│   ├── WebSocket endpoints for real-time features
│   ├── Global exception handling
│   └── Request/response DTOs
├── 💼 Business Layer
│   ├── Service interfaces and implementations
│   ├── Event-driven architecture
│   ├── Business rule validation
│   └── Transaction management
├── 💾 Data Layer
│   ├── JPA entities with Hibernate
│   ├── Repository pattern implementation
│   └── Connection pooling with HikariCP
└── 🔧 Infrastructure
    ├── Structured logging with correlation IDs
    ├── Email service integration
    ├── PDF report generation
    └── Redis caching and session storage
```

### Frontend (React)
```
├── 🎨 UI Components
│   ├── Responsive design with Tailwind CSS
│   ├── DaisyUI component library
│   ├── Interactive charts and visualizations
│   └── Mobile-first responsive layouts
├── 🔄 State Management
│   ├── React Context for global state
│   ├── Custom hooks for business logic
│   ├── Local storage persistence
│   └── Real-time WebSocket integration
├── 🛣️ Routing & Navigation
│   ├── Protected routes with authentication
│   ├── Role-based route access
│   ├── Dynamic navigation components
│   └── Mobile navigation optimization
└── 🌐 API Integration
    ├── Axios HTTP client with interceptors
    ├── Automatic token refresh
    ├── Error handling and retry logic
    └── Request/response transformation
```

## 🛠️ Technology Stack

### Backend Technologies
- **Framework**: Spring Boot 3.4.2 with Java 17
- **Security**: Spring Security with JWT authentication
- **Database**: PostgreSQL with JPA/Hibernate ORM
- **Caching**: Redis for session management and caching
- **Communication**: WebSocket for real-time features

- **Build Tool**: Maven with multi-profile configuration

### Frontend Technologies
- **Framework**: React 19.0.0 with modern hooks
- **Styling**: Tailwind CSS 4.1.1 with DaisyUI components
- **Routing**: React Router DOM for SPA navigation
- **Forms**: React Hook Form with Yup validation
- **Charts**: Custom chart components for analytics
- **Maps**: Leaflet integration for geolocation
- **Build Tool**: Vite for fast development and building

### Infrastructure & DevOps
- **Containerization**: Docker Compose for development
- **Database**: PostgreSQL with connection pooling
- **Monitoring**: Structured logging with Logback
- **Email**: SMTP integration for notifications
- **File Storage**: Local file system with image management



## 📈 Performance & Scalability

### Backend Optimizations
- **Connection Pooling**: HikariCP with optimized pool settings
- **Caching Strategy**: Redis integration for session and data caching
- **Database Indexing**: Optimized queries with proper indexing
- **Async Processing**: Event-driven architecture for non-blocking operations

### Frontend Optimizations
- **Code Splitting**: Dynamic imports for route-based splitting
- **Asset Optimization**: Vite-powered build with tree shaking
- **Lazy Loading**: Component and image lazy loading
- **Responsive Design**: Mobile-first approach with optimized layouts

## 🔒 Security Features

- **Authentication**: JWT-based authentication with refresh token rotation
- **Authorization**: Role-based access control (Customer, Restaurant, Admin)
- **Data Protection**: Input validation and SQL injection prevention
- **Session Management**: Secure session handling with Redis
- **CORS Configuration**: Properly configured cross-origin resource sharing
- **Password Security**: BCrypt hashing with salt

## 📱 Mobile Responsiveness

GourmetGo is fully responsive and optimized for:
- **Desktop**: Full-featured dashboard and management interfaces
- **Tablet**: Optimized layouts for medium-screen devices
- **Mobile**: Touch-friendly interface with mobile navigation
- **PWA Ready**: Progressive Web App capabilities for mobile installation

## 🤝 Contributing

This project was developed as a university thesis. While not actively maintained for contributions, the codebase serves as a reference for:
- Full-stack application architecture
- Spring Boot best practices
- React development patterns
- Database design and optimization
- Security implementation

## 📄 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## 🎓 Academic Context

**Institution**: Budapest University of Technology and Economics (BME)  
**Program**: Computer Engineering BSc  

This project demonstrates:
- Advanced software engineering principles
- Full-stack development capabilities
- Database design and optimization
- Security best practices
- Modern web development technologies
- Project management and documentation

---

**Note**: This is an academic project developed for educational purposes. The application demonstrates modern software development practices and serves as a portfolio piece showcasing full-stack development capabilities.