import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Receiver implements Runnable {

	int team_number = 11;
	String hostname = null;
	@SuppressWarnings("unchecked")
	ArrayList<Long>[] latencys = (ArrayList<Long>[]) new ArrayList[5];

	@SuppressWarnings("resource")
	@Override
	public void run() {
		DatagramSocket sock = null;
		for (int i = 0; i < latencys.length; i++) {
			latencys[i] = new ArrayList<Long>();
		}
		try {
			sock = new DatagramSocket(5000 + team_number);
			hostname = InetAddress.getLocalHost().getHostName().toString();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		byte[] buf = new byte[500];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		while (true) {
			try {
				sock.receive(packet);
				String current = new String(packet.getData()).trim();
				String[] data = current.split(":");
				if (data.length == 2 && !data[0].equals(hostname) && !Boot.messages.contains(current)) {
					// System.out.println(hostname + data[0]);
					DatagramPacket f_packet = new DatagramPacket(current.getBytes(), current.getBytes().length,
							InetAddress.getByName("192.168.210.255"), 5000 + team_number);
					sock.send(f_packet);
					Boot.messages.add(current);
					System.out.println(current);
					long latency = (System.currentTimeMillis() - Long.parseLong(data[1]));
					System.out.print("latency: " + data[0] + ": " + latency + "ms");

					int id = Integer.parseInt(data[0].substring(9)) - 1;
					latencys[id].add(latency);
					System.out.println("\t\tmean: " + calculateAverage(latencys[id]));

					/*
					 * String response = current + ":" + hostname; DatagramPacket r_packet = new
					 * DatagramPacket(response.getBytes(), response.getBytes().length,
					 * InetAddress.getByName("192.168.210.255"), 5000 + team_number);
					 * //sock.send(r_packet); } else if (data.length == 3 &&
					 * data[0].equals(hostname)) { long latency = (System.currentTimeMillis() -
					 * Long.parseLong(data[1])) / 2; System.out.println("latency:  me-" + data[0] +
					 * " and client-" + data[2] + " : " + latency + "ms");
					 */
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private double calculateAverage(List<Long> marks) {
		Long sum = 0L;
		if (!marks.isEmpty()) {
			for (Long mark : marks) {
				sum += mark;
			}
			return sum.doubleValue() / marks.size();
		}
		return sum;
	}

}
