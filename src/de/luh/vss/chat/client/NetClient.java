package de.luh.vss.chat.client;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import de.luh.vss.chat.client.Wrapper.*;
import de.luh.vss.chat.common.User.UserId;

public abstract class NetClient extends Worker {
	
	public static enum SocketType {
		TCP("TCP"),
		UDP("UDP");
		
		private final String desc;

	    // Constructor to initialize the description
	    SocketType (String desc) {
	        this.desc = desc;
	    }
	    
	    @Override
	    public String toString() {
	        return desc;
	    }
	}
	
	public static class LBQ_BAWRP extends LinkedBlockingQueue<ByteArrWrapper> {
		private static final long serialVersionUID = 6637014870528657959L;
	};
	
	protected final String serverAddress = "130.75.202.197";
	protected final int port = 4444;
	protected final int PIN = 5621;
	protected final UserId userId;
	   
	public NetClient(Worker.AtomicRefWM workers, UserId userId) {
		super(workers);
		this.userId = userId;
	}
    
    @Override
	public int task() {
    	// Wait for data
		ByteArrWrapper bufferwrp = receive();
        if (bufferwrp == null || bufferwrp.content.length == 0) return 0;

        ((MessageReceiver) workers.get().get(Worker.Type.RECEIVER)).addToQueue(bufferwrp);
        
        return 0;
    }
    
    abstract public void send(ByteArrWrapper bufferwrp);
    abstract protected ByteArrWrapper receive();
    
    abstract public void refresh() throws IOException;
    abstract public void clean() throws IOException;
}