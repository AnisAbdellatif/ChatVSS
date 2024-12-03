package de.luh.vss.chat.client;


import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class LeaseManager extends Worker {
	private final LinkedBlockingQueue<Instant> renewQueue = new LinkedBlockingQueue<Instant>();
	private Instant lastInstant;
	
	public LeaseManager(Worker.AtomicRefWM workers) {
		super(workers);
	}
		
	public void renew() {
		try {		
			// Pause all other threads
			for (HashMap.Entry<Worker.Type, Worker> entry : this.workers.get().entrySet()) {
				if (entry.getKey() == Worker.Type.LEASE_MANAGER
				 || entry.getKey() == Worker.Type.TCP_CLIENT
				 || entry.getKey() == Worker.Type.UDP_CLIENT) continue;
	            entry.getValue().pauseThread();
	            System.out.printf("Thread '%s' is now paused!\n", entry.getKey().toString());
	        }
			
			renewQueue.put(Instant.now());
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int task() {
		try {
			if (lastInstant == null) lastInstant = Instant.now();
			else if (Instant.now().isAfter(lastInstant.plusSeconds(150))) renew();
			
			Instant it = renewQueue.poll(200, TimeUnit.MILLISECONDS);
			if (it == null) {
				return 0;
			}
			
			lastInstant = Instant.now();
	        ZonedDateTime zonedDateTime = lastInstant.atZone(ZoneId.systemDefault());
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	        String readableDate = zonedDateTime.format(formatter);
			System.out.printf("Renewing the Lease at %s!\n", readableDate);
			
			TCP_Client tcpc = (TCP_Client) workers.get().get(Worker.Type.TCP_CLIENT);
			tcpc.renewLease();
	        
	        Thread.sleep(1000);
	        	        
	        for (HashMap.Entry<Worker.Type, Worker> entry : this.workers.get().entrySet()) {
	        	if (entry.getKey() == Worker.Type.LEASE_MANAGER
	   				 || entry.getKey() == Worker.Type.TCP_CLIENT
	   				 || entry.getKey() == Worker.Type.UDP_CLIENT) continue;
				entry.getValue().refresh();
	            entry.getValue().resumeThread();
	            System.out.printf("Thread '%s' is now resumed!\n", entry.getKey().toString());
	        }

		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
		
	}
	
	public void refresh() throws IOException {}
	public void clean() throws IOException {}
}