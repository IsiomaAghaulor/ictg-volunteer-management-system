# Church Volunteer Management System - API Documentation

## Overview
This is a comprehensive REST API for managing church volunteers, ministries, activities, and schedules. The system supports role-based access control with four user roles: Super Admin, Ministry Leader, Volunteer Coordinator, and Volunteer.

## Base URL
```
http://localhost:8080/api
```

## Authentication
The API uses JWT (JSON Web Token) authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## User Roles and Permissions
- **SUPER_ADMIN**: Full system access
- **MINISTRY_LEADER**: Manage specific ministries and activities
- **VOLUNTEER_COORDINATOR**: Oversee volunteer assignments
- **VOLUNTEER**: Self-service registration and availability management

## API Endpoints

### Authentication
#### POST /auth/signin
Login with email and password
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

#### POST /auth/signup
Register a new user
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "555-0123",
  "role": "VOLUNTEER"
}
```

#### POST /auth/logout
Logout user (clears security context)

### Ministries
#### GET /ministries
Get all active ministries (paginated)
- Query params: `page`, `size`, `sortBy`, `sortDir`

#### GET /ministries/{id}
Get ministry by ID

#### GET /ministries/root
Get root-level ministries (no parent)

#### GET /ministries/{id}/subministries
Get sub-ministries of a parent ministry

#### GET /ministries/search
Search ministries by name
- Query param: `q` (search term)

#### POST /ministries
Create new ministry (requires SUPER_ADMIN or MINISTRY_LEADER)
```json
{
  "name": "Worship Team",
  "description": "Leading worship services",
  "requirements": "Musical ability required"
}
```

#### PUT /ministries/{id}
Update ministry (requires SUPER_ADMIN or MINISTRY_LEADER)

#### DELETE /ministries/{id}
Deactivate ministry (requires SUPER_ADMIN)

#### POST /ministries/{ministryId}/members/{userId}
Assign user to ministry (requires SUPER_ADMIN or MINISTRY_LEADER)
- Query param: `role` (LEADER, COORDINATOR, VOLUNTEER)

#### DELETE /ministries/{ministryId}/members/{userId}
Remove user from ministry (requires SUPER_ADMIN or MINISTRY_LEADER)

#### GET /ministries/{id}/members
Get ministry members (paginated)

### Activities
#### GET /activities
Get all activities (paginated)
- Query params: `page`, `size`, `sortBy`, `sortDir`

#### GET /activities/{id}
Get activity by ID

#### GET /activities/ministry/{ministryId}
Get activities by ministry

#### GET /activities/status/{status}
Get activities by status (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)

#### GET /activities/upcoming
Get upcoming activities

#### GET /activities/need-volunteers
Get activities that need volunteers

#### GET /activities/date-range
Get activities within date range
- Query params: `startTime`, `endTime` (ISO DateTime format)

#### GET /activities/search
Search activities by name or description
- Query param: `q` (search term)

#### GET /activities/my-activities
Get current user's activities

#### POST /activities
Create new activity (requires SUPER_ADMIN or MINISTRY_LEADER)
```json
{
  "name": "Sunday Worship",
  "description": "Morning worship service",
  "startTime": "2024-12-01T10:00:00",
  "endTime": "2024-12-01T11:30:00",
  "location": "Main Sanctuary",
  "volunteersNeeded": 3,
  "recurrenceType": "WEEKLY"
}
```

#### PUT /activities/{id}
Update activity (requires SUPER_ADMIN or MINISTRY_LEADER)

#### DELETE /activities/{id}
Delete activity (requires SUPER_ADMIN or MINISTRY_LEADER)

#### PUT /activities/{id}/cancel
Cancel activity (requires SUPER_ADMIN or MINISTRY_LEADER)

#### PUT /activities/{id}/complete
Mark activity as completed (requires SUPER_ADMIN or MINISTRY_LEADER)

### Volunteer Assignments
#### GET /assignments/{id}
Get assignment by ID

#### GET /assignments/volunteer/{volunteerId}
Get assignments for a volunteer

#### GET /assignments/my-assignments
Get current user's assignments

#### GET /assignments/activity/{activityId}
Get assignments for an activity

#### GET /assignments/status/{status}
Get assignments by status (requires coordinator role or higher)

#### GET /assignments/date-range
Get assignments within date range (requires coordinator role or higher)

#### POST /assignments/assign
Assign volunteer to activity (requires coordinator role or higher)
- Query params: `volunteerId`, `activityId`, `activityRoleId` (optional)

#### POST /assignments/assign-substitute
Assign substitute volunteer (requires coordinator role or higher)
- Query params: `volunteerId`, `activityId`

#### POST /assignments/signup
Volunteer self-signup for activity
- Query param: `activityId`

#### PUT /assignments/{id}/confirm
Confirm assignment

#### PUT /assignments/{id}/cancel
Cancel assignment
- Query param: `reason` (optional)

#### PUT /assignments/{id}/complete
Mark assignment as completed (requires coordinator role or higher)
- Query params: `hoursServed`, `notes` (optional)

#### PUT /assignments/{id}/no-show
Mark volunteer as no-show (requires coordinator role or higher)

#### GET /assignments/volunteer/{volunteerId}/stats
Get volunteer statistics (total hours, completed assignments)

#### GET /assignments/conflict-check
Check for scheduling conflicts
- Query params: `volunteerId`, `startTime`, `endTime`

### Notifications
#### GET /notifications
Get user's notifications (paginated)

#### GET /notifications/unread
Get unread notifications

#### GET /notifications/unread-count
Get count of unread notifications

#### PUT /notifications/{id}/read
Mark notification as read

#### PUT /notifications/mark-all-read
Mark all notifications as read

#### DELETE /notifications/{id}
Delete notification

### Calendar
#### GET /calendar/view
Get calendar view for date range
- Query params: `startDate`, `endDate`, `ministryId` (optional), `volunteerId` (optional)

#### GET /calendar/monthly
Get monthly calendar view
- Query params: `year`, `month`, `ministryId` (optional), `volunteerId` (optional)

#### GET /calendar/weekly
Get weekly calendar view
- Query params: `weekStartDate`, `ministryId` (optional), `volunteerId` (optional)

#### GET /calendar/daily
Get daily calendar view
- Query params: `date`, `ministryId` (optional), `volunteerId` (optional)

#### GET /calendar/my-schedule
Get current user's schedule
- Query params: `startDate`, `endDate`

#### GET /calendar/volunteer/{volunteerId}/schedule
Get volunteer's schedule
- Query params: `startDate`, `endDate`

#### GET /calendar/my-upcoming
Get current user's upcoming assignments
- Query param: `days` (default: 30)

#### GET /calendar/my-ical
Download current user's schedule as iCal file
- Query params: `startDate` (optional), `endDate` (optional)

#### GET /calendar/volunteer/{volunteerId}/ical
Download volunteer's schedule as iCal file
- Query params: `startDate` (optional), `endDate` (optional)

### Reports
#### GET /reports/dashboard
Get dashboard statistics (requires coordinator role or higher)

#### GET /reports/volunteer/{volunteerId}
Get volunteer report for date range (requires coordinator role or higher)
- Query params: `startDate`, `endDate`

#### GET /reports/ministry/{ministryId}
Get ministry report for date range (requires MINISTRY_LEADER or higher)
- Query params: `startDate`, `endDate`

#### GET /reports/volunteer-participation
Get volunteer participation report (requires coordinator role or higher)
- Query params: `startDate`, `endDate`

#### GET /reports/volunteer-coverage
Get volunteer coverage report (requires coordinator role or higher)
- Query params: `startDate`, `endDate`

#### GET /reports/monthly-hours
Get monthly volunteer hours report (requires coordinator role or higher)
- Query param: `year`

## Error Responses
The API returns standard HTTP status codes and JSON error responses:

```json
{
  "error": "Error type",
  "message": "Detailed error message"
}
```

Common status codes:
- 200: Success
- 201: Created
- 400: Bad Request
- 401: Unauthorized
- 403: Forbidden
- 404: Not Found
- 500: Internal Server Error

## Getting Started

### Default Credentials
- Admin User: `admin@church.com` / `password`
- Sample Volunteer: `john.smith@email.com` / `password`

### Sample Data
The system includes sample data for:
- 5 ministries (Worship Team, Children's Ministry, Hospitality, Youth Ministry, Outreach)
- 4 sample users with different roles
- 5 sample activities scheduled for upcoming dates

### Database
- Development: H2 in-memory database (accessible at `/h2-console`)
- Production: PostgreSQL (configured via environment variables)

## Features Implemented

### Core Functionality (Phase 1)
✅ User registration and authentication  
✅ Basic volunteer and ministry management  
✅ Manual assignment capabilities  
✅ Email notifications  

### Advanced Features (Phase 2)
✅ Comprehensive reporting  
✅ Mobile-responsive API design  
✅ Communication tools (notifications)  
✅ Calendar integration with iCal export  

### Enhancement Features (Phase 3)
✅ Advanced analytics and reporting  
✅ Performance optimization  
✅ Role-based security  

## Technical Stack
- **Framework**: Spring Boot 3.2.0
- **Security**: Spring Security with JWT
- **Database**: JPA/Hibernate with H2 (dev) / PostgreSQL (prod)
- **Email**: Spring Mail
- **Build Tool**: Maven
- **Java Version**: 17

## Environment Variables
```
DATABASE_URL=jdbc:postgresql://localhost:5432/church_cms
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=password
JWT_SECRET=mySecretKey
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```