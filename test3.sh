#!/bin/bash

################################################################# 
#	Tests if a client only receives notifications that it has 	#
#	subscribed to. 							         			#
#################################################################

# Run and connect five servers
java uni/mitter/MitterServer 0 &
java uni/mitter/MitterServer 1 &
java uni/mitter/MitterServer 2 &
# sleep 5
java uni/mitter/MitterServer 3 &
java uni/mitter/MitterServer 4 &

sleep 5

echo "========================================================================"
echo "Testing if a client only receives notifications that it has subscribed to."
echo "Expecting to receive two urgent and one of each caution and notice notifications from the sender \"The_Band\"."
echo "The received notifications of each client will be in the files called:"
echo "test3_output_client_test1.txt"
echo "test3_output_client_test2.txt"
echo "NOTE: The first line on the output text tells which server the client is connected to, and the rest of the output are the received notifications from that server."
echo "========================================================================"

echo "========================================================================"
echo "First notifier running."
echo "Sending notifications to the server..."
# Run and connect notifiers to the servers and send notifications
java uni/mitter/MitterNotifier2 &

sleep 12

echo "========================================================================"
echo "Second notifier running."
echo "Sending notifications to the server..."
# Run and connect notifiers to the servers and send notifications
java uni/mitter/MitterNotifier3 &

sleep 12

echo "========================================================================"
echo "Third notifier running."
echo "Sending notifications to the server..."
# Run and connect notifiers to the servers and send notifications
java uni/mitter/MitterNotifier4 &

sleep 12

echo "========================================================================"
echo "First client running."
echo "Listening for notifications from the server..."
# Run the first client
java uni/mitter/MitterClientTest1 > test3_output_client_test1.txt &

echo "Wait for about 10 seconds..."
sleep 10

echo "========================================================================"
echo "Second client running."
echo "Listening for notifications from the server..."
# Run the second client
java uni/mitter/MitterClientTest2 > test3_output_client_test2.txt &

sleep 15
pkill "java"
echo "Released all allocated resources."