# Splitwise Backend API

A complete Spring Boot backend that replicates the core functionality of Splitwise:
users, groups, expenses with equal-split sharing, balance calculation, and settlements.

## Project Overview

This project provides a REST API to:
- Manage users
- Create groups and add members
- Record expenses inside a group, split equally among participants
- Calculate balances (who owes whom, and how much) within a group
- Record settlements between users and have balances reflect them

The project uses an in-memory H2 database, so it starts up ready-to-use with no
external database setup required.

## Tech Stack

- Java 17
- Spring Boot 3.3.4
  - Spring Web (REST controllers)
  - Spring Data JPA (persistence)
- H2 Database (in-memory)
- Maven (build tool)

## Architecture

The project follows a standard layered architecture:

```
Controller  ->  Service (interface + impl)  ->  Repository  ->  Entity
                        |
                       DTO (request/response objects)
```

- **Controller** – exposes REST endpoints, delegates to services
- **Service** – interfaces + implementations containing business logic
  (equal-split calculation, balance computation, debt simplification)
- **Repository** – Spring Data JPA repositories
- **Entity** – JPA entities mapped to H2 tables
- **DTO** – request/response objects so entities are never exposed directly
- **Exception** – centralized exception handling via `@RestControllerAdvice`

## Package Structure

```
com.splitwise.backend
├── SplitwiseBackendApplication.java   Main Spring Boot application class
├── DataSeeder.java                    Optional sample data seeded on startup
├── controller/
│   ├── UserController.java
│   ├── GroupController.java
│   ├── ExpenseController.java
│   ├── BalanceController.java
│   └── SettlementController.java
├── service/
│   ├── UserService.java               (interface)
│   ├── GroupService.java              (interface)
│   ├── ExpenseService.java            (interface)
│   ├── BalanceService.java            (interface)
│   ├── SettlementService.java         (interface)
│   └── impl/
│       ├── UserServiceImpl.java
│       ├── GroupServiceImpl.java
│       ├── ExpenseServiceImpl.java
│       ├── BalanceServiceImpl.java
│       └── SettlementServiceImpl.java
├── repository/
│   ├── UserRepository.java
│   ├── GroupRepository.java
│   ├── ExpenseRepository.java
│   ├── ExpenseParticipantRepository.java
│   └── SettlementRepository.java
├── entity/
│   ├── User.java
│   ├── Group.java
│   ├── Expense.java
│   ├── ExpenseParticipant.java
│   └── Settlement.java
├── dto/
│   ├── UserRequest.java / UserResponse.java
│   ├── GroupCreateRequest.java / AddMembersRequest.java / GroupResponse.java
│   ├── ExpenseCreateRequest.java / ExpenseResponse.java / ExpenseParticipantResponse.java
│   ├── SettlementRequest.java / SettlementResponse.java
│   └── UserBalance.java / DebtResponse.java / GroupBalanceResponse.java
└── exception/
    ├── ResourceNotFoundException.java
    ├── BadRequestException.java
    ├── ErrorResponse.java
    └── GlobalExceptionHandler.java
```

## Entity Relationships

- A **User** can belong to many **Groups** (many-to-many)
- A **Group** has many **Users** as members
- A **Group** has many **Expenses**
- An **Expense** belongs to one **Group**, is paid by one **User** (`paidBy`),
  and has many **ExpenseParticipant** rows (one per participant, storing their share)
- A **Settlement** belongs to one **Group** and links two **Users** (`paidBy` -> `paidTo`)

## Features Implemented

1. **Users** – create user, get user by id, get all users
2. **Groups** – create group (optionally with initial members), add members to an
   existing group, view a group and its members
3. **Expenses** – create an expense inside a group with a description, amount,
   payer, and list of participants
4. **Expense Sharing** – when an expense is created, the amount is split **equally**
   among all participants. Any rounding remainder (a few cents, due to division) is
   added to the first participant so the shares always sum up exactly to the total
   amount.
5. **Balances** – for any group, compute:
   - the net balance of every member (positive = is owed money, negative = owes money)
   - a simplified list of "who owes whom and how much" (minimum number of
     transactions needed to settle the group, using a greedy debt-simplification
     algorithm)
6. **Settlements** – record a payment from one user to another within a group.
   Balances automatically reflect settlements the next time they are calculated.

## Database Configuration

