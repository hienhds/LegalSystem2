# CHẠY TẤT CẢ SERVICES - LEGAL SYSTEM

## BƯỚC 1: INFRASTRUCTURE (Docker)
docker compose up -d

## BƯỚC 2: DISCOVERY SERVER (Terminal 1)
cd discovery-server
.\gradlew bootRun --args='--spring.profiles.active=dev'

## BƯỚC 3: USER SERVICE (Terminal 2)
cd user-service
.\gradlew bootRun --args='--spring.profiles.active=dev'

## BƯỚC 4: FILE SERVICE (Terminal 3)
cd file-service
.\gradlew bootRun --args='--spring.profiles.active=dev'

## BƯỚC 5: CHAT SERVICE (Terminal 4)
cd chat-service
.\gradlew bootRun --args='--spring.profiles.active=dev'

## BƯỚC 6: NOTIFICATION SERVICE (Terminal 5)
cd notification-service
.\gradlew bootRun --args='--spring.profiles.active=dev'

## BƯỚC 7: SEARCH SERVICE (Terminal 6)
cd search-service
.\gradlew bootRun --args='--spring.profiles.active=dev'

## BƯỚC 8: SCHEDULE SERVICE (Terminal 7)
cd schedule-service
.\gradlew bootRun --args='--spring.profiles.active=dev'

## BƯỚC 9: API GATEWAY (Terminal 8)
cd api-gateway
.\gradlew bootRun --args='--spring.profiles.active=dev'

cd document-service
.\gradlew bootRun --args='--spring.profiles.active=dev'

cd case-service
.\gradlew bootRun --args='--spring.profiles.active=dev'
## BƯỚC 10: FRONTEND (Terminal 9)
cd frontend/my-react-app
npm run dev

## CHECK SERVICES
# Discovery Server Dashboard
http://localhost:8761

# API Gateway Health
http://localhost:8080/actuator/health

# Frontend
http://localhost:5173
