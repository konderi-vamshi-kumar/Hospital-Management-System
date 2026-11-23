# Student Management API

This is a small Java backend for the React app in `../Frontend`. It uses
Spring Boot for HTTP endpoints and SQLite for a local database file.

## What you need

- Java Development Kit (JDK) 21 or newer
- Node.js and npm for the frontend
- Postman, if you want to send requests by hand

Java 21 and Node.js are already available on this computer. If you set the
project up on another computer, install a JDK from
[Eclipse Temurin](https://adoptium.net/temurin/releases/?version=21) and
Node.js from [nodejs.org](https://nodejs.org/en/download/), then open a new
terminal and check `java -version` and `node --version`.

Install [Postman Desktop](https://www.postman.com/downloads/) if you want to
send the sample requests below. SQLite is included as a Java library, so this
project does not need a separate SQLite server or installer. An optional
[DB Browser for SQLite](https://sqlitebrowser.org/dl/) app can open the
database file to inspect its tables.

Check Java in PowerShell:

```powershell
java -version
```

The project includes the Maven Wrapper, so you do not need to install Maven
separately. The first run downloads Maven and the Java libraries.

## Start the backend

Open a terminal in the `Backend` folder:

```powershell
.\mvnw.cmd spring-boot:run
```

When the server is ready, it listens at `http://localhost:8080`. The first
start creates `Backend/student-management.db` and its `users` table.

Run the backend tests with:

```powershell
.\mvnw.cmd test
```

## Start the frontend

Open a second terminal in the `Frontend` folder:

```powershell
npm.cmd install
npm.cmd run dev
```

Open the address printed by Vite (usually `http://localhost:5173`). The
frontend sends sign-up and sign-in requests to `http://localhost:8080`.
To use a different backend address, copy `.env.example` to `.env` in the
`Frontend` folder and change `VITE_API_BASE_URL`.

PowerShell may block the `npm` PowerShell script on some computers. Using
`npm.cmd` as shown avoids that issue.

## Try the API with Postman

1. Start the backend.
2. In Postman, choose **Import** and select
   `Backend/postman/student-management-api.postman_collection.json`.
3. Open the **Student Management API** collection.
4. Choose **Sign up**, then **Send**. The example uses `student_demo` and
   `LearnJava123!`.
5. Choose **Sign in**, then **Send** with the same username and password.

The collection has `baseUrl`, `username`, and `password` variables. Change
the username before signing up again: usernames must be unique.

## Endpoints

### Create an account

`POST http://localhost:8080/api/auth/signup`

```json
{
  "username": "student_demo",
  "password": "LearnJava123!"
}
```

Success response: **201 Created**. Passwords must be at least 8 characters;
usernames must be 3-30 letters, numbers, or underscores.

### Sign in

`POST http://localhost:8080/api/auth/signin`

```json
{
  "username": "student_demo",
  "password": "LearnJava123!"
}
```

Success response: **200 OK**. Duplicate usernames return **409 Conflict**,
invalid sign-in details return **401 Unauthorized**, and invalid form data
returns **400 Bad Request**. Error responses include a readable `message`.

## Where to find things

- `src/main/java/.../controller`: receives HTTP requests.
- `src/main/java/.../service`: contains sign-up and sign-in rules.
- `src/main/java/.../repository`: reads and writes user records with SQL.
- `src/main/java/.../model`: describes a saved user.
- `src/main/java/.../dto`: describes request and response data.
- `src/main/java/.../exception`: turns expected errors into clear API responses.
- `src/main/resources/schema.sql`: creates the SQLite users table.
- `src/test/java/...`: checks the sign-up and sign-in endpoints.
- `postman`: ready-to-import Postman collection.

SQLite stores a BCrypt password hash, not the original password. The sign-in
endpoint verifies credentials and returns the user, but this learning version
does not issue a session or token and does not protect other API routes yet.
