jars:
	cd Wallet && ./mvnw package && \
	cd ../Delivery && ./mvnw package && \
	cd ../Restaurant && ./mvnw package && \
	cd ..

images:
	docker build -t wallet-service ./Wallet
	docker build -t restaurant-service ./Restaurant
	docker build -t delivery-service ./Delivery
	docker images

containers:
	docker run -d -p 8082:8080 --rm --name wallet --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt wallet-service
	docker run -d -p 8080:8080 --rm --name restaurant --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt restaurant-service
	docker run -d -p 8081:8080 --rm --name delivery --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt delivery-service

stop:
	docker stop wallet restaurant delivery

clean:
	docker rmi -f wallet-service
	docker rmi -f restaurant-service
	docker rmi -f delivery-service