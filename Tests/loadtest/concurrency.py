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


counterLock = threading.Lock()
OrderIds  = 1000

# this global variable is used to keep track of the order queue
# contains the order ids like 1000, 1001, 1002, etc
orderQueue = queue.Queue()

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
    global OrderIds, MenuOrder
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
                        i, custId, MenuOrder[orderItem]["restId"], MenuOrder[orderItem]["itemId"], orderQty, 201)
                    MenuOrder[orderItem]["qty"] -= orderQty
                    money -= (MenuOrder[orderItem]["price"] * orderQty)
                    with counterLock:
                        orderQueue.put(OrderIds)
                        OrderIds += 1
                else:

                    # cannot order food

                    delivery.testRequestOrder(
                        i, custId, MenuOrder[orderItem]["restId"], MenuOrder[orderItem]["itemId"], orderQty, 410)
                    MenuOrder[orderItem]["qty"] += orderQty
                    restaurant.testOrder(i, [{"restId": MenuOrder[orderItem]["restId"],
                                         "itemId": MenuOrder[orderItem]["itemId"], "qty": orderQty}], "refillItem", 201)
                    wallet.testAddBalance(i, custId, amount, 201)
                    money += amount
                locks[orderItem].release()
        else:
            if not orderQueue.empty():

                # order delivered

                delivery.testOrderDelivered(81, orderQueue.get(), 201)
                orderQueue.task_done()

'''
this function is used to test the concurrency of the delivery service
by agents signing in
'''
def deliveryAgentTest(agentId):
    for i in range(100):
        delivery.testAgentSignIn(i, agentId, 201)

if __name__ == '__main__':

    print_box("\033[93mLoad Testing (Project 1 Phase 2) \033[1;32;40m")

    # create a thread pool for testing concurrency of wallet and restaurant services
    with concurrent.futures.ThreadPoolExecutor() as executor:
        wallet.testReinitialize(1)
        restaurant.testReinitialize(2)
        futures = []
        for cid in [301, 302, 303]:
            futures.append(executor.submit(wallettest, custId=cid))

        for rid in [101, 102]:
            futures.append(executor.submit(restauranttest, restId=rid))

        for future in concurrent.futures.as_completed(futures):
            future.result()

    # create a thread pool for testing concurrency of delivery service
    # requests new order and also marks the order as delivered by the agent
    with concurrent.futures.ThreadPoolExecutor() as executor:
        wallet.testReinitialize(1)
        restaurant.testReinitialize(2)
        delivery.testReinitialize(3)
        futures = []

        for delAgent in [201, 202, 203]:
            delivery.testAgentSignIn(4, delAgent, 201)

        locks = []
        for i in range(len(MenuOrder)):
            locks.append(threading.Lock())

        q = queue.Queue()

        for cid in [301, 302, 303]:
            futures.append(executor.submit(
                deliverytest, custId=cid, locks=locks))

        for future in concurrent.futures.as_completed(futures):
            future.result()

        print("total orders placed: " + str(OrderIds))

    # loading testing delivery service with the help of agents signin
    for i in range(10):
        with concurrent.futures.ThreadPoolExecutor() as executor:
            wallet.testReinitialize(1)
            restaurant.testReinitialize(2)
            delivery.testReinitialize(3)
            futures = []

            # random number of threads 1 - 50
            numThreads = random.randint(1, 50)
            print("number of threads: " + str(numThreads))
            while numThreads > 0:
                for aid in [201, 202, 203]:
                    futures.append(executor.submit(
                        deliveryAgentTest, agentId=aid))
                    numThreads -= 1

            for future in concurrent.futures.as_completed(futures):
                future.result()
    
    # --------- test case 1 ------------
    # --------- three agents sign in and 3 orders are placed ------------------------------
    # --------- and checking that order id and agent mapping is correct ----------------------
    wallet.testReinitialize(1)
    restaurant.testReinitialize(2)
    delivery.testReinitialize(3)

    delivery.testAgentSignIn(4, 201, 201)
    delivery.testAgentSignIn(5, 202, 201)
    delivery.testAgentSignIn(6, 203, 201)

    thread1 = threading.Thread(target=delivery.testRequestOrder, args=(4, 301, 101, 2, 1, 201,))
    thread2 = threading.Thread(target=delivery.testRequestOrder, args=(5, 302, 101, 2, 1, 201,))
    thread3 = threading.Thread(target=delivery.testRequestOrder, args=(6, 303, 101, 2, 1, 201,))

    thread1.start()
    thread2.start()
    thread3.start()

    thread1.join()
    thread2.join()
    thread3.join()

    delivery.testGetOrder(51, 1000, "assigned", 201, 200)
    delivery.testGetOrder(56, 1001, "assigned", 202, 200)
    delivery.testGetOrder(57, 1002, "assigned", 203, 200)

    thread4  = threading.Thread(target=delivery.testOrderDelivered, args=(81, 1000, 201,))
    thread5  = threading.Thread(target=delivery.testOrderDelivered, args=(82, 1001, 201,))
    thread6  = threading.Thread(target=delivery.testRequestOrder, args=(7, 301, 101, 2, 1, 201,))
    thread7  = threading.Thread(target=delivery.testRequestOrder, args=(8, 302, 101, 2, 1, 201,))

    thread4.start()
    thread5.start()
    thread6.start()
    thread7.start()

    thread4.join()
    thread5.join()
    thread6.join()
    thread7.join()

    delivery.testGetOrder(81, 1000, "delivered", 201, 200)
    delivery.testGetOrder(82, 1001, "delivered", 202, 200)
    delivery.testGetOrder(57, 1002, "assigned", 203, 200)
    res1 = delivery.testGetOrder(57, 1003, "return", 201, 200)
    res2 = delivery.testGetOrder(57, 1004, "return", 202, 200)

    if res1.json()["agentId"] != res2.json()["agentId"]:
        test_pass()

    # --------- test case 2 ------------
    # --------- 50 threads deducing 40 rupees resulting in 0(40*50) amount ------------------------------
    wallet.testReinitialize(1)
    restaurant.testReinitialize(2)
    delivery.testReinitialize(3)


    threads = []

    for i in range(50):

        threads.append(threading.Thread(target=wallet.testDeductBalance, args=(11, 301, 40, 201,)))

    for i in range(50):
        threads[i].start()

    for i in range(50):
        threads[i].join()

    res1 = wallet.testGetBalance(7, 301, 101, 144)
    # print(res1.json()["amount"])

    if res1.json()["amount"] == 0:
        test_pass()

    print_box("\033[93mPublic Testcases (Project 1 Phase 2) \033[1;32;40m")