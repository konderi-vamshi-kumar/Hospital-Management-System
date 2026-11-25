# Hospital Management System API

This is the Java backend for the React app in `../Frontend`. It uses Spring
Boot, Spring Security, and SQLite. Patients and staff sign in through the same
authentication API; the signed-in role determines which features are shown.

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

When the server is ready, it listens at `http://localhost:8080`. Startup
creates the SQLite database tables defined in `src/main/resources/schema.sql`.

### Configure the platform super admin

The application does not create a default super-admin account. Configure one
using environment variables before starting the backend:

```powershell
$env:SUPER_ADMIN_USERNAME = "your_admin_name"
$env:SUPER_ADMIN_PASSWORD = "use-a-unique-password-of-at-least-12-characters"
.\mvnw.cmd spring-boot:run
```

The username must contain 3-30 letters, numbers, or underscores. The password
must be at least 12 characters and no more than 72 UTF-8 bytes. The password
is BCrypt-hashed before storage. If the configured username already exists,
startup grants that account the super-admin role without changing its
password. A local `Backend/.env` properties file is loaded when present and is
ignored by Git. Do not put real credentials in tracked source files.

For local HTTP development, the session cookie is configured for localhost.
For an HTTPS deployment, set `SESSION_COOKIE_SECURE=true`.

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
frontend sends requests to `http://localhost:8080`, obtains a CSRF token, and
uses the session cookie for authenticated requests.
To use a different backend address, copy `.env.example` to `.env` in the
`Frontend` folder and change `VITE_API_BASE_URL`.

PowerShell may block the `npm` PowerShell script on some computers. Using
`npm.cmd` as shown avoids that issue.

## Try the API with Postman

1. Start the backend.
2. In Postman, choose **Import** and select
   `Backend/postman/hospital-management-api.postman_collection.json`.
3. Open the **Hospital Management System API** collection.
4. Choose **Get CSRF token**, then **Send**.
5. Choose **Sign up**, then **Send**. The example uses `hospital_demo` and
   `LearnJava123!`.
6. Choose **Sign in**, then **Send** with the same username and password.
7. Use **Current user** to verify the session, then **Sign out** to end it.

To try platform onboarding, configure the super-admin credentials described
above, sign in with that account, and call the `/api/platform/hospitals`
endpoints below. Keep the Postman cookie jar enabled and send a fresh CSRF
token with each state-changing request.

The collection has `baseUrl`, `username`, and `password` variables for
patient authentication, plus separate super-admin and onboarding variables.
Change the patient username before signing up again: usernames must be unique.

## Endpoints

### Get a CSRF token

`GET http://localhost:8080/api/auth/csrf`

The response contains a token. The browser and Postman collection send it as
`X-XSRF-TOKEN`; the matching CSRF cookie is also required for state-changing
requests.

### Create a patient account

`POST http://localhost:8080/api/auth/signup`

```json
{
  "username": "hospital_demo",
  "password": "LearnJava123!"
}
```

Public sign-up creates a patient account. A signed-in user can accept a
hospital staff invitation to receive a hospital-scoped staff membership.

Success response: **201 Created**. Passwords must be at least 8 characters;
usernames must be 3-30 letters, numbers, or underscores.

### Sign in

`POST http://localhost:8080/api/auth/signin`

```json
{
  "username": "hospital_demo",
  "password": "LearnJava123!"
}
```

Success response: **200 OK** and an HTTP-only session cookie. Duplicate
usernames return **409 Conflict**, invalid sign-in details return **401
Unauthorized**, and invalid form data returns **400 Bad Request**. Error
responses include a readable `message`.

### Get the signed-in user

`GET http://localhost:8080/api/auth/me`

Requires the session cookie. The response includes global roles and any
hospital memberships; it never includes a password hash.

### Sign out

`POST http://localhost:8080/api/auth/signout`

Requires the session cookie and CSRF token. Success returns **204 No Content**
and invalidates the session.

## Platform hospital onboarding

All routes in this section require a signed-in account with the
`SUPER_ADMIN` role and a valid CSRF token for POST and PUT requests.

