jars:
	cd Wallet && ./mvnw -DskipTests package && \
	cd ../Restaurant && ./mvnw -DskipTests package && \
	cd .. && \
	cd Delivery && \
	mvn compile && \
	cd ..

images:
	docker build -t arman/wallet-service ./Wallet
	docker build -t arman/restaurant-service ./Restaurant
	docker images

containers:
	docker run -d -p 8082:8080 --rm --name wallet -v "$(shell pwd)/Wallet/initialData.txt:/initialData.txt" arman/wallet-service
	docker run -d -p 8080:8080 --rm --name restaurant -v "$(shell pwd)/Restaurant/initialData.txt:/initialData.txt" arman/restaurant-service

# run: containers
# 	cd Delivery && \
# 	mvn exec:java -Dexec.args="$(shell pwd)/initialData.txt"

stop:
	docker stop wallet restaurant

clean:
	docker rmi -f arman/wallet-service arman/restaurant-service &&\
	cd Delivery && \
	mvn clean && \
	cd .. && \
	cd Restaurant && \
	./mvnw clean && \
	cd .. && \
	cd Wallet && \
	./mvnw clean && \
	cd ..