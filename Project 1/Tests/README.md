# distribui-software

## Principles of Distributed Software - Testcases

### command to run test cases
    ./runtest.sh

### Testcase implementation

    Used "requests" library of python

### Troubleshooting

    If you are facing issue with port allocation/deallocation, run the following command for few times

        1. go to parent directory and run the following command

            ps aux | grep port-forward | awk '{print $2}' | xargs kill

        2. After that, run 'make port-forwards' again.
        3. Finally, run './runtest.sh' again.

    Make sure that metric-server is running.

    Note: To view the autoscaling feature, we need to wait for considerable amount of time(maybe 10 to 15 mins) in addition to generating huge amount of concurrent requests.