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
echo "The received notifications of the client will be in the files called:"
echo "test1_output.txt"
echo "========================================================================"

echo "========================================================================"
# Run client test
echo "Client running."
echo "Listening for notifications from the server..."
java uni/mitter/MitterClientTest1 > test1_output.txt &

sleep 12

echo "========================================================================"
echo "Notifier running."
echo "Sending notifications to the server..."
# Run and connect notifiers to the servers and send notifications
java uni/mitter/MitterNotifier2 &

sleep 30

pkill "java"

echo "Released all allocated resources."