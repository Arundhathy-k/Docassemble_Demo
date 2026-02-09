# Getting Started Checklist

Use this checklist to ensure you have everything set up correctly!

## ✅ Pre-Setup Requirements

- [ ] Java 21 or higher installed
  ```bash
  java -version
  ```
  
- [ ] Maven 3.8 or higher installed
  ```bash
  mvn --version
  ```
  
- [ ] Docker installed and running
  ```bash
  docker --version
  docker-compose --version
  ```
  
- [ ] Port 8000 available (Docassemble)
  ```bash
  netstat -ano | findstr :8000
  ```
  
- [ ] Port 8080 available (Spring Boot)
  ```bash
  netstat -ano | findstr :8080
  ```
  
- [ ] Port 5432 available (PostgreSQL)
  ```bash
  netstat -ano | findstr :5432
  ```
  
- [ ] Port 6379 available (Redis)
  ```bash
  netstat -ano | findstr :6379
  ```

## 📦 Step 1: Start Docassemble

- [ ] Navigate to project directory
  ```bash
  cd C:\Users\ArundhathyKathiresan\Downloads\demo\demo
  ```

- [ ] Start Docker Compose
  ```bash
  docker-compose up -d
  ```

- [ ] Wait for services to start (60-90 seconds)
  ```bash
  docker-compose logs -f docassemble
  ```
  
- [ ] Verify all services are running
  ```bash
  docker-compose ps
  # Should show all services as "Up"
  ```

- [ ] Test Docassemble API
  ```bash
  curl http://localhost:8000/api/user/info
  # Should return user info
  ```

## 🔑 Step 2: Get API Key

- [ ] Open http://localhost:8000 in browser

- [ ] Login with default credentials
  - Email: `admin@admin.com`
  - Password: `password`

- [ ] Navigate to System → API Key

- [ ] Copy the API key (long string starting with letters/numbers)

- [ ] Save it somewhere safe temporarily

## ⚙️ Step 3: Configure Application

- [ ] Open `src/main/resources/application.properties`

- [ ] Find line: `docassemble.api.key=your-api-key-here`

- [ ] Replace with your actual API key:
  ```properties
  docassemble.api.key=YOUR_ACTUAL_API_KEY_HERE
  ```

- [ ] Save the file (Ctrl+S)

- [ ] Verify file was saved (no asterisk in tab name)

## 🏗️ Step 4: Build Application

- [ ] Open second terminal window

- [ ] Navigate to project directory
  ```bash
  cd C:\Users\ArundhathyKathiresan\Downloads\demo\demo
  ```

- [ ] Run Maven build
  ```bash
  mvn clean package
  ```
  
- [ ] Wait for build to complete (2-5 minutes)
  
- [ ] Verify build success
  - Look for: `BUILD SUCCESS`
  - Look for JAR file: `target/demo-0.0.1-SNAPSHOT.jar`

## 🚀 Step 5: Run Application

- [ ] Run Spring Boot application
  ```bash
  mvn spring-boot:run
  ```

- [ ] Wait for startup (30-60 seconds)

- [ ] Look for message: `Tomcat started on port(s): 8080`

- [ ] Application is ready when you see no more startup messages

## 🧪 Step 6: Test Application

- [ ] Open http://localhost:8080 in browser

- [ ] Verify home page loads with form

- [ ] Enter test data:
  - Full Name: `Test User`
  - Email: `test@example.com`

- [ ] Click "Start Interview & Sign Document" button

- [ ] Verify success message appears

- [ ] Copy the Session ID

- [ ] Click the interview URL link

- [ ] Docassemble interview page should load

- [ ] Verify you see the interview form

## 📄 Step 7: Complete Interview (Optional)

- [ ] Fill out the interview form fields

- [ ] Follow instructions to sign document

- [ ] Submit the interview form

- [ ] You should return to browser showing completion

## 🔄 Step 8: Retrieve Signed Document

