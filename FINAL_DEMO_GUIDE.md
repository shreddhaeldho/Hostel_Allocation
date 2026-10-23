# Final College Demonstration Guide
## Smart Hostel Room Allocation System
**B.Tech Final Project Demonstration & Viva Preparation**

This guide provides a step-by-step walkthrough for demonstrating the Smart Hostel Room Allocation System to your professor and panel, complete with exact API requests, expected responses, SQL verification queries, and answers to likely viva/evaluation questions.

---

## 1. Demonstration Sequence for the Presentation

Follow these 12 steps in sequence during your presentation:

### Step 1: Problem Statement Explanation (Speech)
> *"Good morning, Professor. In traditional university hostels, room allocation is conducted manually using spreadsheets or paper logs. This manual process often leads to double allocations (two students assigned to the same bed), over-allocation beyond room capacity, preference mismatches, and severe record inconsistency when students vacate rooms. Our project provides an automated, transactional REST API backend that eliminates manual errors, optimizes room occupancy, enforces strict database integrity, and ensures thread-safe concurrent allocations."*

### Step 2: System Architecture Overview
Open the **README.md** or display the 4-layer architecture:
- **Controller Layer** (`@RestController`): Accepts client HTTP requests, validates inputs via `@Valid`, and returns appropriate HTTP status codes (`200`, `201`, `400`, `404`, `409`).
- **Service Layer** (`@Service`, `@Transactional`): Encapsulates smart matching algorithms, room capacity sorting, pessimistic locking, and business invariants.
- **Repository Layer** (`@Repository`): Leverages Spring Data JPA interfaces for database operations.
- **Entity / Database Layer**: Maps `Student`, `Room`, and `Allocation` tables into MySQL 8.0 with foreign keys and unique constraints.

---

### Step 3: Database & Entity Relationship Explanation
Explain the three core entities:
1. `Student`: Stores student identity, department, and preferred room type (`Single`, `Double`, etc.).
2. `Room`: Tracks room type, maximum capacity, and current `occupied_beds`.
3. `Allocation`: Connects a student to a room. Contains a **UNIQUE constraint** on `student_id` so no student can have multiple active rooms.

---

### Step 4: Register Students with Different Room Preferences
Use Postman or curl to register two students:

#### Request 4A: Register Student 1
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/students`
- **Headers**: `Content-Type: application/json`
- **Body**:
  ```json
  {
      "name": "Dev Sharma",
      "department": "CSE",
      "preferredRoomType": "Single"
  }
  ```
- **Expected Status**: `201 Created`
- **Note to show**: Point out the generated `id` (e.g. `1` or `3`).

#### Request 4B: Register Student 2
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/students`
- **Headers**: `Content-Type: application/json`
- **Body**:
  ```json
  {
      "name": "Ishaan Verma",
      "department": "ECE",
      "preferredRoomType": "Double"
  }
  ```
- **Expected Status**: `201 Created`

---

### Step 5: Add Rooms with Different Types and Capacities

#### Request 5A: Add Single Room
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/rooms`
- **Body**:
  ```json
  {
      "roomType": "Single",
      "capacity": 1
  }
  ```
- **Expected Status**: `201 Created`
- **Note to show**: `occupiedBeds` automatically defaults to `0`.

#### Request 5B: Add Double Room
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/rooms`
- **Body**:
  ```json
  {
      "roomType": "Double",
      "capacity": 2
  }
  ```
- **Expected Status**: `201 Created`

---

### Step 6: Demonstrate Smart Room Allocation
Allocate room for Student 1 (Dev Sharma, who preferred "Single"):

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/allocations/1` *(replace `1` with Dev's student ID)*
- **Expected Status**: `201 Created`
- **Expected Response**:
  ```json
  {
      "allocationId": 1,
      "studentId": 1,
      "studentName": "Dev Sharma",
      "department": "CSE",
      "roomId": 1,
      "roomType": "Single",
      "occupiedBeds": 1,
      "capacity": 1
  }
  ```
- **Key point to highlight to Professor**:
  *"The system automatically matched Dev's 'Single' room preference, incremented the room's occupied beds to 1, and created an active allocation record."*

---

### Step 7: Retrieve All Active Allocations
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/allocations`
- **Expected Status**: `200 OK`
- **Response**: Shows the list of active student-to-room allocations formatted via `AllocationResponse` DTO without circular JSON references.

