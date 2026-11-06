setup:
	cd app && ./gradlew clean build -x test

test:
	cd app && ./gradlew test

lint:
	cd app && ./gradlew checkstyleMain
