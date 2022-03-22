clear

curl -X POST localhost:8080/reInitialize
curl -X POST localhost:8082/reInitialize

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
curl -X POST localhost:8081/requestOrder -H 'Content-type:application/json'  -d '{"custId": 301, "restId": 101, "itemId": 1, "qty": 1}'
echo ""
sleep 1
curl -X GET localhost:8082/balance/301
curl -X GET localhost:8082/balance/302
echo ""
curl -X GET localhost:8081/order/1000
echo ""
curl -X GET localhost:8081/order/1002
echo ""