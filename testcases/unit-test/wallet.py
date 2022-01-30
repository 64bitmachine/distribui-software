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
import json
import sys
sys.path.append('..')

from utils import wallet_url, test_pass, test_fail

print("Wallet Service API Test")

# ---------------------  /reInitialize  ---------------------
print("\n1.  request      - post /reInitialize\t", end="")
try:
    response = requests.post(wallet_url + "/reInitialize")
    if response.status_code == 201:
        test_pass()
    else:
        test_fail()
except:
    test_fail()

# ---------------------  /Balance  ---------------------
print("2.  request      - get /balance/301\t", end="")
try:
    response = requests.get(wallet_url + "/balance/301")
    json_data = json.loads(response.text)
    if response.status_code == 200 and json_data["custId"] == 301 and json_data["amount"] == 2000:
        test_pass()
    else:
        test_fail()
except:
    test_fail()