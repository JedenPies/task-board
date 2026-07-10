# Task Board (Kanban Application)

A real-time Kanban board application built with Java (Spring Boot) and Angular. This project allows users to manage tasks efficiently with seamless real-time synchronization.

## 🚀 Features

### 👤 Anonymous Usage
* **No login required:** You can start using the application immediately without creating an account.
* **Public Boards:** Unauthenticated users can create public task boards.
* **Link Sharing:** Anyone with the board ID (URL) can access and collaborate on a public board.
* **Privacy Restriction:** Unauthenticated users cannot view a list or directory of existing boards.

### 🔐 Registered Users
* **Advanced Control:** Authenticated users can create both *private* and *public* boards.
* **Privacy Management:** Ability to change the privacy status of a board at any time.
* **Personal Dashboard:** Logged-in users have access to a dashboard displaying all their created boards.

### ⚙️ Technical Highlights
* **Real-Time Updates:** Powered by Server-Sent Events (SSE) and modern Angular Signals for instant UI updates across all connected clients without page reloads.
* **Secure Authentication:** Dedicated microservice for authentication using JWT, HTTP-Only cookies, and Argon2 password hashing.

## 🛠️ Tech Stack

**Backend:**
* Java
* Spring Boot
* Spring Security
* PostgreSQL
* Liquibase

**Frontend:**
* Angular (Zoneless, Signals)
* TypeScript
* SCSS

**Infrastructure:**
* Docker & Docker Compose

## 🐳 Getting Started

To run the application locally, you can use Docker Compose which will spin up the databases, authentication service, backend service, and frontend application.

```bash
docker-compose up --build
