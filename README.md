# ParkEase — Mall Parking Management System

A **Spring Boot + MongoDB** backend for managing mall parking slots and bookings. Serves a vanilla JS frontend as static files.

---

## 🚀 Quick Start (Local)

### Prerequisites
| Tool | Version |
|---|---|
| Java | 25+ |
| MongoDB | 7.0+ |
| Maven | via the `backend/mvnw` wrapper (no install needed) |

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
cd backend

# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

The app starts at **http://localhost:8080**

---

## 🚀 Deployment

The frontend can be deployed to Vercel as a static site. Render does not provide a native Java runtime for this Spring Boot backend; deploying it to Render requires Docker. Since this project is kept Docker-free, deploy the backend to a Java-capable hosting provider instead.

---

## ⚙️ Environment Variables

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8080` | HTTP server port |
| `SPRING_MONGODB_URI` | `mongodb://127.0.0.1:27018/parking_system` | Full MongoDB connection URI; set to your Atlas URI on Render |
| `MONGODB_DATABASE` | `parking_system` | Database name |
| `PARKEASE_ADMIN_EMAIL` | `admin@parkease.com` | Admin account email |
| `PARKEASE_ADMIN_PASSWORD` | *(empty — skips bootstrap)* | Admin password (≥ 12 chars) |
| `PARKEASE_ADMIN_NAME` | `ParkEase Admin` | Admin display name |
| `LOG_APP_LEVEL` | `INFO` | App-specific log level |
| `LOG_ROOT_LEVEL` | `INFO` | Root log level |

> **Security**: Keep database credentials and admin passwords in your hosting provider's environment-variable settings. Never commit secrets.

For MongoDB Atlas, set `SPRING_MONGODB_URI` in your backend hosting provider to the connection string Atlas provides, for example `mongodb+srv://<user>:<password>@<cluster-host>/parking_system?retryWrites=true&w=majority`. URL-encode special characters in the password. Do not put database credentials in this file.

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
backend/
├── src/
│   ├── main/java/com/parking/parkingsystem/
│   │   ├── config/        # Security & admin initializer
│   │   ├── controller/    # REST controllers + health check
│   │   ├── model/         # MongoDB documents
│   │   ├── repository/    # Spring Data repositories
│   │   └── service/       # Business logic
│   ├── main/resources/
│   │   └── application.properties
│   └── test/              # Backend tests
├── .mvn/                  # Maven wrapper configuration
├── mvnw / mvnw.cmd        # Maven wrapper scripts
└── pom.xml
frontend/
├── index.html
├── app.js
└── style.css
```

The Maven build packages `frontend/` into the backend JAR as static resources, so the app continues to serve the UI at its existing URL.
The repository root contains only these two application folders; `.github/` remains at the root because GitHub Actions requires that location.

---

## 🔐 Security Notes

- Passwords are hashed with **BCrypt**.
- Session cookies are `HttpOnly` and `SameSite=Lax`.
- The admin password env var accepts an empty value — if empty, admin bootstrap is skipped silently.
- Review and tighten CORS and CSRF settings before going public.

---

## ☁️ Cloud Database Setup (MongoDB Atlas)

1. Create a free [MongoDB Atlas](https://www.mongodb.com/cloud/atlas) cluster.
2. Whitelist your server IP and create a DB user.
3. Copy the Atlas connection string into `SPRING_MONGODB_URI`.
4. Set `SPRING_MONGODB_URI` and the admin environment variables in the backend hosting provider.

---

## 🧪 Running Tests

```bash
cd backend

./mvnw.cmd test    # Windows
./mvnw test        # Linux / macOS
```
