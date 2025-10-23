# RunRT - Complete API Testing Guide

## 📋 Table of Contents
1. [Setup & Prerequisites](#setup--prerequisites)
2. [Authentication Service](#1-authentication-service)
3. [Polls Service](#2-polls-service)
4. [Voting Service](#3-voting-service)
5. [Results Service (WebSocket)](#4-results-service-websocket)
6. [Testing Flow](#complete-testing-flow)

---

## Setup & Prerequisites

### Base URLs
- **API Gateway**: `http://localhost:8080`
- **Auth Service (Direct)**: `http://localhost:8081`
- **Polls Service (Direct)**: `http://localhost:8082`
- **Voting Service (Direct)**: `http://localhost:8083`
- **Results Service (Direct)**: `http://localhost:8084`

### Environment Setup in Postman

1. Create a new Environment called "RunRT"
2. Add these variables:
   - `base_url`: `http://localhost:8080`
   - `token`: (leave empty - will be set after login)
   - `poll_id`: (leave empty - will be set after creating poll)
   - `option_id`: (leave empty - will be set after creating poll)

---

## 1. Authentication Service

### 1.1 Register New User

**Endpoint**: `POST {{base_url}}/api/auth/register`

**Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Expected Response (200 OK)**:
```json
{
  "status": "registered"
}
```

**Expected Response (400 Bad Request)** - If username exists:
```json
{
  "error": "username_taken"
}
```

---

### 1.2 Login User

**Endpoint**: `POST {{base_url}}/api/auth/login`

**Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**Expected Response (200 OK)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Expected Response (401 Unauthorized)** - Invalid credentials:
```json
{
  "error": "invalid_credentials"
}
```

**📝 Note**: Copy the token from the response and save it to your environment variable `{{token}}` for subsequent requests.

---

## 2. Polls Service

### 2.1 Create New Poll

**Endpoint**: `POST {{base_url}}/api/polls`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Request Body**:
```json
{
  "title": "What's your favorite programming language?",
  "options": ["Java", "Python", "JavaScript", "Go", "Rust"]
}
```

**Expected Response (200 OK)**:
```json
{
  "id": "f67d591b-95e3-4859-98db-c41c2c8ce9ab",
  "title": "What's your favorite programming language?",
  "createdBy": "anonymous",
  "options": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "text": "Java"
    },
    {
      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "text": "Python"
    },
    {
      "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
      "text": "JavaScript"
    },
    {
      "id": "d4e5f6a7-b8c9-0123-def1-234567890123",
      "text": "Go"
    },
    {
      "id": "e5f6a7b8-c9d0-1234-ef12-345678901234",
      "text": "Rust"
    }
  ]
}
```

**📝 Note**: Save the `id` to `{{poll_id}}` and one of the option `id` values to `{{option_id}}` in your environment.

---

### 2.2 Get All Polls

**Endpoint**: `GET {{base_url}}/api/polls`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Expected Response (200 OK)**:
```json
[
  {
    "id": "f67d591b-95e3-4859-98db-c41c2c8ce9ab",
    "title": "What's your favorite programming language?",
    "createdBy": "anonymous",
    "options": [
      {
        "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "text": "Java"
      },
      {
        "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
        "text": "Python"
      }
    ]
  },
  {
    "id": "0926877e-9296-4992-9c6f-af68eb98d59e",
    "title": "Best framework for microservices?",
    "createdBy": "anonymous",
    "options": [...]
  }
]
```

---

### 2.3 Get Poll by ID

**Endpoint**: `GET {{base_url}}/api/polls/{{poll_id}}`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Expected Response (200 OK)**:
```json
{
  "id": "f67d591b-95e3-4859-98db-c41c2c8ce9ab",
  "title": "What's your favorite programming language?",
  "createdBy": "anonymous",
  "options": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "text": "Java"
    },
    {
      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "text": "Python"
    }
  ]
}
```

**Expected Response (404 Not Found)** - If poll doesn't exist:
```json
{
  "timestamp": "2025-10-23T13:08:00.464+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/polls/invalid-id"
}
```

---

### 2.4 Update Poll

**Endpoint**: `PUT {{base_url}}/api/polls/{{poll_id}}`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Request Body**:
```json
{
  "title": "What's your favorite programming language? (Updated)"
}
```

**Expected Response (200 OK)**:
```json
{
  "id": "f67d591b-95e3-4859-98db-c41c2c8ce9ab",
  "title": "What's your favorite programming language? (Updated)",
  "createdBy": "anonymous",
  "options": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "text": "Java"
    },
    {
      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "text": "Python"
    }
  ]
}
```

**Expected Response (404 Not Found)** - If poll doesn't exist:
```json
{
  "timestamp": "2025-10-23T13:08:00.464+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/polls/invalid-id"
}
```

---

## 3. Voting Service

### 3.1 Submit a Vote

**Endpoint**: `POST {{base_url}}/votes`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Request Body**:
```json
{
  "pollId": "f67d591b-95e3-4859-98db-c41c2c8ce9ab",
  "optionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**Expected Response (202 Accepted)**:
```json
{
  "status": "queued"
}
```

**📝 Note**: 
- Votes are processed asynchronously via Kafka
- The 202 status means the vote was accepted and queued for processing
- Results will be available via the Results Service WebSocket

---

## 4. Results Service (WebSocket)

The Results Service uses WebSocket for real-time poll results. Testing WebSockets requires special tools.

### 4.1 Connect to WebSocket

**WebSocket URL**: `ws://localhost:8084/ws`

**Testing Options**:

#### Option A: Using Browser Console
```javascript
// Open browser console (F12) and run:
const socket = new WebSocket('ws://localhost:8084/ws');

socket.onopen = () => {
  console.log('Connected to Results Service');
  // Subscribe to a specific poll
  socket.send(JSON.stringify({
    type: 'SUBSCRIBE',
    pollId: 'f67d591b-95e3-4859-98db-c41c2c8ce9ab'
  }));
};

socket.onmessage = (event) => {
  console.log('Received:', JSON.parse(event.data));
};

socket.onerror = (error) => {
  console.error('WebSocket error:', error);
};
```

#### Option B: Using Postman WebSocket Client
1. Create a new WebSocket request
2. Enter URL: `ws://localhost:8084/ws`
3. Click "Connect"
4. Send messages to subscribe to poll updates

**Expected Real-Time Updates**:
```json
{
  "pollId": "f67d591b-95e3-4859-98db-c41c2c8ce9ab",
  "results": {
    "a1b2c3d4-e5f6-7890-abcd-ef1234567890": 15,
    "b2c3d4e5-f6a7-8901-bcde-f12345678901": 23,
    "c3d4e5f6-a7b8-9012-cdef-123456789012": 8,
    "d4e5f6a7-b8c9-0123-def1-234567890123": 12,
    "e5f6a7b8-c9d0-1234-ef12-345678901234": 5
  },
  "totalVotes": 63,
  "timestamp": "2025-10-23T13:15:30.123Z"
}
```

---

## Complete Testing Flow

### Scenario: Create Poll → Vote → View Results

#### Step 1: Register & Login
```bash
1. POST /api/auth/register → Register new user
2. POST /api/auth/login → Get JWT token
3. Save token to environment variable
```

#### Step 2: Create a Poll
```bash
4. POST /api/polls → Create poll with multiple options
5. Save poll_id and option_id from response
```

#### Step 3: Verify Poll Creation
```bash
6. GET /api/polls → List all polls (should include new poll)
7. GET /api/polls/{poll_id} → Get specific poll details
```

#### Step 4: Submit Votes
```bash
8. POST /votes → Submit vote for option 1
9. POST /votes → Submit vote for option 2 (multiple votes)
10. POST /votes → Submit vote for option 3
```

#### Step 5: View Real-Time Results
```bash
11. Connect to WebSocket: ws://localhost:8084/ws
12. Subscribe to poll updates
13. Observe real-time vote counts updating
```

#### Step 6: Update Poll (Optional)
```bash
14. PUT /api/polls/{poll_id} → Update poll title
15. GET /api/polls/{poll_id} → Verify update
```

---

## Common Issues & Troubleshooting

### Issue 1: 401 Unauthorized
**Cause**: Missing or invalid JWT token
**Solution**: 
- Ensure you've logged in and copied the token
- Check that `Authorization: Bearer {{token}}` header is set
- Token might be expired (generate new one by logging in again)

### Issue 2: 404 Not Found on Poll Endpoints
**Cause**: Invalid poll ID or poll doesn't exist
**Solution**:
- Verify the poll exists: `GET /api/polls`
- Check you're using the correct UUID format
- Ensure poll was created successfully

### Issue 3: 500 Internal Server Error
**Cause**: Various server-side issues
**Solution**:
- Check service logs: `docker logs runrt-polls-service-1`
- Verify all services are running: `docker ps`
- Check database connectivity

### Issue 4: CORS Errors
**Cause**: Cross-origin request blocked
**Solution**:
- Controllers have `@CrossOrigin(origins = "*")` configured
- Try accessing via API Gateway (port 8080) instead of direct service ports

### Issue 5: Connection Refused
**Cause**: Service not running or wrong port
**Solution**:
- Check services: `docker ps`
- Restart services: `docker compose up -d`
- Verify port mappings in docker-compose.yml

---

## Postman Collection Structure

Recommended folder structure for your Postman collection:

```
📁 RunRT API
  📁 1. Authentication
    ├─ Register User
    └─ Login User
  📁 2. Poll Management
    ├─ Create Poll
    ├─ List All Polls
    ├─ Get Poll by ID
    └─ Update Poll
  📁 3. Voting
    └─ Submit Vote
  📁 4. Results (WebSocket)
    └─ Connect to Results Stream
```

---

## Environment Variables Reference

| Variable | Description | Example |
|----------|-------------|---------|
| `base_url` | API Gateway base URL | `http://localhost:8080` |
| `token` | JWT authentication token | `eyJhbGciOiJIUzI1NiIsInR5cCI6...` |
| `poll_id` | UUID of created poll | `f67d591b-95e3-4859-98db-c41c2c8ce9ab` |
| `option_id` | UUID of poll option | `a1b2c3d4-e5f6-7890-abcd-ef1234567890` |
| `username` | Test user username | `testuser` |
| `password` | Test user password | `password123` |

---

## Quick Test Commands (cURL)

### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

### Create Poll
```bash
curl -X POST http://localhost:8080/api/polls \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"title":"Test Poll","options":["Option 1","Option 2","Option 3"]}'
```

### List Polls
```bash
curl -X GET http://localhost:8080/api/polls \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Submit Vote
```bash
curl -X POST http://localhost:8080/votes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"pollId":"POLL_ID_HERE","optionId":"OPTION_ID_HERE"}'
```

---

## Notes

- All timestamps are in UTC ISO 8601 format
- UUIDs are version 4 (random)
- JWT tokens expire after 24 hours (configurable)
- Votes are processed asynchronously
- WebSocket connections support multiple simultaneous subscribers
- Redis is used for caching real-time results

---

**Last Updated**: October 23, 2025
**API Version**: 0.1.0-SNAPSHOT
