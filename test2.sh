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
echo "Expecting to receive one urgent and two caution notifications for each client."
echo "========================================================================"

echo "========================================================================"
echo "Notifier running."
# Run and connect notifiers to the servers and send notifications
java uni/mitter/MitterNotifier2 &

echo "========================================================================"
echo "First client running."
# Run the first client
java uni/mitter/MitterClientTest1 &

echo "Wait for about 40 seconds..."
sleep 40

echo "========================================================================"
echo "Second client running."
# Run the second client
java uni/mitter/MitterClientTest3