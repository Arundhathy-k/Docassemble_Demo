# API Documentation

## Base URL
```
http://localhost:8080/api/interview
```

## Endpoints

### 1. Start Interview Session
Creates a new interview session with Docassemble and returns the interview URL.

**Endpoint**: `POST /start`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "userName": "John Doe",
  "userEmail": "john.doe@example.com",
  "interviewName": "interview_name",
  "variables": {
    "custom_var": "value"
  }
}
```

**Request Parameters**:
- `userName` (required): Full name of the person signing the document
- `userEmail` (required): Email address of the person
- `interviewName` (optional): Name of the interview to use. If not provided, uses default from config
- `variables` (optional): Additional variables to pass to the interview

**Response** (200 OK):
```json
{
  "success": true,
  "sessionId": "abc123xyz789",
  "interviewUrl": "http://localhost:8000/interview?session=abc123xyz789",
  "message": "Interview session created successfully"
}
```

**Response** (400 Bad Request):
```json
{
  "success": false,
  "error": "User name is required",
  "timestamp": 1707073200000
}
```

**Response** (500 Internal Server Error):
```json
{
  "success": false,
  "error": "Failed to create interview session: [error details]",
  "timestamp": 1707073200000
}
```

**Example Request**:
```bash
curl -X POST http://localhost:8080/api/interview/start \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "Jane Smith",
    "userEmail": "jane.smith@example.com"
  }'
```

---

### 2. Get Signed Document (HTML)
Retrieves the signed document as HTML. Returns the document ready for display or printing.

**Endpoint**: `GET /{sessionId}/document`

**Path Parameters**:
- `sessionId` (required): The session ID returned from the start endpoint

**Response Headers**:
```
Content-Type: text/html
Content-Disposition: inline; filename="document.html"
```

**Response** (200 OK):
HTML document content with signatures

**Response** (500 Internal Server Error):
```json
{
  "success": false,
  "error": "Failed to retrieve signed document: [error details]",
  "timestamp": 1707073200000
}
```

**Example Request**:
```bash
curl http://localhost:8080/api/interview/abc123xyz789/document \
  -o signed_document.html
```

---

### 3. Get Signed Document (JSON)
Retrieves the signed document as a JSON response containing the HTML content.

**Endpoint**: `GET /{sessionId}/document/json`

**Path Parameters**:
- `sessionId` (required): The session ID returned from the start endpoint

**Response** (200 OK):
```json
{
  "success": true,
  "sessionId": "abc123xyz789",
  "document": {
    "id": "doc-uuid-123",
    "sessionId": "abc123xyz789",
    "status": "COMPLETED",
    "signedBy": "Jane Smith",
    "htmlContent": "<html>...</html>"
  }
}
```

**Response** (500 Internal Server Error):
```json
{
  "success": false,
  "error": "Failed to retrieve signed document: [error details]",
  "timestamp": 1707073200000
}
```

**Example Request**:
```bash
curl http://localhost:8080/api/interview/abc123xyz789/document/json \
  -H "Content-Type: application/json"
```

---

### 4. Get Session Status
Retrieves the current status of an interview session.

**Endpoint**: `GET /{sessionId}/status`

**Path Parameters**:
- `sessionId` (required): The session ID returned from the start endpoint

**Response** (200 OK):
```json
{
  "success": true,
  "sessionId": "abc123xyz789",
  "status": {
    "status": "complete",
    "user": "Jane Smith",
    "completion_time": "2026-02-04T10:30:00Z",
    "variables": {
      "var1": "value1",
      "var2": "value2"
    }
  }
}
```

**Response** (500 Internal Server Error):
```json
{
  "success": false,
  "error": "Failed to get session status: [error details]",
  "timestamp": 1707073200000
}
```

**Example Request**:
```bash
curl http://localhost:8080/api/interview/abc123xyz789/status
```

---

### 5. Complete Interview Session
Closes and completes an interview session.

**Endpoint**: `POST /{sessionId}/complete`

**Path Parameters**:
- `sessionId` (required): The session ID returned from the start endpoint

**Response** (200 OK):
```json
{
  "success": true,
  "sessionId": "abc123xyz789",
  "message": "Interview session completed successfully"
}
```

**Response** (500 Internal Server Error):
```json
{
  "success": false,
  "error": "Failed to complete session: [error details]",
  "timestamp": 1707073200000
}
```

**Example Request**:
```bash
curl -X POST http://localhost:8080/api/interview/abc123xyz789/complete \
  -H "Content-Type: application/json"
