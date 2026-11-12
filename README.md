# India Post National Philately Deposit Account (PDA) Portal
### Smart India Hackathon (SIH) — Secure & Scalable Full-Stack Web Application

---

## 🏛️ Project Overview

The **India Post National Philately Deposit Account (PDA) Portal** is a production-ready, highly secure, and scalable Java Full-Stack web application designed for philatelists and the Department of Posts (Ministry of Communications, Government of India).

In traditional philately management, collectors faced difficulties depositing funds in advance, receiving commemorative releases promptly, tracking consignments, and obtaining special postmark cancellations. This solution provides a centralized, automated, and audited digital ecosystem:
- **Philatelic Deposit Account (PDA) Management**: Real-time balance ledger, online top-up (UPI / Net Banking / Cards), and standing instructions for automatic dispatch of new stamp releases.
- **Dynamic Philatelic Materials Catalog**: Browse and filter commemorative stamps, First Day Covers (FDCs), miniature sheets, postcards, sheetlets, and special covers across India's Postal Circles (Delhi, Maharashtra, Tamil Nadu, Karnataka, West Bengal, Kerala, Gujarat, Rajasthan).
- **Special Cancellations & Pictorial Postmarks**: Collectors can view limited-time commemorative postmarks and submit requests for official cancellations to be applied to their covers and consignments.
- **Speed Post Consignment Tracking**: Interactive real-time milestone stepper from order placement, postal circle dispatch, in-transit routing, to doorstep delivery.
- **Postal Officer / Administrator Portal**: Comprehensive back-office console for inventory control, order fulfillment, status updates, and cancellation request processing.

---

## 🔒 Security Architecture ("More Security")

The system incorporates defense-in-depth security best practices aligned with OWASP Top 10 guidelines:

1. **Stateless JWT Authentication (Spring Security 6)**:
   - JSON Web Tokens (HMAC-SHA256 with 256-bit cryptographic secret) for stateless, scalable session management.
   - Bearer token authentication filter (`JwtAuthenticationFilter`) validating claims, issuer, and expiration on every protected request.
2. **Role-Based Access Control (RBAC)**:
   - `ROLE_USER`: Access to personal PDA balance, placing orders, wishlist, order history, and cancellation requests.
   - `ROLE_ADMIN` & `ROLE_OFFICER`: Privileged access to `/api/admin/**` for catalog modifications, order dispatch management, and request approvals.
   - Public access to browsing catalog items, postal circles, and special cancellations.
3. **Password Security**:
   - Industry-standard **BCrypt hashing (strength 12)** ensures credentials are never stored in plaintext.
4. **Financial & PDA Transactional Integrity**:
   - Double-entry bookkeeping for every credit (top-up) and debit (order purchase).
   - Atomic `@Transactional` operations prevent race conditions and double-spending on deposit balances.
5. **Security Headers & Defense-in-Depth**:
   - `X-Frame-Options: SAMEORIGIN` (prevents Clickjacking)
   - `X-Content-Type-Options: nosniff` (prevents MIME-type confusion attacks)
   - Cross-Origin Resource Sharing (CORS) with whitelist policies
   - SQL Injection immunity via Spring Data JPA parameterized Hibernate queries.
   - Input sanitization & Bean Validation (`jakarta.validation`).

---

## ⚡ Scalability Architecture ("Scalable Web-Based Application")

1. **Layered Decoupled Architecture**:
   - **Presentation Layer**: Responsive web views connected to REST API endpoints via `api.js`.
   - **Controller Layer**: REST controllers producing sanitized `ApiResponse<T>` structures.
   - **Service Layer**: Business logic, wallet balance validation, and transaction isolation.
   - **Repository Layer**: Spring Data JPA with pagination (`Pageable`) and indexed queries.
   - **Database Layer**: Default zero-config persistent file database (`./data/philately_db`), seamlessly swappable to **PostgreSQL** or **MySQL** in high-throughput clusters using the provided `application-prod.properties`.
2. **Production Readiness**:
   - Embedded high-performance Apache Tomcat web server.
   - **Spring Boot Actuator** enabled at `/actuator/health` and `/actuator/metrics` for container health probes and Prometheus/Kubernetes monitoring.
   - HikariCP connection pool configured for high concurrency.

---

## 🚀 Quick Start Guide

### Prerequisites
- **Java 21 or Java 25** (Oracle JDK / OpenJDK)
- Windows / Linux / macOS

### 1. Run via Maven Wrapper (Development Mode)
```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

### 2. Run via Standalone Executable Fat JAR
```powershell
# Build fat jar
.\mvnw.cmd package -DskipTests=true

# Run standalone jar
java -jar target/stamp-sih-philately-1.0.0.jar
```

The portal will start on: **`http://localhost:8085`** (or configured port).

---

## 🔑 Default Test Credentials

