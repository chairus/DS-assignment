#!/bin/bash

################################################################
#	Tests if the notifications is replicated to all servers    #
################################################################

echo "========================================================================"
echo "Testing if the notitifcations are replicated to all servers regardless of the server the notifier is connected to."
echo "The received notifications of each server will be in the files called:"
echo "test4_output_server_0.txt"
echo "test4_output_server_3.txt"
echo "test4_output_server_1.txt"
echo "test4_output_server_2.txt"
echo "test4_output_server_4.txt"
echo "========================================================================"

echo "Starting all five servers..."
xterm -title "Server 0" -e "echo \"Server 0 running...\" && java uni/mitter/MitterServer 0 debug > test4_output_server_0.txt" &
echo "Server 0 running..."
xterm -title "Server 3" -e "echo \"Server 3 running...\" && java uni/mitter/MitterServer 3 debug > test4_output_server_3.txt" &
echo "Server 3 running..."
xterm -title "Server 1" -e "echo \"Server 1 running...\" && java uni/mitter/MitterServer 1 debug > test4_output_server_1.txt" &
echo "Server 1 running..."
xterm -title "Server 2" -e "echo \"Server 2 running...\" && java uni/mitter/MitterServer 2 debug > test4_output_server_2.txt" &
echo "Server 2 running..."
xterm -title "Server 4" -e "echo \"Server 4 running...\" && java uni/mitter/MitterServer 4 debug > test4_output_server_4.txt" &
echo "Server 4 running..."

sleep 7

echo "Starting notifiers..."
java uni/mitter/MitterNotifier2 &
sleep 2
java uni/mitter/MitterNotifier6 &
sleep 2
java uni/mitter/MitterNotifier3 &
sleep 1
java uni/mitter/MitterNotifier5 &
sleep 3
java uni/mitter/MitterNotifier4 &
sleep 1
java uni/mitter/MitterNotifier &

echo "Waiting for all notifiers to send all notifications..."
# wait for 60 seconds
sleep 65

pkill "java"
pkill "xterm"
echo "Released all allocated resources."