The app uses an in-memory H2 database, configured in
`src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:splitwisedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

Since it's an in-memory database, all data resets every time the application restarts.
On every startup, `DataSeeder` (optional, safe to delete) inserts a few sample users,
a group, two expenses, and one settlement, so there's immediately something to query.

### H2 Console

Once the app is running, open:

```
http://localhost:8081/h2-console
```

Connection settings:
- **JDBC URL:** `jdbc:h2:mem:splitwisedb`
- **User Name:** `sa`
- **Password:** *(leave blank)*

## How to Run

### Prerequisites
- Java 17 or later
- Maven 3.6+ (or use an IDE with built-in Maven support)

### Option 1: Command line

```bash
cd splitwise-backend
mvn spring-boot:run
```

The application will start on **http://localhost:8081**.

### Option 2: IntelliJ IDEA / Eclipse

1. Open the project folder as a Maven project (File → Open, select the folder
   containing `pom.xml`).
2. Let the IDE download dependencies.
3. Run `SplitwiseBackendApplication.java` as a Java application.

### Option 3: Build a jar and run it

```bash
mvn clean package
java -jar target/splitwise-backend.jar
```

## API Endpoint Reference

Base URL: `http://localhost:8081`

| Method | Endpoint                                   | Description                        |
|--------|---------------------------------------------|-------------------------------------|
| POST   | `/api/users`                                | Create a user                       |
| GET    | `/api/users/{id}`                           | Get a user by id                    |
| GET    | `/api/users`                                | Get all users                       |
| POST   | `/api/groups`                               | Create a group                      |
| POST   | `/api/groups/{groupId}/members`             | Add members to a group              |
| GET    | `/api/groups/{groupId}`                     | View a group and its members        |
| POST   | `/api/groups/{groupId}/expenses`            | Create an expense in a group        |
| GET    | `/api/groups/{groupId}/expenses`            | List all expenses in a group        |
| GET    | `/api/groups/{groupId}/balances`            | Get balances for a group            |
| POST   | `/api/groups/{groupId}/settlements`         | Record a settlement in a group      |
| GET    | `/api/groups/{groupId}/settlements`         | List all settlements in a group     |

---

## Sample Requests & Responses

### 1. Create User

**POST** `/api/users`

Request:
```json
{
  "name": "Alice",
  "email": "alice@example.com"
}
```

Response (`201 Created`):
```json
{
  "id": 1,
  "name": "Alice",
  "email": "alice@example.com"
}
```

### 2. Get User

**GET** `/api/users/1`

Response (`200 OK`):
```json
{
  "id": 1,
  "name": "Alice",
  "email": "alice@example.com"
}
```

### 3. Get All Users

**GET** `/api/users`

Response (`200 OK`):
```json
[
  { "id": 1, "name": "Alice", "email": "alice@example.com" },
  { "id": 2, "name": "Bob", "email": "bob@example.com" },
  { "id": 3, "name": "Charlie", "email": "charlie@example.com" }
]
```

### 4. Create Group

**POST** `/api/groups`

Request:
```json
{
  "name": "Goa Trip",
  "description": "Expenses for the Goa trip",
  "memberIds": [1, 2, 3]
}
```

Response (`201 Created`):
```json
{
  "id": 1,
  "name": "Goa Trip",
  "description": "Expenses for the Goa trip",
  "members": [
    { "id": 1, "name": "Alice", "email": "alice@example.com" },
    { "id": 2, "name": "Bob", "email": "bob@example.com" },
    { "id": 3, "name": "Charlie", "email": "charlie@example.com" }
  ]
}
```

### 5. Add Members to Group

**POST** `/api/groups/1/members`

Request:
```json
{
  "memberIds": [4]
}
```

Response (`200 OK`):
```json
{
  "id": 1,
  "name": "Goa Trip",
  "description": "Expenses for the Goa trip",
  "members": [
    { "id": 1, "name": "Alice", "email": "alice@example.com" },
    { "id": 2, "name": "Bob", "email": "bob@example.com" },
    { "id": 3, "name": "Charlie", "email": "charlie@example.com" },
    { "id": 4, "name": "Diana", "email": "diana@example.com" }
  ]
}
```

### 6. View Group

**GET** `/api/groups/1`

Response (`200 OK`):
```json
{
  "id": 1,
  "name": "Goa Trip",
  "description": "Expenses for the Goa trip",
  "members": [
    { "id": 1, "name": "Alice", "email": "alice@example.com" },
    { "id": 2, "name": "Bob", "email": "bob@example.com" },
    { "id": 3, "name": "Charlie", "email": "charlie@example.com" }
  ]
}
```

### 7. Create Expense (Equal Split)

**POST** `/api/groups/1/expenses`

