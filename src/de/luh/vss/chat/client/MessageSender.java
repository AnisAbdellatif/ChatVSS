package de.luh.vss.chat.client;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import de.luh.vss.chat.common.MessageType;
import de.luh.vss.chat.client.Wrapper.*;

public class MessageSender extends Worker {
    	private MessageWrapper prevMsg;

    	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	private final DataOutputStream dos = new DataOutputStream(baos);
        
        private LinkedBlockingQueue<MessageWrapper> outQueue = new LinkedBlockingQueue<MessageWrapper>();
        
        public MessageSender(Worker.AtomicRefWM workers) {
            super(workers);
        }

        public void send(MessageWrapper msgwrp) {
    		addToQueue(msgwrp);
    		if (msgwrp.content.getMessageType() == MessageType.CHAT_MESSAGE) {
    			this.prevMsg = msgwrp;
    		}
    	}
        
        public MessageWrapper getPrevMsg() {
        	return prevMsg;
        }
        
        public LinkedBlockingQueue<MessageWrapper> getOutQueue() {
        	return outQueue;
        }
        
        public void addToQueue(MessageWrapper msgwrp) {
        	try {
        		if (msgwrp.content.getMessageType() == MessageType.REGISTER_REQUEST) {
        			addToTop(msgwrp);
        		} else {
        			outQueue.put(msgwrp);        			
        		}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        public void addToTop(MessageWrapper msgwrp) {
            try {
            	LinkedBlockingQueue<MessageWrapper> tempQueue = new LinkedBlockingQueue<>(outQueue.size() + 1);
				tempQueue.put(msgwrp);
				tempQueue.addAll(outQueue);
				outQueue.clear();
				outQueue.addAll(tempQueue);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        public int task() {
        	MessageWrapper msgwrp = null;
        	try {
        		msgwrp = outQueue.poll(100, TimeUnit.MILLISECONDS);
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			return -1;
    		}
        	
        	if (msgwrp == null) return 0;
        	
        	try {
    			msgwrp.content.toStream(dos);
    			dos.flush();
    			byte[] buffer = baos.toByteArray();
    			baos.reset();
        			  			
    			System.out.printf("%s: Sending to %s:%d : %s\n", msgwrp.sType, msgwrp.remoteAddr, msgwrp.remotePort, msgwrp.content);
    			
    			switch (msgwrp.sType) {
    			case NetClient.SocketType.TCP:
    				TCP_Client tcpc = (TCP_Client) workers.get().get(Worker.Type.TCP_CLIENT);
    				tcpc.send(new ByteArrWrapper(NetClient.SocketType.TCP, tcpc.remoteAddr, tcpc.remotePort, buffer));
    				break;
    			case NetClient.SocketType.UDP:
    				UDP_Client udpc = (UDP_Client) workers.get().get(Worker.Type.UDP_CLIENT);
    				udpc.send(new ByteArrWrapper(NetClient.SocketType.TCP, msgwrp.remoteAddr, msgwrp.remotePort, buffer));
    				break;
    			}
    		} catch (Exception e) {
    			e.printStackTrace();
    			return -1;
    		}
        	
        	return 0;
        }
        
        protected void refresh() throws IOException {};
        
        protected void clean() throws IOException {
        	dos.close();
        	baos.close();
        };
    }