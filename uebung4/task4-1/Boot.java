import java.util.HashSet;

public class Boot {
	
	int team_number = 11;

	public static HashSet<String> messages = new HashSet<String>();

	public static void main(String[] args) {
		
		System.out.println(System.currentTimeMillis());

		new Thread(new Receiver()).start();
		new Thread(new Sender()).start();
	}

}