Request:
```json
{
  "description": "Hotel booking",
  "amount": 3000.00,
  "paidById": 1,
  "participantIds": [1, 2, 3]
}
```

Response (`201 Created`):
```json
{
  "id": 1,
  "description": "Hotel booking",
  "amount": 3000.00,
  "groupId": 1,
  "paidBy": { "id": 1, "name": "Alice", "email": "alice@example.com" },
  "participants": [
    { "userId": 1, "name": "Alice", "shareAmount": 1000.00 },
    { "userId": 2, "name": "Bob", "shareAmount": 1000.00 },
    { "userId": 3, "name": "Charlie", "shareAmount": 1000.00 }
  ],
  "createdAt": "2026-07-26T10:15:30"
}
```

### 8. List Expenses in a Group

**GET** `/api/groups/1/expenses`

Response (`200 OK`):
```json
[
  {
    "id": 1,
    "description": "Hotel booking",
    "amount": 3000.00,
    "groupId": 1,
    "paidBy": { "id": 1, "name": "Alice", "email": "alice@example.com" },
    "participants": [
      { "userId": 1, "name": "Alice", "shareAmount": 1000.00 },
      { "userId": 2, "name": "Bob", "shareAmount": 1000.00 },
      { "userId": 3, "name": "Charlie", "shareAmount": 1000.00 }
    ],
    "createdAt": "2026-07-26T10:15:30"
  }
]
```

### 9. Get Group Balances

**GET** `/api/groups/1/balances`

Response (`200 OK`):
```json
{
  "groupId": 1,
  "netBalances": [
    { "userId": 1, "name": "Alice", "netBalance": 2000.00 },
    { "userId": 2, "name": "Bob", "netBalance": -1000.00 },
    { "userId": 3, "name": "Charlie", "netBalance": -1000.00 }
  ],
  "simplifiedDebts": [
    { "fromUserId": 2, "fromUserName": "Bob", "toUserId": 1, "toUserName": "Alice", "amount": 1000.00 },
    { "fromUserId": 3, "fromUserName": "Charlie", "toUserId": 1, "toUserName": "Alice", "amount": 1000.00 }
  ]
}
```

`netBalance` is positive when the user is owed money overall, and negative when the
user owes money overall. `simplifiedDebts` gives the minimum number of payments
needed for everyone to be settled up.

### 10. Create Settlement

**POST** `/api/groups/1/settlements`

Request:
```json
{
  "paidById": 2,
  "paidToId": 1,
  "amount": 1000.00
}
```

Response (`201 Created`):
```json
{
  "id": 1,
  "groupId": 1,
  "paidBy": { "id": 2, "name": "Bob", "email": "bob@example.com" },
  "paidTo": { "id": 1, "name": "Alice", "email": "alice@example.com" },
  "amount": 1000.00,
  "settledAt": "2026-07-26T10:20:00"
}
```

After this settlement, calling `GET /api/groups/1/balances` again would show Bob's
balance updated to `0.00`, since his debt to Alice has been fully settled.

### 11. List Settlements in a Group

**GET** `/api/groups/1/settlements`

Response (`200 OK`):
```json
[
  {
    "id": 1,
    "groupId": 1,
    "paidBy": { "id": 2, "name": "Bob", "email": "bob@example.com" },
    "paidTo": { "id": 1, "name": "Alice", "email": "alice@example.com" },
    "amount": 1000.00,
    "settledAt": "2026-07-26T10:20:00"
  }
]
```

---

## Error Response Format

All errors return a consistent JSON body:

```json
{
  "timestamp": "2026-07-26T10:25:00",
  "status": 404,
  "error": "Not Found",
  "message": "Group not found with id: 99",
  "path": "/api/groups/99"
}
```

- `404 Not Found` – when a referenced user, group, expense, etc. does not exist
- `400 Bad Request` – for invalid input (e.g. missing fields, non-member participant,
  duplicate email, zero/negative amount)
- `500 Internal Server Error` – for any unexpected error

## Notes on Design Decisions

- The `Group` entity maps to the table `app_group` (instead of `group`) because
  `GROUP` is a reserved keyword in SQL.
- Expense splitting is always **equal split** among the given participants, as
  specified in the requirements. Any rounding difference (a few cents) from dividing
  the amount is added to the first participant's share, so shares always sum exactly
  to the expense amount.
- Balances are computed on-the-fly from the full history of expenses and
  settlements each time the balances endpoint is called, rather than being stored
  as a running total. This keeps the data model simple and always consistent.