```

---

### 6. Health Check
Checks if the interview service is running and accessible.

**Endpoint**: `GET /health`

**Response** (200 OK):
```json
{
  "status": "UP",
  "service": "Interview Service",
  "timestamp": 1707073200000
}
```

**Example Request**:
```bash
curl http://localhost:8080/api/interview/health
```

---

## Complete Workflow Example

### Step 1: Initiate Interview
```bash
# Create a new interview session
curl -X POST http://localhost:8080/api/interview/start \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "John Doe",
    "userEmail": "john@example.com"
  }'

# Response:
# {
#   "success": true,
#   "sessionId": "abc123xyz",
#   "interviewUrl": "http://localhost:8000/interview?session=abc123xyz"
# }
```

### Step 2: User Completes Interview
- Open the `interviewUrl` in a browser
- Fill out the form
- Sign the document
- Submit

### Step 3: Check Session Status
```bash
# Check if the session is complete
curl http://localhost:8080/api/interview/abc123xyz/status

# Response shows if the interview is completed
```

### Step 4: Retrieve Signed Document
```bash
# Get the signed HTML document
curl http://localhost:8080/api/interview/abc123xyz/document \
  -o final_document.html

# Or get as JSON
curl http://localhost:8080/api/interview/abc123xyz/document/json
```

### Step 5: Complete Session (Optional)
```bash
# Close the session when done
curl -X POST http://localhost:8080/api/interview/abc123xyz/complete
```

---

## Error Codes

| Status Code | Meaning |
|-------------|---------|
| 200 | Request successful |
| 201 | Resource created |
| 400 | Bad request (validation error) |
| 404 | Resource not found |
| 500 | Internal server error |

---

## Common Error Messages

| Error | Cause | Solution |
|-------|-------|----------|
| "User name is required" | Missing userName field | Provide userName in request |
| "Failed to create interview session" | Docassemble unavailable or API key invalid | Check Docassemble server and API key |
| "Failed to retrieve signed document" | Session not complete or invalid ID | Ensure session is completed |
| "Server error" | Unexpected exception | Check server logs |

---

## Request/Response Examples

### JavaScript (Fetch API)

```javascript
// Start interview
fetch('/api/interview/start', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    userName: 'John Doe',
    userEmail: 'john@example.com'
  })
})
.then(response => response.json())
.then(data => {
  if (data.success) {
    console.log('Session ID:', data.sessionId);
    window.open(data.interviewUrl);
  }
});
```

### Python (requests library)

```python
import requests
import json

# Start interview
url = 'http://localhost:8080/api/interview/start'
payload = {
    'userName': 'John Doe',
    'userEmail': 'john@example.com'
}

response = requests.post(url, json=payload)
data = response.json()

if data['success']:
    session_id = data['sessionId']
    print(f"Session ID: {session_id}")
    
    # Get signed document
    doc_url = f'http://localhost:8080/api/interview/{session_id}/document/json'
    doc_response = requests.get(doc_url)
    document = doc_response.json()
```

### cURL

```bash
#!/bin/bash

# Start interview
RESPONSE=$(curl -X POST http://localhost:8080/api/interview/start \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "John Doe",
    "userEmail": "john@example.com"
  }')

# Extract session ID
SESSION_ID=$(echo $RESPONSE | grep -o '"sessionId":"[^"]*' | cut -d'"' -f4)

echo "Session ID: $SESSION_ID"

# Get document (after interview is complete)
curl http://localhost:8080/api/interview/$SESSION_ID/document \
  -o signed_document.html
```

---

## Rate Limiting & Performance

- No built-in rate limiting (can be added via Spring Boot Actuator)
- Recommended to add pagination for large document retrieval
- Consider caching session status if checked frequently
- Monitor Docassemble server performance

---

## Security

- API keys are sent in Authorization header (in production use HTTPS)
- Validate user input on both client and server
- Implement CORS configuration for cross-origin requests
- Consider adding API authentication/authorization layer
- Store sensitive data (API keys) in environment variables

---

**Last Updated**: February 4, 2026
**API Version**: 1.0
