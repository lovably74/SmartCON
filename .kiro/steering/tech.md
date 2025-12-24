# Technology Stack & Build System

## Frontend Stack
- **Framework**: React 18 + TypeScript + Vite
- **UI System**: Shadcn/UI + Tailwind CSS (Custom Design System)
- **State Management**: Zustand (client state) + TanStack Query (server state)
- **Routing**: Wouter (lightweight React router)
- **Mobile Bridge**: Capacitor 6.x (Camera, Geolocation, Push notifications)
- **Testing**: Vitest + React Testing Library

## Backend Stack
- **Language**: Java 17 (LTS)
- **Framework**: Spring Boot 3.3.x
- **Database**: H2 In-Memory Database (개발용), MariaDB 10.11 (프로덕션용) with JPA/Hibernate
- **Security**: Spring Security 6.x + JWT + OAuth2 Client
- **Batch Processing**: Spring Batch (for FaceNet sync, billing)
- **Build Tool**: Maven 3.8+

## External Integrations
- **Face Recognition**: FaceNet (Face Embedding & Matching Server)
- **Weather API**: OpenWeatherMap / KMA API
- **Payment**: PG integration for subscription billing
- **Messaging**: SMS/AlimTalk Gateway
- **Storage**: AWS S3 for files and images

## Development Tools
- **IDE**: VS Code (frontend) / IntelliJ IDEA (backend)
- **AI Tools**: Cursor, GitHub Copilot for code generation
- **Database Console**: H2 Console (개발용)
- **CI/CD**: GitHub Actions (recommended)

## Common Commands

### Frontend (Prototype)
```bash
cd prototype
npm install          # Install dependencies
npm run dev         # Start development server
npm run build       # Build for production
npm run test        # Run tests
npm run test:run    # Run tests once (no watch)
```

### Backend
```bash
cd backend
mvn clean install   # Build and install dependencies
mvn spring-boot:run  # Start development server
mvn test            # Run tests
mvn clean package   # Build JAR for production
```

### Full Stack Development
```bash
# Start both frontend and backend
cd prototype && npm run dev &
cd backend && mvn spring-boot:run
```

## Architecture Patterns
- **Multi-tenant SaaS**: Shared database with tenant_id discriminator
- **Domain-Driven Design**: Organized by business domains
- **RESTful APIs**: JSON over HTTPS with JWT authentication
- **Mobile-First**: Responsive design with Capacitor for native features
- **Batch Processing**: Spring Batch for daily face sync and billing