# /usr/bin/env python3

import concurrent.futures
from asyncio import futures
import sys
import random
import threading

sys.path.append('..')
sys.path.append('../unittest')
sys.path.append('../integrationtest')
from utils import *
import delivery
import restaurant
import wallet
import queue

# Global variable containing the restaurant and food item metadata
MenuOrder = [{"restId": 101, "itemId": 1, "qty": 10, "price": 180},
             {"restId": 101, "itemId": 2, "qty": 20, "price": 230},
             {"restId": 102, "itemId": 1, "qty": 10, "price": 50},
             {"restId": 102, "itemId": 3, "qty": 20, "price": 60},
             {"restId": 102, "itemId": 4, "qty": 15, "price": 45}]

'''
this function is used to test the concurrency of the wallet service
by adding and withdrawing money from the wallet
'''
def wallettest(custId):
    balance = 2000
    for i in range(100):
        # 50% chance of adding money
        amount = random.randint(0, 100)
        if random.randint(0, 1) == 0:

            # adds money to wallet

            balance += amount
            wallet.testAddBalance(i, custId, amount, 201)
        else:

            # withdraws money from wallet

            balance -= amount
            wallet.testDeductBalance(i, custId, amount, 201)
        wallet.testGetBalance(i, custId, balance, 200)

'''
this function is used to test the concurrency of the restaurant service
by requesting/accepting and refilling food items
'''
def restauranttest(restId):
    global MenuOrder
    myOrder = []

    for i in range(len(MenuOrder)):
        if MenuOrder[i]["restId"] == restId:
            myOrder.append({"restId": MenuOrder[i]["restId"],
                            "itemId": MenuOrder[i]["itemId"], "qty": MenuOrder[i]["qty"]})

    itemCount = []
    # initializing the item count
    if restId == 101:
        itemCount = [10, 20]
    else:
        itemCount = [10, 20, 15]

    for j in range(100):
        for i in range(len(myOrder)):
            orderItem = random.randint(0, 10)
            if random.randint(0, 1) == 0:

                # order food

                myOrder[i]["qty"] = orderItem
                if orderItem > itemCount[i] or orderItem == 0:
                    restaurant.testOrder(j, [myOrder[i]], "acceptOrder", 410)
                else:
                    restaurant.testOrder(j, [myOrder[i]], "acceptOrder", 201)
                    itemCount[i] -= orderItem
            else:

                # refill item
                myOrder[i]["qty"] = orderItem
                itemCount[i] += orderItem
                restaurant.testOrder(j, [myOrder[i]], "refillItem", 201)

'''
This function is used to test the concurrency of the delivery service.
By requesting/accepting and delivering food items along with updating the wallet balance
Locks are used to ensure that same item cannot be ordered at the same time but different
orders corresponding to different items can be placed at the same time
'''
def deliverytest(custId, locks):
    global MenuOrder
    money = 2000

    for i in range(100):

        amount = random.randint(0, 100)
        orderQty = random.randint(0, 10)
        orderItem = random.randint(0, len(MenuOrder) - 1)

        if random.randint(0, 1) == 0:
            if orderQty > 0:
                locks[orderItem].acquire()
                if (orderQty <= MenuOrder[orderItem]["qty"]) and ((MenuOrder[orderItem]["price"] * orderQty) <= money):
                    
                    # order food
                    
                    delivery.testRequestOrder(
                        i, custId, MenuOrder[orderItem]["restId"], MenuOrder[orderItem]["itemId"], orderQty, 201, -1)
                    MenuOrder[orderItem]["qty"] -= orderQty
                    money -= (MenuOrder[orderItem]["price"] * orderQty)
                else:
                    # cannot order food
                    delivery.testRequestOrder(
                        i, custId, MenuOrder[orderItem]["restId"], MenuOrder[orderItem]["itemId"], orderQty, 201, -1)
                    MenuOrder[orderItem]["qty"] += orderQty
                    restaurant.testOrder(i, [{"restId": MenuOrder[orderItem]["restId"],
                                         "itemId": MenuOrder[orderItem]["itemId"], "qty": orderQty}], "refillItem", 201)
                    wallet.testAddBalance(i, custId, amount, 201)
                    money += amount
                locks[orderItem].release()

if __name__ == '__main__':

    print_box("\033[93mLoad Testing (Project 1 Phase 2) \033[1;32;40m")

    # # create a thread pool for testing concurrency of wallet and restaurant services
    # with concurrent.futures.ThreadPoolExecutor() as executor:
    #     wallet.testReinitialize(1)
    #     restaurant.testReinitialize(2)
    #     futures = []
    #     for cid in [301, 302, 303]:
    #         futures.append(executor.submit(wallettest, custId=cid))

    #     for rid in [101, 102]:
    #         futures.append(executor.submit(restauranttest, restId=rid))

    #     for future in concurrent.futures.as_completed(futures):
    #         future.result()

    # create a thread pool for testing concurrency of delivery service
    # requests new order and also marks the order as delivered by the agent
    with concurrent.futures.ThreadPoolExecutor() as executor:
        wallet.testReinitialize(1)
        restaurant.testReinitialize(2)
        delivery.testReinitialize(3)
        futures = []

        locks = []
        for i in range(len(MenuOrder)):
            locks.append(threading.Lock())

        q = queue.Queue()

        for cid in [301, 302, 303]:
            futures.append(executor.submit(
                deliverytest, custId=cid, locks=locks))

        for future in concurrent.futures.as_completed(futures):
            future.result()

    # --------- test case 2 ------------
    # --------- 50 threads deducing 40 rupees resulting in 0(40*50) amount ------------------------------
    # wallet.testReinitialize(1)
    # restaurant.testReinitialize(2)
    # delivery.testReinitialize(3)
    # threads = []

    # for i in range(50):

    #     threads.append(threading.Thread(target=wallet.testDeductBalance, args=(11, 301, 40, 201,)))

    # for i in range(50):
    #     threads[i].start()

    # for i in range(50):
    #     threads[i].join()

    # res1 = wallet.testGetBalance(7, 301, 101, 144)
    # # print(res1.json()["amount"])

    # if res1.json()["balance"] == 0:
    #     test_pass()

    # print_box("\033[93mPublic Testcases (Project 1 Phase 2) \033[1;32;40m")