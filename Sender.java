package uni.mitter;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import generated.nonstandard.notification.*;

public class Sender extends TimerTask {
    private ConcurrentLinkedQueue queue;

    public Sender(ConcurrentLinkedQueue queue) {
        this.queue = queue;
    }

    public void run() {
        while (!queue.isEmpty()) {
            
        }
    }
}