---

### Step 8: Demonstrate Duplicate Allocation Prevention
Attempt to allocate Student 1 again:

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/allocations/1`
- **Expected Status**: `409 Conflict`
- **Expected Response**:
  ```json
  {
      "timestamp": "2026-10-21T...",
      "status": 409,
      "error": "Conflict",
      "message": "Student already has an active room allocation.",
      "path": "/api/allocations/1"
  }
  ```
- **Key point to highlight**:
  *"The system actively prevents double-allocation. The room occupancy is NOT modified, and the client receives a clear 409 Conflict."*

---

### Step 9: Vacate an Allocated Room
Vacate Student 1:

- **Method**: `DELETE`
- **URL**: `http://localhost:8080/api/allocations/1`
- **Expected Status**: `200 OK`
- **Expected Response**:
  ```json
  {
      "message": "Room vacated successfully",
      "studentId": 1,
      "studentName": "Dev Sharma",
      "roomId": 1,
      "updatedOccupiedBeds": 0
  }
  ```

---

### Step 10: Show That Room Occupancy Decreased
Retrieve Room details:

- **Method**: `GET`
- **URL**: `http://localhost:8080/api/rooms/1`
- **Expected Status**: `200 OK`
- **Expected Response**:
  ```json
  {
      "id": 1,
      "roomType": "Single",
      "capacity": 1,
      "occupiedBeds": 0
  }
  ```
- **Key point to highlight**:
  *"Notice that occupiedBeds decreased from 1 back to 0, and the allocation record was deleted atomically."*

---

### Step 11: Re-allocate the Vacated Bed
Demonstrate bed re-usability:

- **Method**: `POST`
- **URL**: `http://localhost:8080/api/allocations/1`
- **Expected Status**: `201 Created`
- **Key point to highlight**:
  *"Now that the bed was freed, Student 1 can be successfully re-allocated to the available room."*

---

### Step 12: Verify Database Invariants in MySQL Workbench / CLI
Open MySQL and execute:

```sql
USE hostel_db;

-- 1. View all student records
SELECT * FROM student;

-- 2. View all room records and occupancy
SELECT * FROM room;

-- 3. View active allocation records
SELECT * FROM allocation;

-- 4. Invariant Check: Verify NO room exceeds capacity
SELECT * FROM room WHERE occupied_beds > capacity;
-- (Expected: 0 rows)

-- 5. Invariant Check: Verify NO room has negative occupancy
SELECT * FROM room WHERE occupied_beds < 0;
-- (Expected: 0 rows)

-- 6. Invariant Check: Verify NO student has duplicate active allocations
SELECT student_id, COUNT(*) AS active_allocations 
FROM allocation 
GROUP BY student_id 
HAVING COUNT(*) > 1;
-- (Expected: 0 rows)

-- 7. Invariant Check: Verify room.occupied_beds EXACTLY matches allocation count
SELECT r.id AS room_id, r.room_type, r.capacity, r.occupied_beds, COUNT(a.id) AS actual_allocations
FROM room r 
LEFT JOIN allocation a ON r.id = a.room_id 
GROUP BY r.id, r.room_type, r.capacity, r.occupied_beds 
HAVING r.occupied_beds <> COUNT(a.id);
-- (Expected: 0 rows)
```
Show the panel that all invariant queries return 0 violation rows, proving mathematical consistency between entity records and actual bed occupancies.

---

## 2. Comprehensive Viva / Professor Q&A Preparation

### Q1: What problem does this project solve?
**Answer**: It replaces manual, paper- or spreadsheet-based hostel room allotment with an automated, transactional REST service. It prevents human errors such as double allocation, room over-allocation, and untracked vacating.

### Q2: What makes the allocation process "smart"?
**Answer**:
1. **Preference Matching**: It filters rooms matching the student's preferred room type (case-insensitive and trimmed).
2. **Capacity Optimization**: When multiple eligible rooms exist, it prioritizes rooms with the highest number of available beds (`capacity - occupiedBeds`), maximizing availability across the hostel.
3. **Deterministic Tie-Breaking**: If available capacities are equal, it breaks ties deterministically using the lowest room ID.
4. **Concurrency Safety**: It uses JPA pessimistic write locking (`SELECT ... FOR UPDATE`) with in-transaction database re-checking, ensuring that simultaneous requests competing for the final available bed never exceed room capacity.

