make jars
make images
make deployments
make services

sleep 5

minikube kubectl -- port-forward service/restaurant-service 8080:8080 &
minikube kubectl -- port-forward service/delivery-service 8081:8080 &
minikube kubectl -- port-forward service/wallet-service 8082:8080 &

