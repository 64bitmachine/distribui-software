jars:
	cd wallet && ./mvnw package && \
	cd ../delivery && ./mvnw package && \
	cd ../restaurant && ./mvnw package && \
	cd ..

images:
	docker build -t wallet ./wallet
	docker build -t restaurant ./restaurant
	docker build -t delivery ./delivery
	docker images

containers:
	docker run -d --name wallet -p 8082:8082 wallet 
	docker run -d --name restaurant -p 8080:8080 restaurant
	docker run -d --name delivery -p 8081:8081 delivery

start:
	docker start wallet restaurant delivery

stop:
	docker stop wallet restaurant delivery

clean: stop
	docker rm -f wallet
	docker rm -f restaurant
	docker rm -f delivery
	docker rmi -f wallet
	docker rmi -f restaurant
	docker rmi -f delivery