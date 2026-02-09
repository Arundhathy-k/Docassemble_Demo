# Quick Start Guide

## Prerequisites
- Java 21+
- Maven 3.8+
- Docker & Docker Compose installed

## 5-Minute Setup

### 1. Start Docassemble (First Terminal)
```bash
cd C:\Users\ArundhathyKathiresan\Downloads\demo\demo
docker-compose up -d

# Wait for services to start (60-90 seconds)
# Check status:
docker-compose logs -f docassemble
```

### 2. Configure API Key (After Docassemble Starts)
1. Open http://localhost:8000
2. Login with: admin@admin.com / password
3. Go to System → API Key
4. Copy the API key

### 3. Update Configuration
Edit `src/main/resources/application.properties`:
```properties
docassemble.api.key=YOUR_COPIED_API_KEY
```

### 4. Build & Run Application (Second Terminal)
```bash
cd C:\Users\ArundhathyKathiresan\Downloads\demo\demo

# Build
mvn clean package

# Run
mvn spring-boot:run
```

### 5. Test the Application
1. Open http://localhost:8080
2. Enter your name and email
3. Click "Start Interview & Sign Document"
4. Follow the interview link to sign the document
5. Retrieve the signed HTML document

## Troubleshooting

**Port in use?**
```bash
# Find what's using port 8000/8080
netstat -ano | findstr :8000
netstat -ano | findstr :8080

# Change port in docker-compose.yml or application.properties
```

**Docassemble not starting?**
```bash
# Check logs
docker-compose logs docassemble

# Restart
docker-compose down
docker-compose up -d
```

**API Key not working?**
- Make sure to wait for Docassemble to fully start
- Double-check the API key is correct
- Restart the Spring Boot application

## Key Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven dependencies |
| `docker-compose.yml` | Docassemble setup |
| `application.properties` | App configuration |
| `src/main/java/com/kovan/` | Java source code |
| `src/main/resources/templates/` | HTML templates |

## Next Steps

- Read `SETUP_GUIDE.md` for detailed configuration
- Check `API_DOCUMENTATION.md` for API endpoints
- Create custom Docassemble interviews
- Customize the HTML template

---

**Ready?** Start with Step 1 above! 🚀
