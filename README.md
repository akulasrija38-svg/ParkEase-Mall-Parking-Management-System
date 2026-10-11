# ParkEase — Mall Parking Management System

A **Spring Boot + MongoDB** backend for managing mall parking slots and bookings. Serves a vanilla JS frontend as static files.

---

## 🚀 Quick Start (Local)

### Prerequisites
| Tool | Version |
|---|---|
| Java | 25+ |
| MongoDB | 7.0+ |
| Maven | via `mvnw` wrapper (no install needed) |

### 1. Clone the repo
```bash
git clone https://github.com/akulasrija38-svg/ParkEase-Mall-Parking-Management-System.git
cd ParkEase-Mall-Parking-Management-System
```

### 2. Start MongoDB locally
```bash
mongod --port 27018
```

### 3. Run the application
```bash
# Windows
./mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

The app starts at **http://localhost:8080**

---

## 🐳 Docker (Recommended for Deployment)

### Run with Docker Compose (app + MongoDB in one command)

```bash
# 1. Create your environment file from the template
cp .env.example .env

# 2. Edit .env — set a secure admin password (min. 12 chars)
notepad .env   # or nano .env on Linux

# 3. Start everything
docker compose up -d

# 4. View logs
docker compose logs -f app
```

Access: **http://localhost:8080**

### Run with a pre-built image only (supply your own MongoDB)

```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_MONGODB_URI="mongodb+srv://<user>:<pass>@<cluster>.mongodb.net/parking_system" \
  -e PARKEASE_ADMIN_EMAIL="admin@yoursite.com" \
  -e PARKEASE_ADMIN_PASSWORD="SuperSecure123!" \
  your-dockerhub-username/parkease:latest
```

---

## ⚙️ Environment Variables

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8080` | HTTP server port |
| `SPRING_MONGODB_URI` | `mongodb://localhost:27018/parking_system` | Full MongoDB connection URI |
| `MONGODB_DATABASE` | `parking_system` | Database name |
| `PARKEASE_ADMIN_EMAIL` | `admin@parkease.com` | Admin account email |
| `PARKEASE_ADMIN_PASSWORD` | *(empty — skips bootstrap)* | Admin password (≥ 12 chars) |
| `PARKEASE_ADMIN_NAME` | `ParkEase Admin` | Admin display name |
| `LOG_APP_LEVEL` | `INFO` | App-specific log level |
| `LOG_ROOT_LEVEL` | `INFO` | Root log level |

> **Security**: Never commit a real `.env` file. Use the provided `.env.example` as a template.

---

## 🔌 API Reference

### Auth
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a new user |
| `POST` | `/api/auth/login` | Public | Log in (form-based) |
| `POST` | `/api/auth/logout` | Any | Log out |
| `GET` | `/api/auth/me` | User | Current user profile |

### Malls & Slots
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/malls` | Public | List all malls |
| `POST` | `/api/malls` | Admin | Create a mall |
| `GET` | `/api/malls/{id}/slots` | Public | List slots for a mall |
| `POST` | `/api/malls/{id}/slots` | Admin | Add slots to a mall |

### Bookings
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/bookings` | User | Create a booking |
| `GET` | `/api/bookings/my` | User | My active bookings |
| `DELETE` | `/api/bookings/{id}` | User | Cancel a booking |

### Admin
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/admin/users` | Admin | List all users |
| `GET` | `/api/admin/bookings` | Admin | List all bookings |

### Health
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/health` | Public | Liveness + DB connectivity check |

---

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/parking/parkingsystem/
│   │   ├── config/        # Security & admin initializer
│   │   ├── controller/    # REST controllers + health check
│   │   ├── model/         # MongoDB documents
│   │   ├── repository/    # Spring Data repositories
│   │   └── service/       # Business logic
│   └── resources/
│       ├── static/        # Frontend (HTML, CSS, JS)
│       └── application.properties
└── test/
```

---

## 🔐 Security Notes

- Passwords are hashed with **BCrypt**.
- Session cookies are `HttpOnly` and `SameSite=Lax`.
- The Docker image runs as a **non-root user** (`parkease`).
- The admin password env var accepts an empty value — if empty, admin bootstrap is skipped silently.
- Review and tighten CORS and CSRF settings before going public.

---

## ☁️ Cloud Deployment (MongoDB Atlas + Any PaaS)

1. Create a free [MongoDB Atlas](https://www.mongodb.com/cloud/atlas) cluster.
2. Whitelist your server IP and create a DB user.
3. Copy the Atlas connection string into `SPRING_MONGODB_URI`.
4. Deploy the Docker image to **Railway, Render, Fly.io, or any container service**.
5. Set all required environment variables in the PaaS dashboard.

---

## 🧪 Running Tests

```bash
./mvnw.cmd test    # Windows
./mvnw test        # Linux / macOS
```
