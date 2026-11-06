setup:
	npm install
	npx build-frontend
	cd app && ./gradlew clean build -x test

test:
	cd app && ./gradlew test
