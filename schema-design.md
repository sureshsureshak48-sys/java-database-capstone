# Smart Clinic Management System - MySQL Schema Design

## Entity-Relationship Overview
- **Admins** manage system users and doctors.
- **Doctors** have multiple available time slots and manage multiple appointments and prescriptions.
- **Patients** book multiple appointments and receive prescriptions.
- **Appointments** link a `Patient` and a `Doctor` at a scheduled date and time (`1:N` from both `doctors` and `patients`).
- **Prescriptions** are tied to a specific `Appointment`, `Doctor`, and `Patient`.

---

## Database Tables

### 1. `admins`
Stores system administrator credentials.

| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `PRIMARY KEY, AUTO_INCREMENT` | Unique admin identifier |
| `username` | `VARCHAR(50)` | `NOT NULL, UNIQUE` | Admin login username |
| `email` | `VARCHAR(100)` | `NOT NULL, UNIQUE` | Admin email address |
| `password` | `VARCHAR(255)` | `NOT NULL` | Hashed password |

---

### 2. `doctors`
Stores doctor profiles and specializations.

| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `PRIMARY KEY, AUTO_INCREMENT` | Unique doctor identifier |
| `name` | `VARCHAR(100)` | `NOT NULL` | Full name of the doctor |
| `speciality` | `VARCHAR(100)` | `NOT NULL` | Medical specialty (e.g., Cardiology) |
| `email` | `VARCHAR(100)` | `NOT NULL, UNIQUE` | Doctor login email |
| `phone` | `VARCHAR(20)` | `NOT NULL, UNIQUE` | Contact phone number |
| `password` | `VARCHAR(255)` | `NOT NULL` | Hashed password |

---

### 3. `doctor_available_times`
Stores available appointment time slots for each doctor (`@ElementCollection`).

| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `doctor_id` | `BIGINT` | `NOT NULL, FOREIGN KEY REFERENCES doctors(id)` | Associated doctor ID |
| `available_times` | `VARCHAR(50)` | `NOT NULL` | Available time slot (e.g., `09:00-10:00`) |

---

### 4. `patients`
Stores registered patient information.

| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `PRIMARY KEY, AUTO_INCREMENT` | Unique patient identifier |
| `name` | `VARCHAR(100)` | `NOT NULL` | Full name of the patient |
| `email` | `VARCHAR(100)` | `NOT NULL, UNIQUE` | Patient login email |
| `phone` | `VARCHAR(20)` | `NOT NULL, UNIQUE` | Contact phone number |
| `password` | `VARCHAR(255)` | `NOT NULL` | Hashed password |
| `address` | `VARCHAR(255)` | `NULL` | Residential address |

---

### 5. `appointments`
Tracks scheduled consultations between patients and doctors.

| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `PRIMARY KEY, AUTO_INCREMENT` | Unique appointment identifier |
| `doctor_id` | `BIGINT` | `NOT NULL, FOREIGN KEY REFERENCES doctors(id)` | Assigned doctor |
| `patient_id` | `BIGINT` | `NOT NULL, FOREIGN KEY REFERENCES patients(id)` | Booking patient |
| `appointment_time` | `DATETIME` | `NOT NULL` | Scheduled date and time |
| `status` | `INT` | `NOT NULL DEFAULT 0` | Status (`0` = Scheduled, `1` = Completed, `2` = Cancelled) |

---

### 6. `prescriptions`
Stores medications and notes issued during appointments.

| Column | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | `PRIMARY KEY, AUTO_INCREMENT` | Unique prescription identifier |
| `appointment_id` | `BIGINT` | `NOT NULL, FOREIGN KEY REFERENCES appointments(id)` | Related appointment |
| `doctor_id` | `BIGINT` | `NOT NULL, FOREIGN KEY REFERENCES doctors(id)` | Prescribing doctor |
| `patient_id` | `BIGINT` | `NOT NULL, FOREIGN KEY REFERENCES patients(id)` | Receiving patient |
| `medication` | `VARCHAR(255)` | `NOT NULL` | Prescribed medicine(s) |
| `dosage` | `VARCHAR(100)` | `NOT NULL` | Dosage instructions |
| `doctor_notes` | `TEXT` | `NULL` | Additional clinical notes |
