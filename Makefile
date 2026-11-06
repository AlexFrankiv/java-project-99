setup:
	apt-get update && apt-get install -y openjdk-21-jdk
	update-alternatives --set java /usr/lib/jvm/java-21-openjdk-amd64/bin/java
	npm install
	npx build-frontend
	cd app && JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew clean build -x test

test:
	apt-get update && apt-get install -y openjdk-21-jdk
	update-alternatives --set java /usr/lib/jvm/java-21-openjdk-amd64/bin/java
	cd app && JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew test