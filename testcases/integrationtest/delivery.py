# /usr/bin/env python3

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

from sqlalchemy import null
sys.path.append('..')
sys.path.append('../unittest')
import wallet
import restaurant
from utils import *

'''
test case for reinitialize
@param t: test case number
'''
def testReinitialize(t):
    print(str(t) + ".\tdelivery request\t- post /reInitialize\t\t", end="")
    try:
        response = requests.post(delivery_url + "/reInitialize")
        if response.status_code == 201:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()

'''
test case for get agent details
@param t - test case number
@param agentId - agent id
@param agentStatus - agent status
@param statuscode - http status code
'''
def testGetAgent(t, agentId, agentStatus, statuscode):
    print(str(t) + ".\tdelivery request\t- get /agent/" + str(agentId) + "\t\t", end="")
    try:
        response = requests.get(delivery_url + "/agent/" + str(agentId))
        if response.status_code == statuscode and response.json()["agentId"] == agentId and response.json()["status"] == agentStatus:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()

'''
test cases for agent sign in
@param t - test case number
@param agentId - agent id
@param statuscode - http status code
'''
def testAgentSignIn(t, agentId, statuscode):
    print(str(t) + ".\tdelivery request\t- post /agentSignIn\t\t", end="")
    try:
        response = requests.post(
            delivery_url + "/agentSignIn", json={"agentId": agentId})
        if response.status_code == statuscode:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()


'''
test cases for agent sign out
@param t - test case number
@param agentId - agent id
@param statuscode - http status code
'''
def testAgentSignOut(t, agentId, statuscode):
    print(str(t) + ".\tdelivery request\t- post /agentSignOut\t\t", end="")
    try:
        response = requests.post(
            delivery_url + "/agentSignOut", json={"agentId": agentId})
        if response.status_code == statuscode:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()

'''
test case for request order
@param t - test case number
@param custId - customer id
@param restId - restaurant id
@param itemId - item id
@param qty - quantity
@param statuscode - http status code
'''
def testRequestOrder(t, custId, restId, itemId, qty, statuscode):
    print(str(t) + ".\tdelivery request\t- post /requestOrder\t\t", end="")
    try:
        response = requests.post(delivery_url + "/requestOrder", json={
                                 "custId": custId, "restId": restId, "itemId": itemId, "qty": qty})
        if response.status_code == statuscode:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()

'''
test case for order delivered
@param t - test case number
@param orderId - order id
@param statuscode - http status code
'''
def testOrderDelivered(t, orderId, statuscode):
    print(str(t) + ".\tdelivery request\t- post /orderDelivered\t\t", end="")
    try:
        response = requests.post(
            delivery_url + "/orderDelivered", json={"orderId": orderId})
        if response.status_code == statuscode:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()

'''
test case for get order details
@param t - test case number
@param orderId - order id
@param status - order status
@param agentId - agent id
@param statuscode - http status code
'''
def testGetOrder(t, orderId, status, agentId, statuscode):
    print(str(t) + ".\tdelivery request\t- get /order/" + str(orderId) + "\t\t", end="")
    try:
        response = requests.get(delivery_url + "/order/" + str(orderId))
        if response.status_code == statuscode:
            if (status is not None):
                if (response.json()["status"] == status and response.json()["agentId"] == agentId and response.json()["orderId"] == orderId):
                    test_pass()
                else:
                    test_fail()
            else:
                test_pass()
        else:
            test_fail()
    except:
        test_fail()

if __name__ == "__main__":
    # **********************************************************
    restaurants = []

    restaurants.append(Restaurants(101))
    restaurants.append(Restaurants(102))

    restaurants[0].addItem(1, 180, 10)
    restaurants[0].addItem(2, 230, 20)

    restaurants[1].addItem(1, 50, 10)
    restaurants[1].addItem(3, 60, 20)
    restaurants[1].addItem(4, 45, 15)
    # **********************************************************

    deliveryAgents = [201, 202, 203]
    # **********************************************************

    customers = [301, 302, 303]
    # **********************************************************

    print_box("\033[93mDelivery Service API Test\033[1;32;40m")

    # ---------------------  /reInitialize  --------------------
    testReinitialize(1)

    # ---------------------  /getAgent  ------------------------
    testGetAgent(2, 201, "signed-out", 200)
    testGetAgent(3, 202, "signed-out", 200)
    testGetAgent(4, 203, "signed-out", 200)

    # ---------------------  /agentSignIn  ---------------------
    testAgentSignIn(5, 201, 201)
    testAgentSignIn(6, 202, 201)
    testAgentSignIn(7, 203, 201)

    # ---------------------  /getAgent  ------------------------
    testGetAgent(8, 201, "available", 200)
    testGetAgent(9, 202, "available", 200)
    testGetAgent(10, 203, "available", 200)

    # ---------------------  /agentSignOut  --------------------
    testAgentSignOut(11, 201, 201)
    testAgentSignOut(12, 202, 201)
    testAgentSignOut(13, 203, 201)

    # ---------------------  /getAgent  ------------------------
    testGetAgent(14, 201, "signed-out", 200)
    testGetAgent(15, 202, "signed-out", 200)
    testGetAgent(16, 203, "signed-out", 200)

    # ---------------------  /reInitialize  --------------------
    testReinitialize(17)
    wallet.testReinitialize(18)
    restaurant.testReinitialize(19)

    # ---------------------  /requestOrder  --------------------
    testRequestOrder(18, 301, 101, 2, 10, 410)
    testRequestOrder(19, 302, 101, 2, 11, 410)
    # order = [   {"restId": 101, "itemId": 1, "qty": 10},
    #             {"restId": 101, "itemId": 2, "qty": 20},
    #             {"restId": 102, "itemId": 1, "qty": 10},
    #             {"restId": 102, "itemId": 3, "qty": 20},
    #             {"restId": 102, "itemId": 4, "qty": 15}]

    # ---------------------  /addBalance  ----------------------
    wallet.testAddBalance(20, 301, 300, 201)

    # ---------------------  /balance  -------------------------
    wallet.testGetBalance(21, 301, 2300, 200)

    # ---------------------  /requestOrder  --------------------
    testRequestOrder(22, 301, 101, 2, 10, 201)
    testRequestOrder(23, 302, 101, 2, 11, 410)

    # ---------------------  /getOrder  ------------------------
    testGetOrder(24, 1000, "unassigned", -1, 200)
    testAgentSignIn(25, 201, 201)
    testGetOrder(26, 1000, "assigned", 201, 200)