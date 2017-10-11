#!/bin/bash

########################################################################## 
#	Tests if two clients with the same subscription receives the same 	 #			
#	notifications in the same order. 							         #
##########################################################################

# Run and connect five servers
java uni/mitter/MitterServer 0 &
java uni/mitter/MitterServer 1 &
java uni/mitter/MitterServer 2 &
# sleep 5
java uni/mitter/MitterServer 3 &
java uni/mitter/MitterServer 4 &

sleep 5

echo "========================================================================"
echo "Testing if two clients connected to different servers with same subscrption receives the same notifications in the same order."
echo "Expecting to receive one urgent, two caution and two notice notifications for each client in that order."
echo "========================================================================"

echo "========================================================================"
echo "Notifier running."
# Run and connect notifiers to the servers and send notifications
echo "Sending notifications to the server..."
java uni/mitter/MitterNotifier2 &

sleep 12

echo "========================================================================"
echo "First client running."
# Run the first client
java uni/mitter/MitterClientTest1 &

# wait for about 20 seconds..."
sleep 10

echo "========================================================================"
echo "Second client running."
# Run the second client
java uni/mitter/MitterClientTest3 &

sleep 15

pkill "java"
echo "Released all allocated resources."