# Visual Guide & Diagrams

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                          USER BROWSER                           │
│                                                                 │
│    ┌────────────────────────────────────────────────────────┐  │
│    │              Web UI (HTML/CSS/JS)                      │  │
│    │  • Home page with form                                 │  │
│    │  • Document retrieval page                             │  │
│    │  • Loading states & messages                           │  │
│    └────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                          ↑ HTTP ↓
┌─────────────────────────────────────────────────────────────────┐
│              SPRING BOOT APPLICATION (Port 8080)                │
├───────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ Controllers                                              │ │
│  │  • HomeController - Serves pages                         │ │
│  │  • InterviewController - REST API                        │ │
│  │                                                          │ │
│  │ API Endpoints:                                           │ │
│  │  POST   /api/interview/start                             │ │
│  │  GET    /api/interview/{id}/document                     │ │
│  │  GET    /api/interview/{id}/document/json                │ │
│  │  GET    /api/interview/{id}/status                       │ │
│  │  POST   /api/interview/{id}/complete                     │ │
│  │  GET    /api/interview/health                            │ │
│  └──────────────────────────────────────────────────────────┘ │
│                          ↓                                      │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ Services                                                 │ │
│  │  • DocassembleService - API client                       │ │
│  │  • DocassembleRestTemplateService - Alternative client   │ │
│  │  • Exception Handling                                    │ │
│  └──────────────────────────────────────────────────────────┘ │
│                          ↓                                      │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ Configuration                                            │ │
│  │  • DocassembleConfig - Loads properties                  │ │
│  │  • JacksonConfig - JSON serialization                    │ │
│  │  • RestTemplateConfig - HTTP client                      │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                          ↑ HTTP ↓
┌─────────────────────────────────────────────────────────────────┐
│           DOCASSEMBLE SERVER (Port 8000)                        │
│                                                                 │
│    • Interview sessions                                         │
│    • Document generation                                        │
│    • Digital signatures                                         │
│    • API endpoints                                              │
│    • Admin panel                                                │
│                                                                 │
│    ┌────────────────┐         ┌──────────────────┐            │
│    │  PostgreSQL DB │         │  Redis Cache     │            │
│    │  (Port 5432)   │         │  (Port 6379)     │            │
│    └────────────────┘         └──────────────────┘            │
└─────────────────────────────────────────────────────────────────┘
```

## Request/Response Flow

```
1. USER CREATES SESSION
   ┌─────────────┐
   │   Browser   │
   │ Fills Form  │
   │ Clicks Send │
   └──────┬──────┘
          │
          │ POST /api/interview/start
          │ {userName, userEmail}
          ↓
   ┌────────────────────────┐
   │  Spring Boot Server    │
   │ HomeController.start() │
   └──────┬─────────────────┘
          │
          │ Calls DocassembleService
          ↓
   ┌────────────────────────┐
   │  HTTP Request to       │
   │  Docassemble API       │
   │  /api/session          │
   └──────┬─────────────────┘
          │
          │ Docassemble creates session
          ↓
   ┌────────────────────────┐
   │  HTTP Response         │
   │  sessionId, URL        │
   └──────┬─────────────────┘
          │
          │ Return to Browser
          ↓
   ┌─────────────────────────┐
   │   Browser receives:     │
   │  {success: true,        │
   │   sessionId: "abc123",  │
   │   interviewUrl: "..."}  │
   └─────────────────────────┘

2. USER COMPLETES INTERVIEW
   ┌──────────────────────┐
   │   User opens URL     │
   │ in interview browser │
   │  Fills & signs       │
   │   Submits form       │
   └──────┬───────────────┘
          │
          │ Docassemble processes
          │ Creates signed document
          │
          ↓
   ┌──────────────────────┐
   │ Interview Complete   │
   │ Document Ready       │
   └──────┬───────────────┘

3. USER RETRIEVES DOCUMENT
   ┌─────────────┐
   │   Browser   │
   │ GET request │
   │  /document  │
   └──────┬──────┘
          │
          │ GET /api/interview/{sessionId}/document
          ↓
   ┌────────────────────────┐
   │  Spring Boot Server    │
   │ InterviewController    │
   │ getSignedDocument()    │
   └──────┬─────────────────┘
          │
          │ Calls DocassembleService
          ↓
   ┌────────────────────────┐
   │  HTTP Request to       │
   │  Docassemble API       │
   │  /api/session/{id}/get │
   └──────┬─────────────────┘
          │
          │ Docassemble returns HTML
          ↓
   ┌────────────────────────┐
   │  HTTP Response         │
   │  Signed HTML content   │
   └──────┬─────────────────┘
          │
          │ Return HTML to Browser
          ↓
   ┌──────────────────────┐
   │   Browser displays   │
   │  Signed Document     │
   │   (can print/save)   │
   └──────────────────────┘
