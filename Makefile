setup:
	cd app && npm install
	cd app && npx build-frontend
	cd app && gradle clean build -x test

test:
	cd app && gradle test

lint:
	cd app && gradle checkstyleMain