### Q3: Why did you choose Spring Boot?
**Answer**: Spring Boot simplifies enterprise application development through opinionated autoconfiguration, embedded Tomcat server, declarative transaction management (`@Transactional`), and dependency injection. It allows building production-ready REST microservices with minimal boilerplate.

### Q4: Why use MySQL?
**Answer**: MySQL is an ACID-compliant relational database management system. Because room allocation involves relational foreign keys (`allocation` referencing `student` and `room`) and requires strict transactional consistency (atomicity, consistency, isolation, durability), a relational database with foreign key and unique constraints is essential.

### Q5: What is the role of JPA and Hibernate?
**Answer**: Java Persistence API (JPA) is the standard Java specification for object-relational mapping (ORM). Hibernate is the underlying JPA implementation. It maps Java entity classes (`Student`, `Room`, `Allocation`) to database tables, generates optimized SQL queries, handles database locking, and manages transaction lifecycles.

### Q6: Can you explain the 4 layers of your application?
**Answer**:
1. **Entity Layer**: Defines Java objects that map to database tables and their relational constraints.
2. **Repository Layer**: Provides database access interfaces using Spring Data JPA (`JpaRepository`).
3. **Service Layer**: Contains core business logic, validation rules, algorithm calculations, and transaction boundaries.
4. **Controller Layer**: Exposes RESTful endpoints, handles HTTP requests, and serializes responses into clean JSON payloads.

### Q7: How is duplicate allocation prevented?
**Answer**: It is enforced at two distinct levels (defense-in-depth):
1. **Service Level**: Before allocating, `allocationRepository.existsByStudentId(studentId)` checks if an allocation already exists and immediately throws an `AllocationConflictException` (HTTP 409).
2. **Database Level**: The `allocation` table has a `UNIQUE KEY (student_id)`. Even in the event of an unexpected race condition, the database constraint prevents duplicate records by throwing a Data Integrity Violation.

### Q8: How is room capacity maintained?
**Answer**:
- Rooms can only be created with positive capacity (`capacity >= 1`).
- During allocation, candidate rooms are filtered by `occupiedBeds < capacity`.
- Pessimistic locking locks the selected room record in MySQL (`FOR UPDATE`), refreshes the latest committed data from the database, and re-verifies that `occupiedBeds < capacity`.
- Once confirmed, `occupiedBeds` is incremented by 1 and flushed within the transaction.

### Q9: What happens if no suitable room is available?
**Answer**: The system aborts the operation and throws an `AllocationConflictException`, which our `GlobalExceptionHandler` converts into an HTTP `409 Conflict` response with an explanatory message: *"No available room matches the student's preferred room type"*. The room occupancy and student status remain untouched.

### Q10: How does vacating a room work?
**Answer**:
- The client issues `DELETE /api/allocations/{studentId}`.
- Within an atomic transaction, the service looks up the active allocation, locks the room, decrements `occupiedBeds` by 1 (using `Math.max(0, occupiedBeds - 1)` so it never drops below zero), deletes the allocation record, and returns a confirmation JSON response.

### Q11: How do transactions maintain consistency?
**Answer**: By annotating service methods with `@Transactional`, Spring ensures that incrementing room occupancy and saving the allocation record occur within a single database transaction. If any step fails (e.g., constraint error, server crash), the entire transaction rolls back automatically, preventing partial or corrupt states.

### Q12: How would this system scale to a larger hostel campus with thousands of students?
**Answer**:
1. **Connection Pooling**: HikariCP handles connection pooling for high throughput.
2. **Database Indexing**: Adding composite indexes on `room(room_type, occupied_beds, capacity)`.
3. **Caching**: Frequently queried static data (e.g., room types, building blocks) can be cached using Redis.
4. **Horizontal Scaling**: Because the application is stateless and concurrency is protected at the database row-locking level, multiple instances of the Spring Boot application can run behind a load balancer (e.g., NGINX).
