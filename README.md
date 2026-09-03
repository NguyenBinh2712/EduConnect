# EduConnect

**Social Learning & Real-Time Chat Platform**

EduConnect is a social learning platform that combines social networking, online quizzes, AI-powered feedback, and real-time communication.

## ✨ Features

* 🔐 **Authentication & Authorization** — JWT Access/Refresh Token, Spring Security, RBAC
* 👨‍🏫 **Teacher Management** — Teacher application and Admin approval
* 🌐 **Social Network** — Posts, comments, reactions, friendships, groups, search
* 📝 **Quiz System** — Quiz creation, attempts, submission and results
* 🤖 **AI Review** — Google Gemini-powered quiz analysis and feedback
* 💬 **Real-Time Chat** — 1-1/group chat with WebSocket + STOMP
* 🔔 **Real-Time Notifications** — WebSocket notifications
* ☁️ **Media Storage** — Cloudinary for images/videos
* 📧 **Email** — OTP verification and password recovery via SMTP

## 🛠️ Tech Stack

| Category | Technology               |
| -------- | ------------------------ |
| Backend  | Java 21, Spring Boot     |
| Security | Spring Security, JWT     |
| Database | MySQL, MongoDB           |
| Realtime | WebSocket, STOMP, SockJS |
| AI       | Google Gemini API        |
| Storage  | Cloudinary               |
| Email    | Spring Mail / SMTP       |
| Build    | Maven                    |

## 📁 Structure

```text
src/main/java/com/example/DATN/
├── config/
├── controller/
├── service/
├── entity/
├── dto/
└── repository/
```

## ⚙️ Requirements

* JDK 21+
* Maven 3.9+
* MySQL 8+
* MongoDB 6+
* Google Gemini API Key
* Cloudinary Account
* Gmail App Password

## 🔧 Configuration

Create:

```text
src/main/resources/application.yaml
```

Example:

```yaml
spring:
  application:
    name: DATN

  datasource:
    url: jdbc:mysql://localhost:3306/doan
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  data:
    mongodb:
      uri: mongodb://localhost:27017/datn

  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_APP_PASSWORD}

server:
  port: 8080
  servlet:
    context-path: /doan

jwt:
  secretKey: ${JWT_SECRET_KEY}
  valid-duration: 15
  refresh-duration: 3

cloudinary:
  cloud_name: ${CLOUDINARY_CLOUD_NAME}
  api_key: ${CLOUDINARY_API_KEY}
  api_secret: ${CLOUDINARY_API_SECRET}

gemini:
  api-key: ${GEMINI_API_KEY}
  model: gemini-2.5-flash
```

> ⚠️ Do not commit real passwords, API keys, JWT secrets or other sensitive credentials to Git.

## ▶️ Run

```bash
mvn clean install
mvn spring-boot:run
```

Application:

```text
http://localhost:8080/doan
```
