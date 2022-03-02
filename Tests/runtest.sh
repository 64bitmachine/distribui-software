clear

# -------------- phase 1 testcases --------------

# cd unittest
# python3 wallet.py
# python3 restaurant.py
# cd ..
# cd integrationtest
# python3 delivery.py
# cd ../PublicTests
# python3 Public1-Project1Phase1.py
# python3 Public2-Project1Phase1.py
# cd ..

# -------------- phase 2 testcases --------------
cd loadtest
python3 concurrency.py
cd ..
cd PublicTests
python3 Public1-Project1Phase2.py
cd ..