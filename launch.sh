minikube start

# compile the project
make jars

# build the docker images
make images

# metric server
make install_metrics_server

# deploy the project
make deployments

# deploy the service
make services

# wait for the service to be ready
echo "Waiting for the service to be ready "
for i in {1..10}; do
    echo .
    sleep 1
done

# port
make port-forwards