network:
	docker network create --driver bridge foodapp-network

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
	docker run -d --name wallet -p 8082:8082 --network foodapp-network wallet
	docker run -d --name restaurant -p 8080:8080 --network foodapp-network restaurant
	docker run -d --name delivery -p 8081:8081 --network foodapp-network delivery

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
	docker network rm foodapp-network