```

## File Organization Diagram

```
demo/
│
├── 📋 Configuration Files
│   ├── pom.xml (Maven dependencies - UPDATED)
│   ├── docker-compose.yml (Docker services)
│   ├── application.properties (App configuration)
│   └── env.template (Environment variables)
│
├── 📚 Documentation (6 files)
│   ├── README.md (Main guide)
│   ├── QUICK_START.md (5-min setup)
│   ├── SETUP_GUIDE.md (Detailed setup)
│   ├── API_DOCUMENTATION.md (API reference)
│   ├── PROJECT_SUMMARY.md (Technical overview)
│   ├── GETTING_STARTED.md (Checklist)
│   └── COMPLETE_INSTALLATION_SUMMARY.md (This summary)
│
└── src/main/
    │
    ├── java/com/kovan/
    │   │
    │   ├── 🔧 config/ (3 files)
    │   │   ├── DocassembleConfig.java
    │   │   ├── JacksonConfig.java
    │   │   └── RestTemplateConfig.java
    │   │
    │   ├── 🌐 controller/ (2 files)
    │   │   ├── HomeController.java
    │   │   └── InterviewController.java
    │   │
    │   ├── 🛡️ exception/ (1 file)
    │   │   └── GlobalExceptionHandler.java
    │   │
    │   ├── 📦 model/ (4 files)
    │   │   ├── InterviewRequest.java
    │   │   ├── InterviewSession.java
    │   │   ├── SignatureDocument.java
    │   │   └── ApiResponse.java
    │   │
    │   ├── 🔌 service/ (2 files)
    │   │   ├── DocassembleService.java
    │   │   └── DocassembleRestTemplateService.java
    │   │
    │   ├── 🛠️ util/ (2 files)
    │   │   ├── DocumentUtil.java
    │   │   └── ApiResponseUtil.java
    │   │
    │   └── 🚀 DocassembleDemoApplication.java
    │
    └── resources/
        │
        ├── application.properties
        │
        └── templates/
            ├── index.html (Home page)
            └── retrieve-document.html (Document page)
```

## Workflow Diagram

```
START
  │
  ├─→ User Visits http://localhost:8080
  │   │
  │   └─→ HomeController Serves index.html
  │       │
  │       └─→ Browser displays form
  │
  ├─→ User Fills Form
  │   │
  │   └─→ Enters: Name, Email
  │
  ├─→ User Clicks "Start Interview"
  │   │
  │   └─→ JavaScript sends POST /api/interview/start
  │       │
  │       ├─→ InterviewController.startInterview()
  │       │
  │       ├─→ DocassembleService.createInterviewSession()
  │       │
  │       ├─→ HTTP call to Docassemble /api/session
  │       │
  │       └─→ Receives: sessionId, interviewUrl
  │
  ├─→ Browser Shows Session ID
  │   │
  │   └─→ Provides Interview Link
  │
  ├─→ User Clicks Interview Link
  │   │
  │   └─→ Opens Docassemble interview page
  │
  ├─→ User Fills Interview Form
  │   │
  │   └─→ Enters details, signs document
  │
  ├─→ User Submits Form
  │   │
  │   └─→ Docassemble processes & creates document
  │
  ├─→ User Retrieves Document
  │   │
  │   └─→ GET /api/interview/{sessionId}/document
  │       │
  │       ├─→ InterviewController.getSignedDocument()
  │       │
  │       ├─→ DocassembleService.getSignedDocument()
  │       │
  │       ├─→ HTTP call to Docassemble
  │       │
  │       └─→ Receives: HTML document
  │
  ├─→ Browser Displays Document
  │   │
  │   └─→ User can print, save, download
  │
  └─→ END
```

## Data Flow for Session Creation

```
┌──────────────────┐
│ User Input Data  │
│ • Name: John     │
│ • Email: john@.. │
└────────┬─────────┘
         │
         ↓
┌──────────────────────────────┐
│ InterviewRequest Object      │
│ • userName: "John"           │
│ • userEmail: "john@..."      │
│ • interviewName: null        │
│ • variables: {}              │
└────────┬─────────────────────┘
         │
         ↓
┌──────────────────────────────┐
│ Docassemble API Request      │
│ POST /api/session            │
│ {                            │
│   "user": "John",            │
│   "email": "john@...",       │
│   "interview": "interview.." │
│ }                            │
└────────┬─────────────────────┘
         │
         ↓
┌──────────────────────────────┐
│ Docassemble Response         │
│ {                            │
│   "session_id": "abc123",    │
│   ...more fields...          │
│ }                            │
└────────┬─────────────────────┘
         │
         ↓
