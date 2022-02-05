jars:
	cd wallet && ./mvnw -DskipTests package && \
	cd ../delivery && ./mvnw -DskipTests package && \
	cd ../restaurant && ./mvnw -DskipTests package && \
	cd ..

images:
	docker build -t wallet ./wallet
	docker build -t restaurant ./restaurant
	docker build -t delivery ./delivery
	docker images

containers:
	docker run -d --name wallet --network host wallet
	docker run -d --name restaurant --network host restaurant
	docker run -d --name delivery --network host delivery

start:
	docker start wallet restaurant delivery

stop:
	docker stop wallet restaurant delivery

clean:
	docker rm -f wallet
	docker rm -f restaurant
	docker rm -f delivery
	docker rmi -f wallet
	docker rmi -f restaurant
	docker rmi -f delivery