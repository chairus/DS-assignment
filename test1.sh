#!/bin/bash

####################################################################################
#	Tests if the notifier can send notifications to the server and if  the client  #
#	can receive notifications from the server it connects to   					   #
####################################################################################

# Run and connect five servers
java uni/mitter/MitterServer 0 &
java uni/mitter/MitterServer 1 &
java uni/mitter/MitterServer 2 &
# sleep 5
java uni/mitter/MitterServer 3 &
java uni/mitter/MitterServer 4 &

sleep 5

echo "========================================================================"
echo "Testing if notifier can send notifications to the server and client can receive notifications from the server it connects to."
echo "Expecting to receive one urgent, two caution and two notice notifications in that order."
echo "========================================================================"

echo "========================================================================"
# Run client test
echo "Client running."
java uni/mitter/MitterClientTest1 &

echo "========================================================================"
echo "Notifier running."
# Run and connect notifiers to the servers and send notifications
java uni/mitter/MitterNotifier2