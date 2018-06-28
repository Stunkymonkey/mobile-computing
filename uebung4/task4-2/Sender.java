import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sender implements Runnable {

	int team_number = 11;
	byte[] bcast_msg = "HELLOworld!".getBytes();
	String sendingHost = "mcladhoc04";

	@Override
	public void run() {
		DatagramSocket sock = null;

		try {
			sock = new DatagramSocket();
			sock.setBroadcast(true);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		String hostname = "";
		try {
			hostname = InetAddress.getLocalHost().getHostName().toString();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		while (hostname.equals(sendingHost)) {
			for (int i = 1; i < 6; i++) {
				if (Integer.parseInt(hostname.substring(9)) != i) {
					try {
						// F:sender:target:timestamp:me,firstRouteHop(,Second,Third...)
						String bcastMsgString = "F:" + hostname + ":" + "mcladhoc0" + i + ":"
								+ System.currentTimeMillis() + ":" + hostname;
						System.out.println("### create F-message: from " + hostname.substring(9) + " to " + i);
						bcast_msg = bcastMsgString.getBytes();
						DatagramPacket packet = new DatagramPacket(bcast_msg, bcast_msg.length,
								InetAddress.getByName("192.168.210.255"), 5000 + team_number);
						Boot.messages.add(bcastMsgString.substring(0, bcastMsgString.length() - 11));
						sock.send(packet);
						Thread.sleep(200);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
