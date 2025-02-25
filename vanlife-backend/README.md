# 🚐 VanLife Navigator

**VanLife Navigator** is a web application designed to help van lifers find safe places to sleep, access water, eat affordably, locate gyms, showers, attractions, and other essential resources. The long-term vision includes AI-powered route planning and itinerary suggestions.

## 🌍 Features (MVP)
- 🔐 **User Authentication** – Signup/Login with JWT-based authentication
- 📍 **Location Database** – Find and add van life-friendly locations (camping spots, water stations, showers, etc.)
- 🔎 **Search & Filtering** – Filter locations by type, amenities, and location
- ⭐ **Reviews & Ratings** – User feedback on places
- 🗺️ **Map Integration** – View locations on an interactive map

### 🚀 Future Enhancements
- 🛣️ **AI-Powered Route Planning & Itineraries**
- 🏕️ **Community Features** (saved locations, user profiles, comments)
- 💰 **Monetization** (ads, premium features)

---

## 🛠️ Tech Stack

### **Backend**
- **Java / Spring Boot** – REST API
- **Spring Security + JWT** – Authentication
- **Spring Data JPA + PostgreSQL** – Database
- **Google Maps API / OpenStreetMap** – Location & mapping
- **Docker** – Containerization for deployment

### **Frontend**
- **Vue 3 + TypeScript** – UI Framework
- **Vue Router** – Navigation
- **Pinia / Vuex** – State management
- **Axios** – API calls
- **Leaflet.js / Google Maps API** – Map display

### **Infrastructure**
- **Docker + Docker Compose** – Local development
- **CI/CD** – GitHub Actions or GitLab CI
- **Deployment** – Heroku, Render, Fly.io, or VPS (DigitalOcean)
- **Database Hosting** – Supabase, ElephantSQL, or self-hosted PostgreSQL

---

## 📦 Getting Started

### **1️⃣ Clone the Repository**
```sh
git clone https://github.com/yourusername/vanlife-navigator.git
cd vanlife-navigator
```
### **2️⃣ Set Environment Variables**
* DB_USERNAME
* DB_PASSWORD
* JWT_EXPIRATION
* JWT_SECRET
