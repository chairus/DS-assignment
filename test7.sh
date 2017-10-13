#!/bin/bash

############################################################################
#	Tests if the system is fault-tolerant to two failed servers and when   #
#	the failed servers are restarted								       #
############################################################################


echo "========================================================================"
echo "Testing if the system is fault-tolerant to two failed servers by terminating two replicas, a leader and a non-leader, and restarting the failed servers."
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

# grab the process id of the leader and non-leader server
leader_pid=$(ps | grep xterm | awk '{print $1}' | awk 'FNR == 5 {print}')
replica_pid=$(ps | grep xterm | awk '{print $1}' | awk 'FNR == 1 {print}')

echo "========================================================================"
echo "Starting notifiers..."
echo "Sending notifications to the server..."
java uni/mitter/MitterNotifier2 &	# Connects to SERVER 1
sleep 2
java uni/mitter/MitterNotifier3 &	# Connects to SERVER 0
sleep 1.5
java uni/mitter/MitterNotifier &	# Connects to SERVER 4

sleep 8

echo "========================================================================"
echo "Terminating SERVER 4(leader) and SERVER 0(non-leader)..."
kill $leader_pid
kill $replica_pid
echo "Servers terminated."

sleep 7

echo "========================================================================"
echo "Starting notifiers..."
echo "Sending notifications to the server..."
java uni/mitter/MitterNotifier6 &	# Connects to SERVER 1
sleep 2
java uni/mitter/MitterNotifier7 &	# Connects to SERVER 2
sleep 1.5
java uni/mitter/MitterNotifier8 &	# Connects to SERVER 3
sleep 2

echo "========================================================================"
echo "Restarting SERVER 4 and SERVER 0..."
xterm -title "Server 0" -e "echo \"Server 0 running...\" && java uni/mitter/MitterServer 0 debug" &
echo "Server 0 running..."
xterm -title "Server 4" -e "echo \"Server 4 running...\" && java uni/mitter/MitterServer 4 debug" &
echo "Server 4 running..."

sleep 5

echo "========================================================================"
echo "Starting notifiers..."
echo "Sending notifications to the server..."
java uni/mitter/MitterNotifier9 &	# Connects to SERVER 4
sleep 2
java uni/mitter/MitterNotifier10 &	# Connects to SERVER 2
sleep 1.5
java uni/mitter/MitterNotifier11 &	# Connects to SERVER 1
sleep 1

sleep 40

pkill "java"
pkill "xterm"
echo "Released all allocated resources."