# GourmetGo Backend

## Setup Instructions

### 1. Clone the repository
```bash
git clone <repository-url>
cd viaual04_gourmetgo_backend
```

### 2. Configure environment variables
```bash
# Copy the example environment file
cp .env.example .env

# Edit .env with your actual values
```

### 3. Copy application properties
```bash
# Copy the template file
cp src/main/resources/application.properties.template src/main/resources/application.properties
```

### 4. Run the application
```bash
./mvnw spring-boot:run
```

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | Database connection URL | `jdbc:postgresql://localhost:5432/gourmetgo_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `your-password` |
| `JWT_SECRET` | JWT signing secret | `your-secret-key` |
| `EMAIL_USERNAME` | Email service username | `your-email@gmail.com` |
| `EMAIL_PASSWORD` | Email service password | `your-app-password` |