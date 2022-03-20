clear
curl -X POST localhost:8081/agentSignIn -H 'Content-type:application/json'  -d '{"agentId": 201}'
echo ""
curl -X GET localhost:8081/agent/201
echo ""
curl -X POST localhost:8081/agentSignOut -H 'Content-type:application/json'  -d '{"agentId": 201}'
echo ""
curl -X GET localhost:8081/agent/201
echo ""
curl -X POST localhost:8081/agentSignIn -H 'Content-type:application/json'  -d '{"agentId": 201}'
curl -X POST localhost:8081/agentSignIn -H 'Content-type:application/json'  -d '{"agentId": 202}'
echo ""
curl -X GET localhost:8081/agent/201
curl -X GET localhost:8081/agent/202
echo ""
curl -X POST localhost:8081/reInitialize
echo ""
curl -X GET localhost:8081/agent/201
curl -X GET localhost:8081/agent/202
curl -X GET localhost:8081/agent/203
echo ""