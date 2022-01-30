#/usr/bin/env python3

wallet_url      = "http://localhost:8082"
restaurant_url  = "http://localhost:8080"
delivery_url    = "http://localhost:8081"

def test_pass():
    print("\033[92m" + "pass" + "\033[0m")

def test_fail():
    print("\033[91m" + "fail" + "\033[0m")