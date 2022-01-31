#/usr/bin/env python3

'''
test cases for delivery service api

1.  request      - post /requestOrder
    requestbody  - {"custId": num, "restId": x, "itemId": y, "qty": z}
    response     - http code 201 if success { "orderId": num }
                 - http code 410 if fail

2.  request      - post /agentSignIn
    requestbody  - {"agentId": num}
    response     - http code 201

3.  request      - post /agentSignOut
    requestbody  - {"agentId": num}
    response     - http code 201

4.  request      - post /orderDelivered
    requestbody  - {"orderId": num}
    response     - http code 201

5.  request      - get /order/num
    response     - http code 404 if no order
                 - http code 200 if has order
                 - {"orderId":num, "status": x, "agentId": y}

6.  request      - get /agent/num
    response     - http code 200
                 - {"agentId": num, "status": y}

7.  request      - post /reInitialize
    response     - http code 201
'''

import requests
import sys
sys.path.append('..')

from utils import *

# **********************************************************************************************************************
restaurants = []

restaurants.append(Restaurants(101))
restaurants.append(Restaurants(102))

restaurants[0].addItem(1, 180, 10)
restaurants[0].addItem(2, 230, 20)

restaurants[1].addItem(1, 50, 10)
restaurants[1].addItem(3, 60, 20)
restaurants[1].addItem(4, 45, 15)
# **********************************************************************************************************************

deliveryAgents = [201, 202, 203]
# **********************************************************************************************************************

customers = [301, 302, 303]
# **********************************************************************************************************************

print_box("\033[93mDelivery Service API Test\033[1;32;40m")

def testReinitialize(t):
    print(str(t) + ".\trequest\t- post /reInitialize\t\t", end="")
    try:
        response = requests.post(delivery_url + "/reInitialize")
        if response.status_code == 201:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()

def testGetAgent(t):
    for i in range(len(deliveryAgents)):
        print(str(i+t) +".\trequest\t- get /agent/" + str(deliveryAgents[i]) + "\t\t", end="")
        try:
            response = requests.get(delivery_url + "/agent/" + str(deliveryAgents[i]))
            if response.status_code == 200 and response.json()["agentId"] == deliveryAgents[i] and response.json()["status"] == "SIGNED_OUT":
                test_pass()
            else:
                test_fail()
        except:
            test_fail()

'''
test cases for agent sign in
'''
def testAgentSignIn(t):
    for i in range(len(deliveryAgents)):
        print(str(i+t) +".\trequest\t- post /agentSignIn\t\t", end="")
        try:
            response = requests.post(delivery_url + "/agentSignIn", json={"agentId": deliveryAgents[i]})
            if response.status_code == 201:
                test_pass()
            else:
                test_fail()
        except:
            test_fail()

'''
test cases for agent sign out
'''
def testAgentSignOut(t):
    for i in range(len(deliveryAgents)):
        print(str(i+t) +".\trequest\t- post /agentSignOut\t\t", end="")
        try:
            response = requests.post(delivery_url + "/agentSignOut", json={"agentId": deliveryAgents[i]})
            if response.status_code == 201:
                test_pass()
            else:
                test_fail()
        except:
            test_fail()

# TODO: test for order delivered
# TODO: test for request order
# TODO: test for get order

# ---------------------  /reInitialize  ---------------------
testReinitialize(1)

# ---------------------  /getAgent  ---------------------
testGetAgent(2)

# ---------------------  /agentSignIn  ---------------------
testAgentSignIn(5)

# ---------------------  /agentSignOut  ---------------------
testAgentSignOut(8)