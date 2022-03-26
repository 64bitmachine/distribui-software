clear

# -------------- phase 1 testcases --------------

cd unittest
python3 wallet.py
python3 restaurant.py
cd ..
cd integrationtest
python3 delivery.py
cd ../PublicTests
python3 Public1-Project1Phase1.py
python3 Public2-Project1Phase1.py
cd ..