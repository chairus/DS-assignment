#!/bin/bash

# XSD files
xjc heartbeat.xsd
xjc message.xsd
xjc notification.xsd
xjc subscription.xsd

# Server
javac -d . ClientListener.java ClientThread.java Filter.java FilteredNotificationList.java LogEntry.java MitterServer.java NotificationAssembler.java NotifierListener.java OrderedNotification.java Sender.java ServerPeers.java Proposer.java Acceptor.java

# Notifiers and Clients(TESTING)
javac -d . MitterClientTest1.java MitterClientTest2.java MitterClientTest3.java MitterNotifier.java MitterNotifier2.java