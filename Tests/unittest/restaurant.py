#/usr/bin/env python3

'''
test cases for restaurant service api

1.  request      - post /acceptOrder
    requestbody  - {"restId": num, "itemId": x, "qty": y}
    response     - http code 201 if success
                 - http code 410 if fail

2.  request      - post /reInitialize
    response     - http code 201

3.  request      - post /refillItem
    requestbody  - {"restId": num, "itemId": x, "qty": y}
    response     - http code 201
'''

import requests
import sys
sys.path.append('..')

from utils import *

'''
test case for reinitialize
@param t: test case number
'''
def testReinitialize(t):
    print(str(t) + ".\trestaurant request\t- post /reInitialize\t\t", end="")
    try:
        response = requests.post(restaurant_url + "/reInitialize")
        if response.status_code == 201:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()

'''
test case for order
@param t: test case number
@param order: order data list
@param type: "acceptOrder" or "refillItem"
@param status: expected http code 201
'''
def testOrder(t, order, type, status):
    for i in range(len(order)):
        print(str(i+t) +".\trestaurant request\t- post /" + type +"\t\t", end="")
        try:
            response = requests.post(restaurant_url + "/" + type, json=order[i])
            if response.status_code == status:
                test_pass()
            else:
                print(order, type, response.status_code)
                test_fail()
        except:
            test_fail()

if __name__ == "__main__":

    restaurants = []

    restaurants.append(Restaurants(101))
    restaurants.append(Restaurants(102))

    restaurants[0].addItem(1, 180, 10)
    restaurants[0].addItem(2, 230, 20)

    restaurants[1].addItem(1, 50, 10)
    restaurants[1].addItem(3, 60, 20)
    restaurants[1].addItem(4, 45, 15)

    print_box("\033[93mRestaurant Service API Test\033[1;32;40m")

    # ---------------------  /reInitialize  ---------------------
    testReinitialize(1)

    # ---------------------  /acceptOrder  ---------------------
    order = [   {"restId": 101, "itemId": 1, "qty": 10},
                {"restId": 101, "itemId": 2, "qty": 20},
                {"restId": 102, "itemId": 1, "qty": 10},
                {"restId": 102, "itemId": 3, "qty": 20},
                {"restId": 102, "itemId": 4, "qty": 15}]

    testOrder(2, order, "acceptOrder", 201)

    # ---------------------  /acceptOrder  ---------------------
    order = [   {"restId": 101, "itemId": 1, "qty": 10},
                {"restId": 101, "itemId": 2, "qty": 20},
                {"restId": 102, "itemId": 1, "qty": 10},
                {"restId": 102, "itemId": 3, "qty": 20},
                {"restId": 102, "itemId": 4, "qty": 15}]

    testOrder(7, order, "acceptOrder", 410)

    # ---------------------  /reInitialize  ---------------------
    testReinitialize(12)

    # ---------------------  /refillItem  ---------------------
    order = [   {"restId": 101, "itemId": 1, "qty": 5},
                {"restId": 101, "itemId": 2, "qty": 10},
                {"restId": 102, "itemId": 1, "qty": 15},
                {"restId": 102, "itemId": 3, "qty": 29},
                {"restId": 102, "itemId": 4, "qty": 16}]

    testOrder(13, order, "refillItem", 201)

    # ---------------------  /acceptOrder  ---------------------
    order = [   {"restId": 101, "itemId": 1, "qty": 15},
                {"restId": 101, "itemId": 2, "qty": 30},
                {"restId": 102, "itemId": 1, "qty": 25},
                {"restId": 102, "itemId": 3, "qty": 49},
                {"restId": 102, "itemId": 4, "qty": 31}]
        
    testOrder(18, order, "acceptOrder", 201)

    # ---------------------  /acceptOrder  ---------------------
    order = [   {"restId": 101, "itemId": 1, "qty": 0},
                {"restId": 101, "itemId": 2, "qty": 0},
                {"restId": 102, "itemId": 1, "qty": 0},
                {"restId": 102, "itemId": 3, "qty": 0},
                {"restId": 102, "itemId": 4, "qty": 0}]
    
    testOrder(18, order, "acceptOrder", 410)