| Role | Username | Password | Full Name | Starting PDA Balance |
| :--- | :--- | :--- | :--- | :--- |
| **Philatelist (Collector)** | `philatelist` | `Password123!` | Aarav Sharma | **₹ 2,450.00** |
| **Postal Officer (Admin)** | `admin` | `AdminPassword123!` | Senior Philatelic Officer | *Back-Office Privileges* |

*Note: The login page (`login.html`) features one-click "Quick Demo Sign-in" buttons for instant evaluation.*

---

## 🌐 Web Pages Overview

| Page | File | Purpose |
| :--- | :--- | :--- |
| **Home Portal** | `home.html` / `index.html` | Hero banner, New Releases carousel, Popular Items, How it Works, Ticker |
| **Materials Catalog** | `Philatelic Materials.html` | Dynamic catalog, filters (Circle, Type, Year, Theme), sort, search, pagination |
| **National PDA Dashboard**| `Deposit Account.html` | PDA Balance, Top-up modal, Audit ledger, Wishlist, Speed Post Tracking stepper |
| **Product Detail** | `Product Detail.html?id=1` | High-res gallery, historical notes, quantity selector, instant PDA order |
| **Special Cancellations**| `Special Cancellations.html` | Pictorial postmarks gallery, cancellation request modal, status tracker |
| **Authentication** | `login.html` | Secure Sign In & Registration with automatic PDA account creation |
| **Officer Back-Office** | `admin.html` | Catalog management, order dispatch, status transitions, cancellation processing |

---

## 📡 REST API Reference

### 1. Authentication (`/api/auth`)
- `POST /api/auth/register` — Register collector and activate National PDA account.
- `POST /api/auth/login` — Authenticate and receive JWT Bearer token.
- `GET /api/auth/me` — Retrieve current authenticated session and PDA balance.

### 2. Catalog & Items (`/api/items`)
- `GET /api/items` — Paginated items with filters (`circleId`, `materialType`, `releaseYear`, `theme`, `query`, `sortBy`).
- `GET /api/items/{id}` — Retrieve full item details.
- `GET /api/items/featured` — Retrieve featured new releases.
- `GET /api/items/popular` — Retrieve popular collector items.
- `GET /api/items/{id}/related` — Retrieve related items by thematic category.
- `GET /api/circles` — Retrieve list of India Post Postal Circles.

### 3. Philatelic Deposit Account (`/api/account`)
- `GET /api/account/summary` — PDA account number, balance, pending orders count, total orders.
- `POST /api/account/top-up` — Deposit funds into PDA (UPI, Net Banking, Card).
- `GET /api/account/transactions` — Full audit transaction history with running balance.
- `PUT /api/account/instructions` — Update standing auto-dispatch instructions.

### 4. Orders & Fulfillment (`/api/orders`)
- `POST /api/orders` — Place order using PDA wallet or gateway; auto-generates Speed Post tracking number.
- `GET /api/orders` — List user's orders with current consignment status.
- `GET /api/orders/{orderNumber}` — Retrieve specific order and milestone tracking details.

### 5. Special Cancellations (`/api/cancellations`)
- `GET /api/cancellations` — List active special commemorative postmarks.
- `POST /api/cancellations/request` — Submit special cancellation request on purchased items.
- `GET /api/cancellations/my-requests` — Track status of submitted cancellation requests.

### 6. Admin & Officer Operations (`/api/admin`)
- `GET /api/admin/metrics` — Back-office KPIs (users, orders, catalog stock, active accounts).
- `POST /api/admin/items` — Add new commemorative stamp or material to national catalog.
- `PUT /api/admin/items/{id}` — Update item details and stock levels.
- `DELETE /api/admin/items/{id}` — Delete item from catalog.
- `GET /api/admin/orders` — View all orders across postal circles.
- `PUT /api/admin/orders/{id}/status` — Update order status (`PROCESSING`, `SHIPPED`, `IN_TRANSIT`, `DELIVERED`).
- `GET /api/admin/cancellations/requests` — Review user cancellation requests.
- `PUT /api/admin/cancellations/requests/{id}/status` — Approve or process cancellation requests.

---

## 🧪 Testing

The test suite includes comprehensive tests for:
- Spring context initialization
- JWT authentication flow
- Top-up balance calculation and atomic consistency
- Order creation with PDA balance deduction and stock decrement

```powershell
.\mvnw.cmd test
```
All tests pass with `0 Failures, 0 Errors`.

---

## 🏆 Smart India Hackathon (SIH) Compliance

This project addresses the complete workflow requirements for India Post Philately:
1. Digitize and modernize the National Philately Deposit Account (PDA).
2. Automate standing instructions for commemorative stamp issuances.
3. Enhance transparency with Speed Post tracking integration.
4. Promote India's rich cultural, scientific, and natural heritage through philatelic collections.

