# HRGenius – HR Management System

HCLTech Career Shaper Program | Java 17 | Spring Boot 3.3.5 | MySQL / H2

## Student
Ishan Arora · Roll No. 2300321530091 · ishan.23b1531004@abes.ac.in  
ABES Engineering College · Documentation version 1.0

## Frontend update in this delivery
The browser interface under `src/main/resources/static/` has been redesigned as a responsive HR workspace. It includes:

- Sign-in and candidate account registration screens.
- Role-filtered navigation for ADMIN, HR_MANAGER, MANAGER, EMPLOYEE and CANDIDATE.
- Overview, employee directory with search/pagination/edit/deactivate, departments, job openings, candidate applications, interview scheduling/feedback, onboarding, salary structures, payroll history/run/payslip download, performance cycles/goals/self-assessment/reviews, analytics charts, and change-password views.
- API-backed data only in the application. Empty states are shown when the API returns no records; the UI does not seed mock business records.
- HTML escaping for values received from the API before they are inserted into the page, plus visible request errors and success notifications.

The interface expects the REST API paths described in the project documentation and the code. Several of those endpoints are not present in the current backend source. The frontend therefore represents the intended client for the documented API contract; it is not a claim that every screen can currently be exercised against the included backend.

## Backend status
The existing backend source has been retained and has not been expanded to implement every requested workflow in this delivery. In particular, the complete recruitment, interviews, onboarding, payroll, performance, analytics and full authentication endpoint set is not available in the current server code. Entity relationships, security rules and database constraints also require further backend work. Do not describe the backend as feature-complete or fully verified.

## Requirements to run the backend
- JDK 17
- Maven 3.9+
- MySQL 8+ (or H2 for tests)
- Modern browser

## Run
1. Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` in the environment.
2. Configure the administrator credential using `HRGENIUS_ADMIN_PASSWORD` as required by the backend configuration.
3. Run `mvn clean package`.
4. Run `mvn spring-boot:run`.
5. Open `http://localhost:8080`.

## Verification note
The frontend JavaScript was checked with `node --check`. Maven-based tests, package creation and Spring Boot startup were not run in the authoring environment because Maven was unavailable. No test or build success is claimed.

## API contract
The frontend calls the `/api/v1` endpoints documented in the project specification, including authentication, employees, departments, jobs, candidates, interviews, onboarding, salary structures, payroll, performance and analytics. Consult the source documentation for the complete endpoint matrix and role access rules.

## Payroll/TDS assumption
The database-design worked examples and its annual slab table are inconsistent. The specified slab rule is annual gross = monthly gross × 12; calculate tax using the documented annual slab rates, then divide annual tax by 12 with `HALF_UP`. The worked-example TDS values are retained separately and require confirmation. This delivery does not claim a verified payroll implementation.
