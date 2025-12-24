# Project Structure & Organization

## Repository Layout
```
SmartCON/
├── docs/                    # Documentation (PRD, guides, architecture)
├── backend/                 # Spring Boot backend application
├── prototype/              # React frontend prototype/development
├── smartcon_saas_web/      # Production web application
├── prototype_vue_backup/   # Legacy Vue.js prototype (archived)
└── .kiro/                  # Kiro AI assistant configuration
```

## Backend Structure (Spring Boot)
```
backend/src/main/java/com/smartcon/
├── domain/                 # Business domains
│   ├── admin/             # Super admin functionality
│   ├── attendance/        # Attendance tracking
│   ├── tenant/            # Multi-tenant management
│   └── user/              # User management
├── global/                # Cross-cutting concerns
│   ├── common/            # Common utilities (ApiResponse)
│   └── tenant/            # Tenant context management
└── infra/                 # External integrations (planned)
    ├── face/              # FaceNet API client
    ├── weather/           # Weather API integration
    └── notification/      # SMS/Push notifications
```

## Frontend Structure (React)
```
prototype/src/
├── components/
│   ├── ui/                # Shadcn/UI base components
│   └── layout/            # Layout components
├── pages/                 # Route components organized by role
│   ├── auth/              # Authentication pages
│   ├── hq/                # HQ admin pages
│   ├── site/              # Site manager pages
│   ├── worker/            # Worker pages
│   └── super/             # Super admin pages
├── contexts/              # React contexts (Theme, etc.)
├── hooks/                 # Custom React hooks
├── stores/                # Zustand state stores
├── mock-data/             # Development mock data
└── lib/                   # Utilities and configurations
```

## Naming Conventions

### Files & Directories
- **React Components**: PascalCase (e.g., `DashboardHQ.tsx`)
- **Hooks**: camelCase with `use` prefix (e.g., `useMediaQuery.ts`)
- **Utilities**: camelCase (e.g., `utils.ts`)
- **Java Classes**: PascalCase (e.g., `User.java`, `TenantContext.java`)
- **Java Packages**: lowercase with dots (e.g., `com.smartcon.domain.user`)

### Routes & URLs
- **Role-based routing**: `/{role}/{feature}` (e.g., `/hq/dashboard`, `/site/attendance`)
- **Auth routes**: `/login`, `/login/hq`, `/login/social`
- **API endpoints**: `/api/v1/{domain}/{resource}` (planned)

## Code Organization Principles

### Domain-Driven Design
- Backend organized by business domains (user, tenant, attendance)
- Each domain contains entities, repositories, services, controllers
- Cross-cutting concerns in `global` package

### Role-Based Frontend
- Pages organized by user roles (hq, site, worker, super)
- Shared components in `components/ui`
- Role-specific logic isolated in respective directories

### Multi-tenant Architecture
- All entities extend `BaseTenantEntity` for automatic tenant isolation
- `TenantContext` manages current tenant via ThreadLocal
- Database queries automatically filtered by `tenant_id`

## Configuration Files
- **Frontend**: `vite.config.ts`, `tailwind.config.js`, `tsconfig.json`
- **Backend**: `pom.xml`, `application.yml` (planned)
- **Development**: `.gitignore`, `package.json`, Docker configurations

## Documentation Structure
- **docs/**: All project documentation
- **docs/backup/**: Version history of documents
- **docs/prototype-UI-sample/**: UI mockup images
- Key documents: PRD.md, System-Architecture.md, Development-Process.md