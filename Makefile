jars:
	cd Wallet && ./mvnw package && \
	cd ../Delivery && ./mvnw -DskipTests package && \
	cd ../Restaurant && ./mvnw package && \
	cd ..

images:
	docker build -t wallet-service ./Wallet
	docker build -t restaurant-service ./Restaurant
	docker build -t delivery-service ./Delivery
	docker images

containers:
	docker run --name delivery-db --add-host=host.docker.internal:host-gateway -p 3306 -e MYSQL_ROOT_PASSWORD=root -d mysql:latest
	docker run -d -p 8082:8080 --rm --name wallet --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt wallet-service
	docker run -d -p 8080:8080 --rm --name restaurant --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt restaurant-service
	docker run -d -p 8081:8080 --rm --name delivery --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt delivery-service
	
stop:
	docker stop wallet restaurant delivery

clean:
	docker rmi -f wallet-service
	docker rmi -f restaurant-service
	docker rmi -f delivery-service
	docker rm -f delivery-db