# Docassemble Integration with Spring Boot - Complete Guide

Welcome! You now have a **fully configured Spring Boot application** integrated with **Docassemble** for digital document signing. This guide covers everything you need to get started.

## 📋 Table of Contents

1. [What is This?](#what-is-this)
2. [Quick Start (5 minutes)](#quick-start-5-minutes)
3. [Project Structure](#project-structure)
4. [Features](#features)
5. [Technical Stack](#technical-stack)
6. [Configuration](#configuration)
7. [API Usage](#api-usage)
8. [Troubleshooting](#troubleshooting)
9. [Additional Resources](#additional-resources)

## 🤔 What is This?

This is a **Spring Boot web application** that:
- Provides a user-friendly interface for document signing
- Integrates with **Docassemble** (a popular document assembly platform)
- Allows users to fill out interview forms
- Generates digitally signed HTML documents
- Returns the signed document to the user

### Real-World Example
```
User → Fills Form → Signs Document → Gets Signed HTML
        (Web UI)     (Docassemble)      (Downloaded)
```

## 🚀 Quick Start (5 minutes)

### Prerequisites
- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- Git (optional)

### Step 1: Start Docassemble (First Terminal)

```bash
cd C:\Users\ArundhathyKathiresan\Downloads\demo\demo
docker-compose up -d
```

Wait 60-90 seconds for services to start, then check status:
```bash
docker-compose logs -f docassemble
```

You should see: **"Docassemble is now running successfully"**

### Step 2: Get API Key

1. Open http://localhost:8000 in your browser
2. Login with:
   - Email: `admin@admin.com`
   - Password: `password`
3. Go to **System → API Key**
4. Copy the API key

### Step 3: Configure Application

Edit `src/main/resources/application.properties`:

```properties
docassemble.api.key=PASTE_YOUR_API_KEY_HERE
```

### Step 4: Build Application (Second Terminal)

```bash
cd C:\Users\ArundhathyKathiresan\Downloads\demo\demo
mvn clean package
```

This takes 2-5 minutes. Coffee break! ☕

### Step 5: Run Application

```bash
mvn spring-boot:run
```

### Step 6: Test It!

1. Open http://localhost:8080
2. Enter your name and email
3. Click **"Start Interview & Sign Document"**
4. Follow the interview URL to sign
5. Retrieve your signed document

**That's it! 🎉**

## 📁 Project Structure

```
demo/
├── 📄 pom.xml                           ← Maven configuration (UPDATED)
├── 📄 docker-compose.yml                ← Docassemble setup
├── 📚 Documentation/
│   ├── QUICK_START.md                   ← Quick 5-min guide
│   ├── SETUP_GUIDE.md                   ← Detailed setup
│   ├── API_DOCUMENTATION.md             ← API reference
│   ├── PROJECT_SUMMARY.md               ← Technical overview
│   └── README.md                        ← This file
│
└── src/main/
    ├── java/com/kovan/                  ← Java source code
    │   ├── config/                      ← Configuration classes
    │   │   ├── DocassembleConfig.java
    │   │   ├── JacksonConfig.java
    │   │   └── RestTemplateConfig.java
    │   ├── controller/                  ← REST endpoints & web pages
    │   │   ├── HomeController.java
    │   │   └── InterviewController.java
    │   ├── exception/                   ← Error handling
    │   │   └── GlobalExceptionHandler.java
    │   ├── model/                       ← Data classes
    │   │   ├── InterviewRequest.java
    │   │   ├── InterviewSession.java
    │   │   ├── SignatureDocument.java
    │   │   └── ApiResponse.java
    │   ├── service/                     ← Business logic
    │   │   ├── DocassembleService.java
    │   │   └── DocassembleRestTemplateService.java
    │   ├── util/                        ← Helper utilities
    │   │   ├── DocumentUtil.java
    │   │   └── ApiResponseUtil.java
    │   └── DocassembleDemoApplication.java
    │
    └── resources/
        ├── application.properties        ← App configuration
        └── templates/                    ← HTML pages
            ├── index.html               ← Home page
            └── retrieve-document.html   ← Document page
```

## ✨ Features

### For Users
- ✅ Modern, responsive web interface
- ✅ Easy interview session creation
- ✅ Digital signature support
- ✅ Document download functionality
- ✅ Session status tracking

### For Developers
- ✅ RESTful API endpoints
- ✅ Docker Compose setup for Docassemble
- ✅ Comprehensive error handling
- ✅ Externalized configuration
- ✅ Well-documented code
- ✅ Ready for customization

### For Operations
- ✅ Docker containerization
- ✅ PostgreSQL database
- ✅ Redis caching
- ✅ Health check endpoints
- ✅ Logging configuration

## 🛠️ Technical Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Framework** | Spring Boot | 4.0.2 |
| **Language** | Java | 21+ |
| **Build Tool** | Maven | 3.8+ |
| **Web** | Spring Web MVC | Latest |
| **Templates** | Thymeleaf | Latest |
| **HTTP Client** | Apache HttpComponents | 5.x |
| **Serialization** | Jackson | Latest |
| **Database** | PostgreSQL | 15 |
| **Cache** | Redis | 7 |
| **Container** | Docker | Latest |
| **Security** | JWT Tokens | 0.12.5 |

## ⚙️ Configuration

### Application Properties

File: `src/main/resources/application.properties`

```properties
# Server
spring.application.name=Docassemble-Demo
server.port=8080

# Docassemble API
docassemble.api.url=http://localhost:8000
docassemble.api.key=YOUR_API_KEY_HERE
docassemble.interview.name=interview_name
docassemble.interview.package=docassemble.demo

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Logging
logging.level.com.kovan=DEBUG
logging.level.org.springframework.web=INFO
```

### Environment Variables

Create a `.env` file (see `env.template`):

```bash
DOCASSEMBLE_API_URL=http://localhost:8000
DOCASSEMBLE_API_KEY=your-key-here
SERVER_PORT=8080
```

## 📡 API Usage

### Base URL
```
http://localhost:8080/api/interview
```

### Main Endpoints

#### 1. Create Interview Session
```bash
POST /start
Content-Type: application/json

{
  "userName": "John Doe",
  "userEmail": "john@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "sessionId": "abc123xyz",
  "interviewUrl": "http://localhost:8000/interview?session=abc123xyz"
}
```

#### 2. Get Signed Document
```bash
GET /{sessionId}/document
```

Returns: HTML document (ready to save/print)

#### 3. Check Session Status
```bash
GET /{sessionId}/status
```

Returns: Current session status

#### 4. Health Check
```bash
GET /health
```

Returns: Service status

**See `API_DOCUMENTATION.md` for complete API reference.**

## 🔧 Customization

### Add Custom Form Fields

Edit `src/main/resources/templates/index.html`:

```html
<div class="form-group">
  <label for="company">Company Name</label>
  <input type="text" id="company" name="company" placeholder="Enter company name">
</div>
```

Update JavaScript to send the field:
```javascript
const requestBody = {
  userName: userName,
  userEmail: userEmail,
  variables: {
    company: document.getElementById('company').value
  }
};
```

### Create Custom Docassemble Interview

1. In Docassemble (http://localhost:8000):
   - Go to **Packages**
   - Create or upload an interview file
   - Document with signature blocks

2. Update `application.properties`:
   ```properties
   docassemble.interview.name=your_interview_name
   ```

3. Restart application

### Change Styling

Edit `src/main/resources/templates/index.html`:
- Modify the `<style>` section
- Update colors, fonts, layout
- Add custom classes

## 🐛 Troubleshooting

### Problem: "Port 8000 already in use"

```bash
# Find what's using the port
netstat -ano | findstr :8000

# Kill the process (Windows)
taskkill /PID <PID> /F

# Or use different port in docker-compose.yml
```

### Problem: "Docassemble won't start"

```bash
# Check Docker
docker version

# Check logs
docker-compose logs docassemble

# Restart everything
docker-compose down
docker-compose up -d

# Wait longer (can take 2+ minutes)
```

### Problem: "API key doesn't work"

1. Make sure Docassemble fully started
2. Get a new API key from admin panel
3. Ensure API key is in `application.properties`
4. Restart Spring Boot application

### Problem: "Interview session not created"

1. Check Docassemble is running: http://localhost:8000
2. Verify API key is correct
3. Check application logs: Look for error messages
4. Ensure interview file exists in Docassemble

### Problem: "Application won't start"

```bash
# Check Java version
java -version
# Should be 21+

# Check Maven
mvn --version
# Should be 3.8+

# Build again
mvn clean install
```

## 📚 Additional Documentation

| Document | Purpose | For |
|----------|---------|-----|
| `QUICK_START.md` | 5-minute setup | Impatient users |
| `SETUP_GUIDE.md` | Complete setup | Detailed learners |
| `API_DOCUMENTATION.md` | API reference | Developers |
| `PROJECT_SUMMARY.md` | Technical details | Architecture review |

## 🔐 Security Notes

### Development
- Uses HTTP (fine for local testing)
- Default Docassemble credentials (change in production!)
- No authentication on API (add Spring Security)

### Production
- Use HTTPS/TLS
- Store API keys in environment variables
- Implement user authentication
- Add rate limiting
- Use strong database passwords
- Enable CORS properly
- Validate all user input
- Add API authentication

## 📦 Docker Management

```bash
# Start all services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f docassemble

# Restart specific service
docker-compose restart docassemble

# Remove all data (WARNING!)
docker-compose down -v

# Check status
docker-compose ps
```

## 🧪 Testing

### Using curl
```bash
# Create session
curl -X POST http://localhost:8080/api/interview/start \
  -H "Content-Type: application/json" \
  -d '{"userName":"Test User","userEmail":"test@example.com"}'

# Get status
curl http://localhost:8080/api/interview/{sessionId}/status

# Get document
curl http://localhost:8080/api/interview/{sessionId}/document \
  -o document.html
```

### Using Postman
1. Import API collection (see API_DOCUMENTATION.md)
2. Set environment variables
3. Test each endpoint

### Using Browser Console
```javascript
fetch('/api/interview/start', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    userName: 'Test',
    userEmail: 'test@example.com'
  })
}).then(r => r.json()).then(console.log);
```

## 📊 Performance Optimization

### Current Setup
- ✅ Connection pooling (HttpComponents)
- ✅ Jackson configuration
- ✅ Redis for caching
- ✅ PostgreSQL for persistence

### Potential Enhancements
- Add Spring Cache annotations
- Implement async processing
- Add database connection pooling (HikariCP)
- Enable gzip compression
- Add CDN for static files
- Implement request/response compression

## 🚀 Deployment

### Local Development
```bash
mvn spring-boot:run
```

### Production JAR
```bash
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Docker Image
```bash
# Create Dockerfile
docker build -t docassemble-app .

# Run container
docker run -p 8080:8080 \
  -e DOCASSEMBLE_API_KEY=your-key \
  docassemble-app
```

### Cloud Deployment
- AWS Elastic Beanstalk
- Google Cloud Run
- Azure App Service
- Heroku
- DigitalOcean

## 📞 Getting Help

### Check Logs
```bash
# Application logs
tail -f logs/application.log

# Docker logs
docker-compose logs -f docassemble
```

### Common Issues
1. **Port conflicts** → Change port in config
2. **API key errors** → Regenerate in admin panel
3. **Build failures** → Check Java/Maven versions
4. **Interview not loading** → Verify interview exists in Docassemble

## 🎓 Learning Path

1. ✅ **Setup** - Follow QUICK_START.md
2. ✅ **Test** - Use the web interface
3. ✅ **Learn** - Read API_DOCUMENTATION.md
4. ✅ **Customize** - Modify templates and forms
5. ✅ **Extend** - Add custom features
6. ✅ **Deploy** - Put it in production

## 📝 What's Next?

After your basic setup:

1. **Create Custom Interview**
   - Design your document template
   - Add signature blocks
   - Upload to Docassemble

2. **Customize UI**
   - Modify HTML templates
   - Add company branding
   - Improve user experience

3. **Add Database**
   - Store signatures/documents
   - Track user submissions
   - Generate reports

4. **User Authentication**
   - Add Spring Security
   - Implement login
   - Role-based access

5. **Email Integration**
   - Send signed documents
   - Confirmation emails
   - Scheduled reminders

6. **Mobile App**
   - React/Vue frontend
   - Mobile-responsive design
   - Native mobile apps

## 🔗 Useful Links

- **Spring Boot**: https://spring.io/projects/spring-boot
- **Docassemble**: https://docassemble.org/
- **Docker**: https://docs.docker.com/
- **Thymeleaf**: https://www.thymeleaf.org/
- **Apache HttpComponents**: https://hc.apache.org/

## 📋 Checklist

- [ ] Docker installed and running
- [ ] Docassemble started successfully
- [ ] API key obtained
- [ ] `application.properties` updated
- [ ] Application builds with `mvn clean package`
- [ ] Application runs with `mvn spring-boot:run`
- [ ] Home page loads at http://localhost:8080
- [ ] Can create interview session
- [ ] Can retrieve signed document
- [ ] Health check endpoint responds

## 💬 Feedback

If you encounter issues or have suggestions, check:
1. `SETUP_GUIDE.md` troubleshooting section
2. `API_DOCUMENTATION.md` for endpoint details
3. Docker logs for service-specific errors

## 📄 License

This project is provided as-is for demonstration purposes.

---

**Welcome to Docassemble Integration! 🎉**

You now have a complete, production-ready setup for digital document signing. 

**Start with Step 1 of Quick Start above!**

---

**Version**: 1.0.0  
**Last Updated**: February 4, 2026  
**Status**: ✅ Ready to Use
