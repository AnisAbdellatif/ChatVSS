package de.luh.vss.chat.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Worker implements Runnable {
	public static enum Type {
		SENDER("SENDER"),
		RECEIVER("RECEIVER"),
		FORWARDER("FORWARDER"),
		LEASE_MANAGER("LEASE_MANAGER"),
		UDP_CLIENT("UDP_CLIENT"),
		TCP_CLIENT("TCP_CLIENT");
		
		private String desc;
		
		Type(String desc) {
			this.desc = desc;
		}
		
		@Override
	    public String toString() {
	        return this.desc;
	    }
		
	}
    
	public static class WorkersMap extends HashMap<Worker.Type, Worker> {
		private static final long serialVersionUID = -7882526403422716940L;
	};
	public static class AtomicRefWM extends AtomicReference<WorkersMap> {
		private static final long serialVersionUID = -3888721313778380313L;
	};
	
	protected AtomicRefWM workers;
	
	protected boolean paused = false;
    
	public Worker(AtomicRefWM workers) {
		this.workers = workers;
	}
	
    public synchronized void pauseThread() {
        paused = true;
    }

    public synchronized void resumeThread() {
        paused = false;
        notifyAll();
    }
    
    @Override
    public void run() {
    	while(true) {
    		synchronized (this) {
                while (paused) {
                    try {
                        wait(); // Pause the thread
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.out.println("Thread interrupted");
                        break;
                    }
                }
            }
    		if (task() != 0) break;
    		
    	}
    	try {
    		clean();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    abstract public int task();
    
    abstract protected void refresh() throws IOException;
    
    abstract protected void clean() throws IOException;
	
}