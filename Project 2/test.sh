curl -X POST localhost:8081/agentSignIn -H 'Content-type:application/json'  -d '{"agentId": 201}'
curl -X GET localhost:8081/agent/201
curl -X POST localhost:8081/agentSignOut -H 'Content-type:application/json'  -d '{"agentId": 201}'
curl -X GET localhost:8081/agent/201