┌──────────────────────────────┐
│ InterviewSession Object      │
│ • sessionId: "abc123"        │
│ • interviewUrl: "http://..." │
│ • userId: "John"             │
│ • status: "CREATED"          │
└────────┬─────────────────────┘
         │
         ↓
┌──────────────────────────────┐
│ API Response to Browser      │
│ {                            │
│   "success": true,           │
│   "sessionId": "abc123",     │
│   "interviewUrl": "http://.."│
│ }                            │
└──────────────────────────────┘
```

## Spring Boot Request Handling

```
REQUEST: POST /api/interview/start
│
├─→ DispatcherServlet receives request
│
├─→ Maps to InterviewController.startInterview()
│
├─→ Controller method receives @RequestBody InterviewRequest
│
├─→ Validates input
│   ├─→ Check userName not null
│   └─→ Check userEmail not null
│
├─→ Calls DocassembleService.createInterviewSession()
│   │
│   ├─→ Builds Docassemble API request
│   │
│   ├─→ Uses HttpClient to send request
│   │
│   ├─→ Parses JSON response with ObjectMapper
│   │
│   └─→ Returns InterviewSession object
│
├─→ Creates response Map
│
├─→ Returns ResponseEntity with status 200
│
└─→ Jackson serializes response to JSON
   │
   └─→ RESPONSE: { "success": true, ... }
```

## Configuration Loading

```
application.properties
        │
        ├─→ docassemble.api.url=http://localhost:8000
        │
        ├─→ docassemble.api.key=YOUR_KEY
        │
        ├─→ docassemble.interview.name=interview_name
        │
        └─→ docassemble.interview.package=docassemble.demo
                │
                └─→ @ConfigurationProperties(prefix = "docassemble")
                    │
                    └─→ DocassembleConfig class
                        │
                        └─→ Injected into Services
                            │
                            └─→ Used to make API calls
```

## HTTP Communication

```
Spring Boot ←→ Docassemble Server
     │                  │
     └─ POST /api/session
        Authorization: Bearer KEY
        Content-Type: application/json
        {
          "user": "John",
          "email": "john@example.com",
          "interview": "interview_name"
        }
                    ↓
                    │
     ←─ 200 OK
        Content-Type: application/json
        {
          "session_id": "abc123",
          "url": "...",
          ...
        }
                    │
     ┌──────────────┘
     │
     └─ GET /api/session/{sessionId}/get
        Authorization: Bearer KEY
                    ↓
                    │
     ←─ 200 OK
        Content-Type: application/json
        {
          "html": "<html>...",
          "user": "John",
          ...
        }
```

## Docker Service Dependencies

```
docker-compose.yml
        │
        ├─→ docassemble (Port 8000)
        │   └─→ Depends on:
        │       ├─→ docassemble-postgresql
        │       │   └─→ Port 5432
        │       │   └─→ Database for Docassemble
        │       │
        │       └─→ docassemble-redis
        │           └─→ Port 6379
        │           └─→ Cache/Session storage
        │
        └─→ All connected via 'docassemble-network'
            └─→ Services can communicate by name
```

## Error Handling Flow

```
HTTP Request
    │
    ├─→ Controller receives request
    │
    ├─→ Service processes
    │   │
    │   └─→ Exception occurs
    │       │
    │       ├─→ IOException
    │       │   └─→ Docassemble unreachable
    │       │
    │       ├─→ IllegalArgumentException
    │       │   └─→ Invalid input
    │       │
    │       └─→ RuntimeException
    │           └─→ Unexpected error
    │
    ├─→ GlobalExceptionHandler catches
    │
    ├─→ Logs error
    │
    ├─→ Creates error response
    │   {
    │     "success": false,
    │     "error": "User-friendly message",
    │     "timestamp": 1234567890
    │   }
    │
    └─→ Returns with appropriate HTTP status
        ├─→ 400 Bad Request (validation)
        └─→ 500 Internal Server Error (unexpected)
```

## Development Setup Diagram

```
Your Computer
    │
    ├─→ Terminal 1
    │   └─→ docker-compose up -d
    │       ├─→ Creates docassemble container
    │       ├─→ Creates postgresql container
    │       └─→ Creates redis container
    │
    ├─→ Terminal 2
    │   └─→ mvn spring-boot:run
    │       ├─→ Compiles Java code
    │       ├─→ Starts Spring Boot
    │       └─→ Listens on port 8080
    │
    └─→ Browser
        ├─→ http://localhost:8080 (Spring Boot)
        ├─→ http://localhost:8000 (Docassemble)
        └─→ http://localhost:5432 (PostgreSQL)
```

---

**These diagrams help visualize how all the components work together!**
