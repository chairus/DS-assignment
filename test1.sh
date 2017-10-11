#!/bin/bash

####################################################################################
#	Tests if the notifier can send notifications to the server and if  the client  #
#	can receive notifications from the server it connects to   					   #
####################################################################################

# Run and connect three servers
java uni/mitter/MitterServer 0 &
java uni/mitter/MitterServer 1 &
java uni/mitter/MitterServer 2 &
sleep 5
java uni/mitter/MitterServer 3 &
java uni/mitter/MitterServer 4 &

sleep 1

# Run client test
java uni/mitter/MitterClientTest1 &

# Run and connect notifiers to the servers and send notifications
java uni/mitter/MitterNotifier2