#!/bin/bash

# XSD files
xjc message.xsd
xjc notification.xsd
xjc subscription.xsd

# Server
javac -d . ClientListener.java ClientThread.java Filter.java FilteredNotificationList.java LogEntry.java MitterServer.java NotificationAssembler.java NotifierListener.java OrderedNotification.java Sender.java ServerPeers.java Proposer.java Acceptor.java NotificationRelayer.java ServerInfo.java

# Notifiers and Clients(TESTING)
javac -d . MitterClientTest1.java MitterClientTest2.java MitterClientTest3.java MitterNotifier.java MitterNotifier2.java MitterNotifier3.java MitterNotifier4.java MitterNotifier5.java MitterNotifier6.java Notifier.java Client.java Constants.java
