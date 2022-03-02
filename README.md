# distribui-software

## Principles of Distributed Software

### commands

    for setup and start the application

        ./launch.sh

    for stop the application and clean the application

        ./teardown.sh

    Incase, the launch fails due to port allocation/deallocation

        make port-forwards

    for testcases, goto "Tests folder", and run the following commands

        ./runtest.sh

### Testcase implementation

    Used "requests" library of python

### Troubleshooting

    If issue with port allocation/deallocation, run the following command for few times

        ps aux | grep port-forward | awk '{print $2}' | xargs kill

    Make sure that metric-server is running.

    Note: To view the autoscaling feature, we need to wait for considerable amount of time(maybe 10 to 15 mins) in addition to generating huge amount of concurrent requests.

### Screenshots

    ![Screenshot](Screenshots/1.png)
    ![Screenshot](Screenshots/2.png)
    ![Screenshot](Screenshots/3.png)
    ![Screenshot](Screenshots/4.png)