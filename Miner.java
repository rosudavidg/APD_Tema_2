import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class Miner extends Thread {

    private final Integer hashCount;
    private final Set<Integer> solved;
    private final CommunicationChannel channel;

    // Semafor pentru a citi doua mesaje consecutive
    private static Semaphore semaphore = new Semaphore(1);

    public Miner(Integer hashCount, Set<Integer> solved, CommunicationChannel channel) {
        this.hashCount = hashCount;
        this.solved = solved;
        this.channel = channel;
    }

	private String encryptMultipleTimes(String input, Integer count) {
        String hashed = input;
        for (int i = 0; i < count; ++i) {
            hashed = encryptThisString(hashed);
        }

        return hashed;
    }

	private String encryptThisString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // convert to string
            StringBuilder hexString = new StringBuilder();
            for (byte item : messageDigest) {
                String hex = Integer.toHexString(0xff & item);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            Message message1; // primul mesaj: current room
            Message message2; // al doilea mesaj: adjacent room

            try {
                semaphore.acquire();
            } catch (Exception e) {
                e.printStackTrace();
            }

            message1 = channel.getMessageWizardChannel();
            message2 = channel.getMessageWizardChannel();

            // Daca camera pe care am extras-o este deja
	        // rezolvata, se vor ignora mesajele
	        if (solved.contains(message2.getCurrentRoom())) {
	        	semaphore.release();
	        	continue;
	        }

	        // Se marcheaza camera ca rezolvata
	        solved.add(message2.getCurrentRoom());
	        semaphore.release();

	        // Se rezolva camera
	        String hashed = encryptMultipleTimes(message2.getData(), hashCount);

	        // Se creeaza si se trimite mesajul catre vrajitori
	        Message message = new Message(message1.getCurrentRoom(), message2.getCurrentRoom(), hashed);
	        channel.putMessageMinerChannel(message);
        }
    }
}
