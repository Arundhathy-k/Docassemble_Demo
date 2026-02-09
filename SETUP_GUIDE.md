# Docassemble Integration Demo

This Spring Boot application demonstrates complete integration with Docassemble for digital document signing and generation. Users can fill out an interview form, sign a document, and receive the signed HTML document as output.

## Project Overview

The application provides:
- A modern web interface for initiating interview sessions
- REST API endpoints for Docassemble integration
- Session management and document retrieval
- HTML document generation with client signatures
- Docker Compose configuration for local Docassemble setup

## Features

- ✅ Create interview sessions with client information
- ✅ Generate interview URLs for signing
- ✅ Retrieve signed HTML documents
- ✅ Check session status
- ✅ Complete session workflow
- ✅ PDF generation (configurable)
- ✅ Interview variable retrieval
- ✅ Webhook support (configurable)

## Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- Git

## Project Structure

```
demo/
├── src/
│   ├── main/
│   │   ├── java/com/kovan/
│   │   │   ├── config/
│   │   │   │   ├── DocassembleConfig.java       # Configuration properties
│   │   │   │   ├── JacksonConfig.java           # JSON serialization config
│   │   │   │   └── RestTemplateConfig.java      # REST client config
│   │   │   ├── controller/
│   │   │   │   ├── HomeController.java          # Home page routing
│   │   │   │   └── InterviewController.java     # Interview API endpoints
│   │   │   ├── model/
│   │   │   │   ├── InterviewRequest.java        # Interview request DTO
│   │   │   │   ├── InterviewSession.java        # Session response DTO
│   │   │   │   └── SignatureDocument.java       # Signed document DTO
│   │   │   ├── service/
│   │   │   │   ├── DocassembleService.java      # Docassemble API client
│   │   │   │   └── DocassembleRestTemplateService.java  # REST template service
│   │   │   └── DocassembleDemoApplication.java  # Main application class
│   │   └── resources/
│   │       ├── application.properties            # Application configuration
│   │       └── templates/
│   │           └── index.html                    # Home page template
│   └── test/
│       └── java/com/kovan/...                    # Test files
├── pom.xml                                        # Maven dependencies
└── docker-compose.yml                             # Docker Compose configuration
```

## Installation & Setup

### Step 1: Clone and Build the Project

```bash
cd C:\Users\ArundhathyKathiresan\Downloads\demo\demo
mvn clean install
```

### Step 2: Start Docassemble with Docker Compose

Before running your application, you need to start the Docassemble server:

```bash
# Navigate to the project directory
cd C:\Users\ArundhathyKathiresan\Downloads\demo\demo

# Start the Docker containers
docker-compose up -d

# Wait for services to be ready (check logs)
docker-compose logs -f docassemble
```

**Wait for the message: "Docassemble is now running successfully"**

This will start:
- **Docassemble Server**: http://localhost:8000
- **PostgreSQL Database**: localhost:5432
- **Redis Cache**: localhost:6379

### Step 3: Configure Docassemble

1. **Access Docassemble Admin Panel**:
   - Open http://localhost:8000 in your browser
   - Login with default credentials:
     - Email: admin@admin.com
     - Password: password

2. **Generate API Key**:
   - Go to System → API Key
   - Copy the generated API key

3. **Update Application Configuration**:
   - Edit `src/main/resources/application.properties`
   - Replace `your-api-key-here` with your actual Docassemble API key

```properties
docassemble.api.url=http://localhost:8000
docassemble.api.key=YOUR_ACTUAL_API_KEY_HERE
```

4. **Create an Interview File** (Optional):
   - In Docassemble, go to Packages
   - Create or upload an interview file
   - Default interview name: `interview_name`

### Step 4: Run the Spring Boot Application

```bash
# Using Maven
mvn spring-boot:run

# Or using Java directly
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

The application will start at: **http://localhost:8080**

## API Endpoints

### 1. Start Interview Session
```
POST /api/interview/start
Content-Type: application/json

Request Body:
{
  "userName": "John Doe",
  "userEmail": "john@example.com",
  "interviewName": "interview_name",  // optional
  "variables": {}                      // optional
}

Response:
{
  "success": true,
  "sessionId": "abc123xyz",
  "interviewUrl": "http://localhost:8000/interview?session=abc123xyz",
  "message": "Interview session created successfully"
}
```

### 2. Get Signed Document (HTML)
```
GET /api/interview/{sessionId}/document

Response: HTML document with client signature
```

### 3. Get Signed Document (JSON)
```
GET /api/interview/{sessionId}/document/json

Response:
{
  "success": true,
  "sessionId": "abc123xyz",
  "document": {
    "id": "...",
    "sessionId": "abc123xyz",
    "status": "COMPLETED",
    "signedBy": "John Doe",
    "htmlContent": "<html>...</html>"
  }
}
```

### 4. Get Session Status
```
GET /api/interview/{sessionId}/status

