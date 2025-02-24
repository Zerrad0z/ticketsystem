# IT Support Ticket System API Documentation

This document provides comprehensive information about the REST API endpoints available in the IT Support Ticket System.

## Authentication

The API uses JWT (JSON Web Token) for authentication. Most endpoints require a valid authentication token.

### Headers

- `Authorization`: Bearer token for authentication
- `User-Id`: The ID of the current user (required for many endpoints)

## Base URL

All API URLs referenced in the documentation have the base:

```
http://[hostname]:8080/api
```

---

## Authentication Endpoints

### Login

Authenticates a user and returns a JWT token.

- **URL**: `/auth/login`
- **Method**: `POST`
- **Auth Required**: No
- **Request Body**:
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: User information with authorization header containing token
- **Error Response**:
  - **Code**: `401 UNAUTHORIZED`

### Register

Registers a new user.

- **URL**: `/auth/register`
- **Method**: `POST`
- **Auth Required**: No
- **Request Body**:
  ```json
  {
    "username": "string",
    "password": "string",
    "role": "ROLE_EMPLOYEE" or "ROLE_IT_SUPPORT"
  }
  ```
- **Success Response**:
  - **Code**: `201 CREATED`
  - **Content**: User information
- **Error Response**:
  - **Code**: `400 BAD REQUEST` - If username already exists

### Logout

Logs out the current user.

- **URL**: `/auth/logout`
- **Method**: `POST`
- **Auth Required**: Yes
- **Success Response**:
  - **Code**: `200 OK`

---

## Ticket Endpoints

### Create Ticket

Creates a new support ticket.

- **URL**: `/tickets`
- **Method**: `POST`
- **Auth Required**: Yes
- **Headers**: `User-Id: {userId}`
- **Request Body**:
  ```json
  {
    "title": "string",
    "description": "string",
    "priority": "LOW", "MEDIUM", or "HIGH",
    "category": "NETWORK", "HARDWARE", "SOFTWARE", or "OTHER"
  }
  ```
- **Success Response**:
  - **Code**: `201 CREATED`
  - **Content**: Created ticket information
- **Error Response**:
  - **Code**: `400 BAD REQUEST` - If ticket data is invalid

### Update Ticket Status

Updates the status of a ticket (IT Support only).

- **URL**: `/tickets/{ticketId}/status?newStatus={status}`
- **Method**: `PUT`
- **Auth Required**: Yes
- **Headers**: `User-Id: {userId}`
- **URL Parameters**:
  - `ticketId=[long]`: ID of the ticket
  - `newStatus=[enum]`: New status (NEW, IN_PROGRESS, RESOLVED)
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: Updated ticket information
- **Error Response**:
  - **Code**: `404 NOT FOUND` - If ticket not found
  - **Code**: `403 FORBIDDEN` - If not IT Support

### Add Comment to Ticket

Adds a comment to a ticket (IT Support only).

- **URL**: `/tickets/{ticketId}/comments`
- **Method**: `POST`
- **Auth Required**: Yes
- **Headers**: `User-Id: {userId}`
- **URL Parameters**:
  - `ticketId=[long]`: ID of the ticket
- **Request Body**: Comment content as plain text
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: Updated ticket information
- **Error Response**:
  - **Code**: `404 NOT FOUND` - If ticket not found
  - **Code**: `403 FORBIDDEN` - If not IT Support
  - **Code**: `400 BAD REQUEST` - If comment content is empty

### Get User's Tickets

Retrieves tickets created by the authenticated user.

- **URL**: `/tickets/user`
- **Method**: `GET`
- **Auth Required**: Yes
- **Headers**: `User-Id: {userId}`
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: Array of tickets

### Get All Tickets

Retrieves all tickets in the system (IT Support only).

- **URL**: `/tickets`
- **Method**: `GET`
- **Auth Required**: Yes
- **Headers**: `User-Id: {userId}`
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: Array of tickets
- **Error Response**:
  - **Code**: `403 FORBIDDEN` - If not IT Support