| Method | Route | Purpose |
|---|---|---|
| `GET` | `/api/platform/hospitals` | List hospitals |
| `POST` | `/api/platform/hospitals` | Create a hospital with its name |
| `GET` | `/api/platform/hospitals/{hospitalId}` | View hospital details |
| `PUT` | `/api/platform/hospitals/{hospitalId}` | Update name or set status to `ACTIVE`/`INACTIVE` |
| `GET`, `POST` | `/api/platform/hospitals/{hospitalId}/branches` | List or add branches |
| `PUT` | `/api/platform/hospitals/{hospitalId}/branches/{branchId}` | Update branch details or status |
| `GET`, `POST` | `/api/platform/hospitals/{hospitalId}/departments` | List or add departments |
| `PUT` | `/api/platform/hospitals/{hospitalId}/departments/{departmentId}` | Update department details or status |
| `GET`, `POST` | `/api/platform/hospitals/{hospitalId}/administrators` | List or assign hospital administrators |

The platform super-admin can assign an existing account as the initial
hospital administrator. Assignment is repeatable and reactivates an existing
administrator membership. Branch and department names are
case-insensitively unique within their hospital. Deactivation is used instead
of deleting records.

The platform dashboard appears after a `SUPER_ADMIN` signs in. It provides
hospital, branch, department, and administrator setup in the React frontend.

## Hospital administration and staff invitations

Hospital administrators use the same sign-in portal. After signing in, an
account with an active `HOSPITAL_ADMIN` membership sees the hospital-admin
workspace. The backend checks that membership and active hospital status on
every hospital-admin route; another hospital's IDs are forbidden.

| Method | Route | Purpose |
|---|---|---|
| `GET` | `/api/hospital-admin/hospitals` | List the signed-in admin's active hospitals |
| `GET`, `POST` | `/api/hospital-admin/hospitals/{hospitalId}/branches` | List or add branches |
| `PUT` | `/api/hospital-admin/hospitals/{hospitalId}/branches/{branchId}` | Update a branch or its status |
| `GET`, `POST` | `/api/hospital-admin/hospitals/{hospitalId}/departments` | List or add departments |
| `PUT` | `/api/hospital-admin/hospitals/{hospitalId}/departments/{departmentId}` | Update a department or its status |
| `GET`, `POST` | `/api/hospital-admin/hospitals/{hospitalId}/invitations` | List or create staff invitations |
| `DELETE` | `/api/hospital-admin/hospitals/{hospitalId}/invitations/{invitationId}` | Revoke a pending invitation |
| `POST` | `/api/invitations/accept` | Accept an invitation while signed in |

Hospital admins can invite another `HOSPITAL_ADMIN` or a `DOCTOR`. Invitations
are delivered as one-time codes to copy and share manually; email delivery is
not configured. Codes expire after seven days, are stored as SHA-256 hashes,
and can only be accepted once. The invitee creates an account or signs in,
then submits the code from the signed-in page. The acceptance adds an active
hospital membership without changing the account's global patient role.

Invitation creation request:

```json
{
  "role": "DOCTOR"
}
```

Invitation acceptance request:

```json
{
  "inviteCode": "paste-the-one-time-code-here"
}
```

The raw code is returned only when the invitation is created. The invitation
list never returns it.

After accepting a hospital-admin invitation, sign-in opens the hospital
administration workspace. Staff can create, edit, activate, or deactivate
branches and departments only within their assigned active hospital. Other
hospital IDs return **403 Forbidden**.

## Where to find things

- `src/main/java/.../controller`: receives authentication, platform, and hospital-admin requests.
- `src/main/java/.../service`: applies authentication, onboarding, and invitation rules.
- `src/main/java/.../repository`: reads and writes user records with SQL.
- `src/main/java/.../model`: describes a saved user.
- `src/main/java/.../dto`: describes request and response data.
- `src/main/java/.../exception`: turns expected errors into clear API responses.
- `src/main/java/.../security`: loads account roles and formats security errors.
- `src/main/java/.../hospital`: hospital records and platform hospital endpoints.
- `src/main/java/.../branch`: branch records and database access.
- `src/main/java/.../department`: department records and database access.
- `src/main/java/.../membership`: hospital memberships and one-time staff invitations.
- `src/main/resources/schema.sql`: creates identity, role, hospital, branch, department, membership, and invitation tables.
- `src/test/java/...`: checks authentication, onboarding, tenant access, and invitations.
- `postman`: ready-to-import Postman collection.

SQLite stores a BCrypt password hash, not the original password. Sessions use
HTTP-only cookies, and state-changing requests require a CSRF token. Phase 1
provides authentication and role foundations. Phase 2 implements super-admin
onboarding. Phase 3 adds hospital-admin branch/department management and
hospital-scoped staff invitations. Doctor profiles, rosters, patient
appointment booking, and appointment workflows are planned for later phases.
