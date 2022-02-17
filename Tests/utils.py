#/usr/bin/env python3

wallet_url      = "http://10.100.74.252:8080"
restaurant_url  = "http://10.100.184.172:8080"
delivery_url    = "http://10.105.202.153:8080"

class Restaurants:
    def __init__(self, id):
        self.id = id
        self.items = []
    
    def addItem(self, itemId, price, qty):
        self.items.append({"itemId":itemId, "price": price, "qty": qty})

    def getItem(self, itemId):
        return self.items[itemId]

def test_pass():
    print("\033[92m" + "pass" + "\033[0m")

def test_fail():
    print("\033[91m" + "fail" + "\033[0m")

# create a box for text message
def print_box(text):
    print("\033[1;32;40m")
    print("+" + "-" * (len(text) + 2) + "+")
    print("| " + text + " |")
    print("+" + "-" * (len(text) + 2) + "+")
    print("\033[0m")