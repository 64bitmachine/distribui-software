jars:
	cd Wallet && ./mvnw package && \
	cd ../Delivery && ./mvnw -DskipTests package && \
	cd ../Restaurant && ./mvnw package && \
	cd ..

images:
	cp $$HOME/Downloads/initialData.txt Wallet
	cp $$HOME/Downloads/initialData.txt Delivery
	cp $$HOME/Downloads/initialData.txt Restaurant
	@eval $$(minikube docker-env) ; \
	docker build -t dist/wallet-service ./Wallet ;\
	docker build -t dist/restaurant-service ./Restaurant ;\
	docker build -t dist/delivery-service ./Delivery ; \
	docker images

# containers:
# 	docker run -d -p 8082:8080 --rm --name wallet --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt wallet-service
# 	docker run -d -p 8080:8080 --rm --name restaurant --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt restaurant-service
# 	docker run -d -p 8081:8080 --rm --name delivery --add-host=host.docker.internal:host-gateway -v ~/Downloads/initialData.txt:/initialData.txt delivery-service

deployments:
#   docker run --name delivery-db --add-host=host.docker.internal:host-gateway -p 3306 -e MYSQL_ROOT_PASSWORD=root -d mysql:latest
	minikube kubectl -- apply -f ./Database/db.yml
	minikube kubectl -- apply -f ./Kubernetes/delivery.yml
	minikube kubectl -- apply -f ./Kubernetes/restaurant.yml
	minikube kubectl -- apply -f ./Kubernetes/wallet.yml
services:
	minikube kubectl -- create service loadbalancer delivery-service --tcp=8080:8080
	minikube kubectl -- create service loadbalancer wallet-service --tcp=8080:8080
	minikube kubectl -- create service loadbalancer restaurant-service --tcp=8080:8080
	
# stop:
# 	docker stop wallet restaurant delivery

clean:
	minikube kubectl -- delete -n default deployment delivery-service
	minikube kubectl -- delete -n default deployment wallet-service
	minikube kubectl -- delete -n default deployment restaurant-service
	minikube kubectl -- delete -n default service delivery-service
	minikube kubectl -- delete -n default service wallet-service
	minikube kubectl -- delete -n default service restaurant-service 
	minikube kubectl -- delete -n default deployment mysql
	minikube kubectl -- delete -n default service mysql 
	docker rmi -f dist/wallet-service
	docker rmi -f dist/restaurant-service
	docker rmi -f dist/delivery-service
	cd Wallet && ./mvnw clean && \
	cd ../Delivery && ./mvnw clean && \
	cd ../Restaurant && ./mvnw clean && \
	cd ..