Response:
{
  "success": true,
  "sessionId": "abc123xyz",
  "status": { ... }
}
```

### 5. Complete Interview Session
```
POST /api/interview/{sessionId}/complete

Response:
{
  "success": true,
  "sessionId": "abc123xyz",
  "message": "Interview session completed successfully"
}
```

### 6. Health Check
```
GET /api/interview/health

Response:
{
  "status": "UP",
  "service": "Interview Service",
  "timestamp": 1707073200000
}
```

## Usage Example

### Step 1: Create Interview Session
```bash
curl -X POST http://localhost:8080/api/interview/start \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "John Doe",
    "userEmail": "john@example.com"
  }'
```

### Step 2: Access Interview URL
Open the returned `interviewUrl` in your browser to:
- Fill out the interview form
- Sign the document

### Step 3: Retrieve Signed Document
```bash
curl http://localhost:8080/api/interview/{sessionId}/document > signed_document.html
```

Or as JSON:
```bash
curl http://localhost:8080/api/interview/{sessionId}/document/json
```

## Dependencies

### Core Dependencies
- **Spring Boot 4.0.2**: Web framework
- **Spring Web**: REST API support
- **Thymeleaf**: Server-side template engine
- **Lombok**: Boilerplate reduction

### Integration Dependencies
- **HttpComponents Client 5**: HTTP client for API calls
- **Jackson**: JSON serialization/deserialization
- **JJWT**: JWT token support (for future enhancements)
- **Commons Lang 3**: Utility functions

## Configuration Properties

Edit `src/main/resources/application.properties`:

```properties
# Server configuration
server.port=8080

# Docassemble API configuration
docassemble.api.url=http://localhost:8000          # Docassemble server URL
docassemble.api.key=your-api-key-here              # Your API key
docassemble.interview.name=interview_name           # Default interview name
docassemble.interview.package=docassemble.demo      # Interview package

# File upload limits
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Logging
logging.level.com.kovan=DEBUG
logging.level.org.springframework.web=INFO
```

## Troubleshooting

### Docassemble Server Not Starting
```bash
# Check container logs
docker-compose logs docassemble

# Ensure ports 8000, 8443, 5432, 6379 are available
netstat -ano | findstr :8000

# Stop and restart containers
docker-compose down
docker-compose up -d
```

### API Key Not Working
1. Verify you copied the correct API key from Docassemble admin panel
2. Check that Docassemble server is running: `curl http://localhost:8000/api/user/info`
3. Restart the Spring Boot application after updating the API key

### Interview Session Creation Fails
1. Ensure Docassemble server is fully started (wait for health check to pass)
2. Verify API key in `application.properties`
3. Check Docassemble logs: `docker-compose logs docassemble`
4. Ensure interview file exists in Docassemble package

### Port Already in Use
```bash
# Windows: Kill process using port
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Or use a different port in application.properties
server.port=8081
```

## Docker Management

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f docassemble

# Stop services
docker-compose down

# Remove volumes (deletes data)
docker-compose down -v

# Restart specific service
docker-compose restart docassemble

# Check service status
docker-compose ps
```

## Development Tips

1. **Enable Debug Logging**:
   ```properties
   logging.level.com.kovan=DEBUG
   logging.level.org.springframework.web=DEBUG
   ```

2. **Test API Endpoints**:
   - Use Postman or curl for testing
   - Set `Content-Type: application/json` header

3. **View Docassemble Logs**:
   ```bash
   docker-compose logs -f docassemble
   ```

4. **Access Docassemble Database**:
   ```bash
   psql -h localhost -U docassemble -d docassemble
   ```

## Creating a Custom Interview

To create your own Docassemble interview:

1. Create a `.docx` template in Docassemble
2. Add signature fields using Docassemble syntax
3. Upload to your Docassemble package
4. Update `docassemble.interview.name` in properties
5. Restart the application

## Security Considerations

- Store API keys in environment variables (not in version control)
- Use HTTPS in production
- Implement authentication/authorization for your endpoints
- Validate user input on both client and server
- Set up CORS properly for production

## Future Enhancements

- [ ] User authentication & authorization
- [ ] Multi-language support
- [ ] Advanced PDF generation options
- [ ] Document versioning
- [ ] Email delivery of signed documents
- [ ] Audit logging
- [ ] Webhook support
- [ ] Progress tracking
- [ ] Custom styling for interviews
- [ ] Integration with document storage services

## Support & Documentation

- **Docassemble Docs**: https://docassemble.org/
- **Docassemble API**: https://docassemble.org/api.html
- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Docker Docs**: https://docs.docker.com/

## License

This project is provided as-is for demonstration purposes.

## Additional Notes

- The default interview name is `interview_name` - customize as needed
- All API responses include a `success` boolean flag
- Error responses include descriptive error messages
- Session IDs can be used to track document signing progress
- The HTML document output is ready for printing or further processing

---

**Last Updated**: February 4, 2026
**Version**: 1.0.0
