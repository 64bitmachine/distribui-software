#/usr/bin/env python3

'''
test cases for wallet service api

1.  request      - post /addBalance
    requestbody  - {"custId": num, "amount": 100}
    response     - http code 201 

2.  request      - post /reInitialize
    response     - http code 201

3.  request      - get /balance/num
    response     - http code 200
                 - {"custId": num, "balance": 100}

4.  request      - post /deductBalance
    requestbody  - {"custId": num, "amount": 100}
    response     - http code 201 if success
                 - http code 410 if fail
'''

import requests
import sys
sys.path.append('..')

from utils import *

'''
test case for balance retrieval
@param t: test case number
@param custId: customer id
@param balance: expected balance
@param statuscode: expected http status code
'''
def testGetBalance(t, custId, balance, statuscode):
    print(str(t) +".\twallet request\t\t- get /balance/" + str(custId) + "\t\t", end="")
    try:
        response = requests.get(wallet_url + "/balance/" + str(custId))
        if response.status_code == statuscode and response.json()["custId"] == custId and response.json()["amount"] == balance:
            test_pass()
        else:
            test_fail()
            print(response.json())
    except:
        test_fail()

'''
test case for reinitialize
'''
def testReinitialize(t):
    print(str(t) + ".\twallet request\t\t- post /reInitialize\t\t", end="")
    try:
        response = requests.post(wallet_url + "/reInitialize")
        if response.status_code == 201:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()

'''
test case for adding balance
@param t: test case number
@param custId: customer id
@param amount: amount to be added
@param statuscode: expected http code
'''
def testAddBalance(t, custId, amount, statuscode):
    print(str(t) +".\twallet request\t\t- post /addBalance-"+ str(custId) +"\t\t", end="")
    try:
        response = requests.post(wallet_url + "/addBalance", json={"custId": custId, "amount": amount})
        if response.status_code == statuscode:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()

'''
test case for deducting balance
@param t: test case number
@param custId: customer id
@param amount: amount to be deducted
@param statuscode: expected http code
'''
def testDeductBalance(t, custId, amount, statuscode):
    print(str(t) +".\twallet request\t\t- post /deductBalance-"+ str(custId) +"\t", end="")
    try:
        response = requests.post(wallet_url + "/deductBalance", json={"custId": custId, "amount": amount})
        if response.status_code == statuscode:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()

if __name__ == "__main__":
    customer_ids = [301, 302, 303]

    print_box("\033[93mWallet Service API Test\033[1;32;40m")

    # ---------------------  /reInitialize  ---------------------
    testReinitialize(1)

    # ---------------------  /Balance  ---------------------
    testGetBalance(2, 301, 2000, 200)
    testGetBalance(3, 302, 2000, 200)
    testGetBalance(4, 303, 2000, 200)

    # ---------------------  /addBalance  ---------------------
    testAddBalance(5, 301, 100, 201)
    testAddBalance(6, 302, 50, 201)
    testAddBalance(7, 303, 100, 201)

    # ---------------------  /Balance  ---------------------
    testGetBalance(8, 301, 2100, 200)
    testGetBalance(9, 302, 2050, 200)
    testGetBalance(10, 303, 2100, 200)

    # ---------------------  /deductBalance  ---------------------
    testDeductBalance(11, 301, 100, 201)
    testDeductBalance(12, 302, 50, 201)
    testDeductBalance(13, 303, 100, 201)

    # ---------------------  /Balance  ---------------------
    testGetBalance(14, 301, 2000, 200)
    testGetBalance(15, 302, 2000, 200)
    testGetBalance(16, 303, 2000, 200)

    # ---------------------  /deductBalance  ---------------------
    testDeductBalance(17, 301, 2100, 410)
    testDeductBalance(18, 302, 5000, 410)
    testDeductBalance(19, 303, 10000, 410)

    # ---------------------  /reInitialize  ---------------------
    testReinitialize(17)

    # ---------------------  /Balance  ---------------------
    testGetBalance(18, 301, 2000, 200)
    testGetBalance(19, 302, 2000, 200)
    testGetBalance(20, 303, 2000, 200)