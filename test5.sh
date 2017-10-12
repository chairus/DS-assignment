#!/bin/bash

####################################################################
#	Tests if the system is fault tolerant to two failed servers    #
####################################################################


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
xterm -title "Server 0" -e "echo \"Server 0 running...\" && java uni/mitter/MitterServer 0 debug" &
echo "Server 0 running..."
xterm -title "Server 3" -e "echo \"Server 3 running...\" && java uni/mitter/MitterServer 3 debug" &
echo "Server 3 running..."
xterm -title "Server 1" -e "echo \"Server 1 running...\" && java uni/mitter/MitterServer 1 debug" &
echo "Server 1 running..."
xterm -title "Server 2" -e "echo \"Server 2 running...\" && java uni/mitter/MitterServer 2 debug" &
echo "Server 2 running..."
xterm -title "Server 4" -e "echo \"Server 4 running...\" && java uni/mitter/MitterServer 4 debug" &
echo "Server 4 running..."

sleep 7

# grab the process id of the leader server
leader_pid= ps | grep xterm | awk '{print $1}' | awk 'FNR == 5 {print}'

echo "========================================================================"
echo "Starting notifiers..."
java uni/mitter/MitterNotifier2 &	# Connects to SERVER 1
sleep 2
java uni/mitter/MitterNotifier3 &	# Connects to SERVER 0
sleep 1.5
java uni/mitter/MitterNotifier &	# Connects to SERVER 4
sleep 2
echo "Sending notifications to the server..."

sleep 10

echo "========================================================================"
echo "Terminating the leader server(SERVER 4)..."
echo "$leader_pid"
echo "Leader terminated."