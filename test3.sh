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
echo "========================================================================"

echo "========================================================================"
echo "First notifier running."
# Run and connect notifiers to the servers and send notifications
java uni/mitter/MitterNotifier2 &
echo "Sending notifications to the server..."

sleep 1

echo "========================================================================"
echo "Second notifier running."
echo "Sending notifications to the server..."
# Run and connect notifiers to the servers and send notifications
java uni/mitter/MitterNotifier3 &

sleep 1

echo "========================================================================"
echo "Third notifier running."
echo "Sending notifications to the server..."
# Run and connect notifiers to the servers and send notifications
java uni/mitter/MitterNotifier4 &

sleep 7

echo "========================================================================"
echo "First client running."
# Run the first client
java uni/mitter/MitterClientTest1 &

echo "Wait for about 40 seconds..."
sleep 40

echo "========================================================================"
echo "Second client running."
# Run the second client
java uni/mitter/MitterClientTest2