### Get Tickets by Status

Retrieves tickets filtered by status.

- **URL**: `/tickets/status/{status}`
- **Method**: `GET`
- **Auth Required**: Yes
- **Headers**: `User-Id: {userId}`
- **URL Parameters**:
  - `status=[enum]`: Status to filter by (NEW, IN_PROGRESS, RESOLVED)
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: Array of tickets

### Get Ticket by ID

Retrieves a specific ticket by ID.

- **URL**: `/tickets/{ticketId}`
- **Method**: `GET`
- **Auth Required**: Yes
- **Headers**: `User-Id: {userId}`
- **URL Parameters**:
  - `ticketId=[long]`: ID of the ticket
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: Ticket information
- **Error Response**:
  - **Code**: `404 NOT FOUND` - If ticket not found
  - **Code**: `403 FORBIDDEN` - If not owner or IT Support

### Get Audit Logs

Retrieves audit logs for all tickets (IT Support only).

- **URL**: `/tickets/audit-logs`
- **Method**: `GET`
- **Auth Required**: Yes
- **Headers**: `User-Id: {userId}`
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: Array of audit logs
- **Error Response**:
  - **Code**: `403 FORBIDDEN` - If not IT Support

---

## User Endpoints

### Get All Users

Retrieves all users in the system.

- **URL**: `/users`
- **Method**: `GET`
- **Auth Required**: Yes
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: Array of users

### Get User by ID

Retrieves a specific user by ID.

- **URL**: `/users/{id}`
- **Method**: `GET`
- **Auth Required**: Yes
- **URL Parameters**:
  - `id=[long]`: ID of the user
- **Success Response**:
  - **Code**: `200 OK`
  - **Content**: User information
- **Error Response**:
  - **Code**: `404 NOT FOUND` - If user not found

### Delete User

Deletes a user by ID.

- **URL**: `/users/{id}`
- **Method**: `DELETE`
- **Auth Required**: Yes
- **URL Parameters**:
  - `id=[long]`: ID of the user
- **Success Response**:
  - **Code**: `204 NO CONTENT`
- **Error Response**:
  - **Code**: `404 NOT FOUND` - If user not found

---

## Data Models

### Ticket Model

```json
{
  "id": "long",
  "title": "string",
  "description": "string",
  "status": "NEW | IN_PROGRESS | RESOLVED",
  "priority": "LOW | MEDIUM | HIGH",
  "category": "NETWORK | HARDWARE | SOFTWARE | OTHER",
  "createdDate": "datetime",
  "lastUpdated": "datetime",
  "createdBy": {
    "id": "long",
    "username": "string",
    "role": "string"
  },
  "comments": [
    {
      "id": "long",
      "content": "string",
      "createdDate": "datetime",
      "createdBy": {
        "id": "long",
        "username": "string"
      }
    }
  ]
}
```

### User Model

```json
{
  "id": "long",
  "username": "string",
  "role": "ROLE_EMPLOYEE | ROLE_IT_SUPPORT"
}
```

### Audit Log Model

```json
{
  "id": "long",
  "action": "string",
  "oldValue": "string",
  "newValue": "string",
  "createdDate": "datetime",
  "performedBy": {
    "id": "long",
    "username": "string"
  },
  "ticketId": "long"
}
```

---

## HTTP Status Codes

The API uses the following HTTP status codes:

- `200 OK` - Request succeeded
- `201 CREATED` - Resource created successfully
- `204 NO CONTENT` - Request succeeded but no content returned
- `400 BAD REQUEST` - Invalid request data
- `401 UNAUTHORIZED` - Authentication failed
- `403 FORBIDDEN` - Permission denied
- `404 NOT FOUND` - Resource not found
- `500 INTERNAL SERVER ERROR` - Server error

---

## API Security

- Authentication is handled via JWT tokens
- Access control is implemented using Spring Security
- Role-based authorization ensures that certain endpoints are only accessible to IT Support users
- Cross-Origin Resource Sharing (CORS) is configured to allow requests from all origins
