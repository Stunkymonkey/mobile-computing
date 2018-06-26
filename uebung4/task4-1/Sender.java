import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sender implements Runnable {

	int team_number = 11;
	byte[] bcast_msg = "HELLOworld!".getBytes();

	@SuppressWarnings("resource")
	@Override
	public void run() {
		DatagramSocket sock = null;

		try {
			sock = new DatagramSocket();
			sock.setBroadcast(true);
			String delay = InetAddress.getLocalHost().getHostName().toString().substring(9);
			System.out.println("delay: " + delay);
			Thread.sleep(Integer.parseInt(delay)*50);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		while (true) {
			try {
				bcast_msg = (InetAddress.getLocalHost().getHostName().toString() + ":" + System.currentTimeMillis())
						.getBytes();
				DatagramPacket packet = new DatagramPacket(bcast_msg, bcast_msg.length,
						InetAddress.getByName("192.168.210.255"), 5000 + team_number);
				Boot.messages.add(new String(bcast_msg));
				sock.send(packet);

				Thread.sleep(250);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

}
