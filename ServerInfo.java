package uni.mitter;

/**
 * This class contains information about a server such as it's id, server port, notifier port, and ip address.
 */

public class ServerInfo {
	public int serverPort;
	public int notifierPort;
	public String ipAddress;
	public int id;

	public ServerInfo() {
		this.id = -1;
		this.serverPort = 0;
		this.notifierPort = 0;
		this.ipAddress = "";
	}

	public ServerInfo(int id, int serverPort, int notifierPort, String ipAddress) {
		this.id = id;
		this.serverPort = serverPort;
		this.notifierPort = notifierPort;
		this.ipAddress = ipAddress;
	}
}