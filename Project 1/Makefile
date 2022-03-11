jars:
	cd Wallet && ./mvnw -DskipTests package && \
	cd ../Delivery && ./mvnw -DskipTests package && \
	cd ../Restaurant && ./mvnw -DskipTests package && \
	cd ..

images:
	cp $$HOME/Downloads/initialData.txt Wallet
	cp $$HOME/Downloads/initialData.txt Delivery
	cp $$HOME/Downloads/initialData.txt Restaurant
	@eval $$(minikube docker-env) ; \
	docker build -t arman/wallet-service ./Wallet ;\
	docker build -t arman/restaurant-service ./Restaurant ;\
	docker build -t arman/delivery-service ./Delivery ; \
	docker images

install_metrics_server:
	minikube kubectl -- apply -f ./Kubernetes/metrics-server.yml

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
	minikube kubectl -- create service loadbalancer arman-delivery-service --tcp=8080:8080
	minikube kubectl -- create service loadbalancer arman-wallet-service --tcp=8080:8080
	minikube kubectl -- create service loadbalancer arman-restaurant-service --tcp=8080:8080

# autoscale:
# 	minikube kubectl -- autoscale deployment delivery-service --cpu-percent=50 --min=1 --max=3

port-forwards:
	minikube kubectl -- port-forward service/arman-restaurant-service 8080:8080 &
	minikube kubectl -- port-forward service/arman-delivery-service 8081:8080 &
	minikube kubectl -- port-forward service/arman-wallet-service 8082:8080 &
# stop:
# 	docker stop wallet restaurant delivery

clean:
	minikube kubectl -- delete -n default hpa arman-delivery-hpa
	minikube kubectl -- delete -n default deployment arman-delivery-service
	minikube kubectl -- delete -n default deployment arman-wallet-service
	minikube kubectl -- delete -n default deployment arman-restaurant-service
	minikube kubectl -- delete -n default service arman-delivery-service
	minikube kubectl -- delete -n default service arman-wallet-service
	minikube kubectl -- delete -n default service arman-restaurant-service 
	minikube kubectl -- delete -n default deployment arman-mysql
	minikube kubectl -- delete -n default service arman-mysql
	cd Wallet && ./mvnw clean && \
	cd ../Delivery && ./mvnw clean && \
	cd ../Restaurant && ./mvnw clean && \
	cd .. ;\
	ps aux|grep port-forward|awk '{print $$2}'| xargs kill ;\
	@eval $$(minikube docker-env) ;\
	docker rmi -f arman/wallet-service ;\
	docker rmi -f arman/restaurant-service ;\
	docker rmi -f arman/delivery-service
