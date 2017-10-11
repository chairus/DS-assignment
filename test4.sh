#!/bin/bash

################################################################
#	Tests if the notifications is replicated to all servers    #
################################################################

echo "Starting all five servers..."
xterm -title "Server 0" -e "java uni/mitter/MitterServer 0 debug" &
xterm -title "Server 3" -e "java uni/mitter/MitterServer 3 debug" &
xterm -title "Server 1" -e "java uni/mitter/MitterServer 1 debug" &
xterm -title "Server 2" -e "java uni/mitter/MitterServer 2 debug" &
xterm -title "Server 4" -e "java uni/mitter/MitterServer 4 debug" &

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

sleep 60

pkill "java"
pkill "xterm"

echo "Released all allocated resources."