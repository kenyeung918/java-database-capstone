# User Story Admin

**Title:**
_As an admin, I want to manage doctor profiles and system access, so that I can maintain operational control and monitor usage._

**Acceptance Criteria:**
1. Log into the portal with your username and password
2. log out of the protal to protect system access
3. Add doctors to the portal
4. Delete doctor's profile from the portal]
5. Run a stored procedure in MySQL CLI to get the number of appointments per month and track usage statistics

**Priority:** High
**Story Points:** 10
**Notes:**
- Ensure proper validation and audit logging for profile changes.
- Stored procedure must handle edge cases like months with zero appointments.

**Title:**
_As an admin, I want to reset doctor passwords, so that I can assist with account recovery._

**Acceptance Criteria:**
1. Admin can search for a doctor by name or email.
2. Admin can trigger a password reset link or manually set a new password.
3. System logs the reset action for audit purposes.

**Priority:** Medium
**Story Points:** 5
**Notes:**
- Ensure secure handling of credentials and confirmation prompts.

**Title:**
_As an admin, I want to view system-wide appointment statistics, so that I can monitor clinic performance._
**Acceptance Criteria:**
- Admin can access a dashboard showing total appointments per doctor and per month.
- Data is fetched from both MySQL and MongoDB.
- Charts and summaries are updated in real time or on demand.
**Priority:** High
**Story Points:** 8
**Notes:**
- Consider caching or pagination for large datasets.
**Title:**
As a doctor, I want to upload medical notes after appointments, so that I can maintain patient records.
**Acceptance Criteria:**
- Doctor can select an appointment and attach notes.
- Notes are stored securely in MongoDB.
- Patients can view notes if access is granted.
**Priority:** High
**Story Points:** 8
**Notes:**
- Include versioning or timestamping for edits.

**Title:**
_As a doctor, I want to receive notifications for new bookings, so that I can stay informed in real time._
**Acceptance Criteria:**
- Doctor receives email or in-app alerts when a patient books an appointment.
- Notification includes patient name, time, and reason for visit.
- Alerts respect doctor’s notification preferences.
**Priority:** Medium
**Story Points:** 5
**Notes:**
- Consider integration with mobile push notifications.

# User Story Doctor
**Title:**
_As a doctor, I want to manage my availability and view patient details, so that I can stay organized and prepared for consultations._

**Acceptance Criteria:**
1. Log into the portal to manage your appointments
2. log out of the protal to protect system access
3. View my appoinment calendar to stay organized
4. Mark your unavailability to inform patints only the available slots
5. Update you profile with specialization and contact information so that patients have up-to-date information
6. View the patient details for upcoming appointments so that I can be prepared

**Priority:** High
**Story Points:** 10
**Notes:**
- Calendar should reflect real-time availability.
- Profile updates should trigger notifications to patients if relevant


# User Story Patient

**Title:**
_As a patient, I want to explore doctors and manage my bookings, so that I can schedule consultations conveniently and securely._

**Acceptance Criteria:**
1. View a list of doctors without logging in to explore options before registering
2. Sign up using your email and password to book appointments
3. Log into the portal to manage your bookings
4. Log out of the portal to secure your account]
5. Log in and book an hour-long appointment to consult with a doctor
6. View my upcoming appointments so that I can prepare accordingly

**Priority:** [High]
**Story Points:** [10]
**Notes:**
- Booking system should prevent double-booking and respect doctor availability.
- Consider adding email reminders for upcoming appointments.

**Title:**
_As a patient, I want to cancel or reschedule appointments, so that I can manage changes in my availability._

**Acceptance Criteria:**
- Patient can view upcoming appointments.
- Patient can cancel or reschedule with available time slots.
- Doctor is notified of changes.
**Priority:** High
**Story Points:** 6
**Notes:**
- Enforce cancellation policies (e.g., minimum notice period).

**Title:**
_As a patient, I want to rate and review doctors after appointments, so that I can share feedback with others._
**Acceptance Criteria:**
- Patient can submit a rating and optional comment post-appointment.
- Reviews are visible to other patients.
- Admin can moderate inappropriate content.
**Priority:** Medium
**Story Points:** 5
**Notes:**
- Consider anonymizing reviews and adding filters.

