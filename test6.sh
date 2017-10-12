#!/bin/bash

#############################################################################################
#	Tests if notifications with different types of severity are sent on different times 	#
#	and that each connected client has their own timers.   					   				#
#############################################################################################


# Run and connect five servers
java uni/mitter/MitterServer 0 &
java uni/mitter/MitterServer 1 &
java uni/mitter/MitterServer 2 &
# sleep 5
java uni/mitter/MitterServer 3 &
java uni/mitter/MitterServer 4 &

sleep 5

echo "========================================================================"
echo "Testing if notifications with different types of severity are sent on different times and that each connected client has their own timers."
echo "Urgent are sent as soon as possible."
echo "Caution are sent every 10 seconds."
echo "Notice are sent every 20 seconds."
echo "========================================================================"

echo "Starting first client..."
xterm -title "Client 1" -e "echo \"Client 1 running...\" && java uni/mitter/MitterClientTest1" &

echo "Wait for 4.5 seconds so that timers of each client have different start time."
sleep 4.5

echo "Starting second client..."
xterm -title "Client 2" -e "echo \"Client 2 running...\" && java uni/mitter/MitterClientTest3" &

echo "========================================================================"
echo "Notifier running."
echo "Sending notifications to the server..."
# Run and connect notifiers to the servers and send notifications
java uni/mitter/MitterNotifier9 &

echo "Waiting for all clients to receive all notifications..."
sleep 60

pkill "java"
pkill "xterm"
echo "Released all allocated resources."