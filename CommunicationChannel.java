import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class CommunicationChannel {

	// Elemente pentru realizarea sincronizarii
	private final Semaphore wizardChannelReadSemaphore  = new Semaphore(0);
	private final Semaphore wizardChannelWriteSemaphore = new Semaphore(1);

	private final AtomicBoolean minerIsReading = new AtomicBoolean(false);
	private static final AtomicLong minerIsReadingFrom = new AtomicLong();
	private int nNewMessages = 0;

	// Mesajele de la vrajitori catre mineri
	private HashMap<Long, ArrayList<Message>> messages = new HashMap<>();

	// Mesajele de la mineri catre vrajitori
	private BlockingQueue<Message> minerMessages = new ArrayBlockingQueue<>(10000);

	public CommunicationChannel() {}

	public void putMessageMinerChannel(Message message) {
		try {
			minerMessages.put(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Message getMessageMinerChannel() {
		try {
			return minerMessages.take();
		} catch (Exception e) {
			return null;
		}
	}

	public void putMessageWizardChannel(Message message) {
		// Daca mesajul este END sau EXIT se ignora
		if (!message.getData().equals(Wizard.END) && !message.getData().equals(Wizard.EXIT)) {
			try {
				wizardChannelWriteSemaphore.acquire();
			} catch (Exception e) {
				return;
			}

			// Daca este primul mesaj trimis de acest vrajitor,
			// se adauga in hashMap
			if (!messages.containsKey(Thread.currentThread().getId())) {
				messages.put(Thread.currentThread().getId(), new ArrayList<Message>());
			}

			// Se adauga mesajul
			messages.get(Thread.currentThread().getId()).add(message);

			nNewMessages++;

			// La fiecare doua mesaje, un nou miner poate citi de pe wizardChannel
			if (nNewMessages == 2) {
				wizardChannelReadSemaphore.release();
				nNewMessages = 0;
			}

			wizardChannelWriteSemaphore.release();
		}
	}

	public Message getMessageWizardChannel() {
		// Verific daca minerul curent este la primul sau la al doilea mesaj
		if (minerIsReading.get()) {
			// Extrag al doilea mesaj
			Message message = messages.get(minerIsReadingFrom.get()).get(0);
			messages.get(minerIsReadingFrom.get()).remove(0);

			wizardChannelWriteSemaphore.release();
			minerIsReading.set(false);

			return message;
		} else {
			try {
				wizardChannelReadSemaphore.acquire();
				wizardChannelWriteSemaphore.acquire();
			} catch (Exception e) {e.printStackTrace();}

			minerIsReading.set(true);

			// Gasesc vrajitorul care a pus cel putin doua mesaje
			Iterator<Map.Entry<Long, ArrayList<Message>>> iterator = messages.entrySet().iterator();
			Map.Entry<Long, ArrayList<Message>> entry = iterator.next();

			while (entry.getValue().size() < 2) {
				entry = iterator.next();
			}
			Long key = entry.getKey();
			minerIsReadingFrom.set(key);

			// Extrag primul mesaj
			Message message = entry.getValue().get(0);
			entry.getValue().remove(0);

			return message;
		}
	}
}
