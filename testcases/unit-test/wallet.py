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

customer_ids = [301, 302, 303]

print_box("\033[93mWallet Service API Test\033[1;32;40m")

def testGetBalance(t, balance):
    for i in range(len(customer_ids)):
        print(str(t+i) +".\trequest\t- get /balance/" + str(customer_ids[i]) + "\t\t", end="")
        try:
            response = requests.get(wallet_url + "/balance/" + str(customer_ids[i]))
            if response.status_code == 200 and response.json()["custId"] == customer_ids[i] and response.json()["amount"] == balance:
                test_pass()
            else:
                test_fail()
        except:
            test_fail()

def testReinitialize(t):
    print(str(t) + ".\trequest\t- post /reInitialize\t\t", end="")
    try:
        response = requests.post(wallet_url + "/reInitialize")
        if response.status_code == 201:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()

# ---------------------  /reInitialize  ---------------------
testReinitialize(1)

# ---------------------  /Balance  ---------------------
testGetBalance(2, 2000)

# ---------------------  /addBalance  ---------------------
for i in range(len(customer_ids)):
    print(str(5+i) +".\trequest\t- post /addBalance-"+ str(customer_ids[i]) +"\t\t", end="")
    try:
        response = requests.post(wallet_url + "/addBalance", json={"custId": customer_ids[i], "amount": 100})
        if response.status_code == 201:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()

# ---------------------  /Balance  ---------------------
testGetBalance(8, 2100)

# ---------------------  /deductBalance  ---------------------
for i in range(len(customer_ids)):
    print(str(11+i) +".\trequest\t- post /deductBalance-"+ str(customer_ids[i]) +"\t", end="")
    try:
        response = requests.post(wallet_url + "/deductBalance", json={"custId": customer_ids[i], "amount": 200})
        if response.status_code == 201:
            test_pass()
        else:
            test_fail()
    except:
        test_fail()

# ---------------------  /Balance  ---------------------
testGetBalance(14, 1900)

# ---------------------  /reInitialize  ---------------------
testReinitialize(17)

# ---------------------  /Balance  ---------------------
testGetBalance(18, 2000)