- [ ] Go back to home page (http://localhost:8080)

- [ ] Note the Session ID from earlier

- [ ] Option A: Use direct URL
  ```
  http://localhost:8080/api/interview/{sessionId}/document
  ```
  Replace `{sessionId}` with your actual ID

- [ ] Option B: Use JSON endpoint
  ```
  http://localhost:8080/api/interview/{sessionId}/document/json
  ```

- [ ] Document should appear (HTML in browser or JSON)

## ✨ Verification Checklist

### Services Running
- [ ] Docassemble: http://localhost:8000 → Admin page loads
- [ ] Spring Boot: http://localhost:8080 → Home page loads
- [ ] PostgreSQL: `docker-compose ps` shows postgres as Up
- [ ] Redis: `docker-compose ps` shows redis as Up

### API Endpoints Working
- [ ] POST /api/interview/start → Returns sessionId
- [ ] GET /api/interview/health → Returns status UP
- [ ] GET /api/interview/{id}/status → Returns status info

### Configuration
- [ ] API key in application.properties matches Docassemble
- [ ] Server port is 8080
- [ ] Docassemble API URL is http://localhost:8000

### Application Features
- [ ] Home page form displays
- [ ] Can create interview session
- [ ] Interview URL works
- [ ] Can retrieve signed document

## 🆘 If Something Goes Wrong

### Docker Issues
```bash
# Stop everything
docker-compose down

# Remove all containers
docker-compose down -v

# Start fresh
docker-compose up -d
```

### Build Issues
```bash
# Clean Maven cache
mvn clean

# Rebuild
mvn package
```

### Port Conflicts
```bash
# Find process using port
netstat -ano | findstr :8080

# Kill process (Windows)
taskkill /PID <PID> /F
```

### Application Won't Start
```bash
# Check Java version (need 21+)
java -version

# Check Maven version (need 3.8+)
mvn --version

# Check logs in console
# Look for error messages
```

## 📞 Troubleshooting Steps

1. **Read the error message carefully**
   - Error messages tell you exactly what's wrong

2. **Check documentation**
   - Read SETUP_GUIDE.md for detailed help
   - Check API_DOCUMENTATION.md for endpoint help

3. **Check logs**
   - Application logs show in terminal
   - Docker logs: `docker-compose logs docassemble`

4. **Verify services**
   - All services should show as "Up" in `docker-compose ps`
   - Try accessing URLs directly in browser

5. **Check configuration**
   - Verify API key in application.properties
   - Verify URLs are correct
   - Verify ports are not in use

## 📊 Expected Results

### Successful Startup Messages

**Docker Compose:**
```
Creating docassemble ... done
Creating docassemble-postgresql ... done
Creating docassemble-redis ... done
```

**Spring Boot:**
```
Tomcat started on port(s): 8080 (http)
Started DocassembleDemoApplication in X.XXX seconds
```

### Successful API Call
```json
{
  "success": true,
  "sessionId": "abc123xyz...",
  "interviewUrl": "http://localhost:8000/interview?session=abc123xyz..."
}
```

## 🎯 Success Criteria

You're done when:

1. ✅ Docker services are all running
2. ✅ Docassemble admin page loads
3. ✅ Spring Boot application starts without errors
4. ✅ Home page loads at http://localhost:8080
5. ✅ You can create an interview session
6. ✅ You can view the interview in Docassemble
7. ✅ You can retrieve the signed document
8. ✅ Health check endpoint returns status UP

## 🎉 Congratulations!

If you've checked all items, your Docassemble integration is ready!

### Next Steps:
1. Create custom Docassemble interviews
2. Customize the HTML templates
3. Add user authentication
4. Integrate with database
5. Deploy to production

### Helpful Commands

```bash
# View application logs
mvn spring-boot:run  # Check console output

# View Docker logs
docker-compose logs -f docassemble

# Check what's running
docker-compose ps

# Stop everything (but keep data)
docker-compose stop

# Start again
docker-compose start

# Complete reset (loses data!)
docker-compose down -v
docker-compose up -d
```

### Key URLs to Remember

| Service | URL | Username | Password |
|---------|-----|----------|----------|
| Spring App | http://localhost:8080 | N/A | N/A |
| Docassemble | http://localhost:8000 | admin@admin.com | password |
| PostgreSQL | localhost:5432 | docassemble | docassemble |
| Redis | localhost:6379 | N/A | N/A |

---

**Last Updated**: February 4, 2026  
**Version**: 1.0.0  
**Status**: Ready for Use ✅
