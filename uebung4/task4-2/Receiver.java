import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Receiver implements Runnable {

	int team_number = 11;
	String hostname = null;

	@SuppressWarnings("resource")
	@Override
	public void run() {
		DatagramSocket sock = null;
		try {
			sock = new DatagramSocket(5000 + team_number);
			hostname = InetAddress.getLocalHost().getHostName().toString();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		while (true) {
			try {
				byte[] buf = new byte[2000];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				sock.receive(packet);
				String current = new String(packet.getData()).trim();
				String[] data = current.split(":");

				//System.out.println("packet" + current);
				switch (data[0]) {
				case "F": // flooding/route request
					String currentId = data[0] + ":" + data[1] + ":" + data[2] + ":" + data[3];
					if (!Boot.messages.contains(currentId)) {
						Boot.messages.add(currentId);
						if (data[2].equals(hostname)) { // send response
							// switch sender and receiver, use R prefix, suffix hostname to found path, keep
							// rest unchanged
							String responseMsg = "R:" + data[2] + ":" + data[1] + ":" + data[3];
							Boot.messages.add(responseMsg);
							responseMsg += ":" + data[4] + "," + hostname;
							DatagramPacket r_packet = new DatagramPacket(responseMsg.getBytes(),
									responseMsg.getBytes().length, InetAddress.getByName("192.168.210.255"),
									5000 + team_number);
							sock.send(r_packet);
							//System.out.println("Making and send ResponseMsg " + responseMsg);
						} else { // forward if not in route
							boolean isInRoute = false;
							for (String hop: data[4].split(",")) {
								if (hostname.equals(hop)) {
									isInRoute = true;
									break;
								}
							}
							if (!isInRoute) {
								String forwardedMsg = current + "," + hostname;
								DatagramPacket f_packet = new DatagramPacket(forwardedMsg.getBytes(),
										forwardedMsg.getBytes().length, InetAddress.getByName("192.168.210.255"),
										5000 + team_number);
								sock.send(f_packet);
								//System.out.println("Forwarding " + forwardedMsg);
							}
						}
					}
					break;
				case "R": // route response
					if (!Boot.messages.contains("R:" + data[1] + ":" + data[2] + ":" + data[3])) {
						Boot.messages.add("R:" + data[1] + ":" + data[2] + ":" + data[3]);
						String[] route = data[4].split(",");
						if (data[2].equals(hostname)) {
							//System.out.println("Received route! Packet: " + current);
							long latency = (System.currentTimeMillis() - Long.parseLong(data[3]));
							System.out.println(
									"Route discovery from " + hostname + " to " + data[1] + " took " + latency + "ms route:" + data[4]);
							// send some data back
							String message = "!DATA!WÖRKS!";
							String dataMessage = "D:" + data[2] + ":" + data[1] + ":" + System.currentTimeMillis() + ":"
									+ data[4] + ":" + message;
							DatagramPacket d_packet = new DatagramPacket(dataMessage.getBytes(),
									dataMessage.getBytes().length, InetAddress.getByName("192.168.210.255"),
									5000 + team_number);
							sock.send(d_packet);
							Boot.messages.add(dataMessage);
						} else {
							for (int i = 0; i < route.length - 1; i++) {// if we are the target we don't forward
								if (route[i].equals(hostname)) {
									// forward
									DatagramPacket f_packet = new DatagramPacket(current.getBytes(),
											current.getBytes().length, InetAddress.getByName("192.168.210.255"),
											5000 + team_number);
									sock.send(f_packet);
									// System.out.println("Forwarding Data: " + current);
									break;
								}
							}
						}
					}
					break;
				case "D": // data
					if (!Boot.messages.contains(current)) {
						Boot.messages.add(current);
						String[] route = data[4].split(",");
						if (data[2].equals(hostname)) {
							System.out.println("Received data: " + data[5]);
						} else {
							for (int i = 0; i < route.length - 1; i++) {// if we are the target we don't forward
								if (route[i].equals(hostname)) {
									// forward
									DatagramPacket f_packet = new DatagramPacket(current.getBytes(),
											current.getBytes().length, InetAddress.getByName("192.168.210.255"),
											5000 + team_number);
									sock.send(f_packet);
									//System.out.println("Forwarding Data " + current);
									break;
								}
							}
						}
					}
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
