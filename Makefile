.PHONY: backend-build admin-build h5-build all-build backend-run admin-dev h5-dev tests

backend-build:
	cd backend && ./mvnw -DskipTests package

admin-build:
	cd frontend-admin && npm ci || npm install && npm run build

h5-build:
	cd frontend-h5 && npm ci || npm install && npm run build

all-build: backend-build admin-build h5-build

backend-run:
	cd backend && ./mvnw spring-boot:run

admin-dev:
	cd frontend-admin && npm run dev

h5-dev:
	cd frontend-h5 && npm run dev

tests:
	cd frontend-admin && npm run test:unit && cd ../frontend-h5 && npm run test:unit

