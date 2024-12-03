package de.luh.vss.chat.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import de.luh.vss.chat.common.Message;
import de.luh.vss.chat.client.NetClient.LBQ_BAWRP;
import de.luh.vss.chat.client.Wrapper.*;

public class MessageReceiver extends Worker {  
    protected LBQ_BAWRP receiveQ = new LBQ_BAWRP();
    BufferedInputStream bufferedIn = null;
	DataInputStream inputStream = null;
    
    private LinkedBlockingQueue<MessageWrapper> inQueue = new LinkedBlockingQueue<MessageWrapper>();
	
    public MessageReceiver(Worker.AtomicRefWM workers) {
    	super(workers);
    	receiveQ.clear();
    }
    
    public LinkedBlockingQueue<MessageWrapper> getInQueue() {
    	return inQueue;
    }
      
    public void addToQueue(ByteArrWrapper buffer) {
    	try {
			receiveQ.put(buffer);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
        
    public int task() {
    	ByteArrWrapper bufferwrp = null;
    	try {
			bufferwrp = receiveQ.poll(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
    	
    	if (bufferwrp == null) return 0;
    	   	  	
    	BufferedInputStream bufferedIn = new BufferedInputStream(new ByteArrayInputStream(bufferwrp.content));
    	DataInputStream inputStream = new DataInputStream(bufferedIn);
    	
    	bufferedIn.mark(1024);
    	try {
			while(bufferedIn.available() > 0) {
				getMessage(bufferwrp, inputStream);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return 0;
    }
    
    private int getMessage(ByteArrWrapper bufferwrp, DataInputStream inputStream) throws IOException {
    	Message message = null;
    	try {
    		message = Message.parse(inputStream);
    	} catch (Exception e) {
    		if (e instanceof SocketException || e instanceof EOFException) {
    			paused = true;
    			clean();
    			return 0;
    		}
    		System.out.println("Received out of bounds message!");
    		bufferedIn.reset();
			System.out.println(inputStream.readUTF());
    	} finally {
			clean();
    		if (message == null) return -1;
    	}
    	
    	MessageWrapper msgwrp = new MessageWrapper(bufferwrp.sType, bufferwrp.remoteAddr, bufferwrp.remotePort, message);

    	switch(message.getMessageType()) {
    	case CHAT_MESSAGE:
    		handleChatMessage(msgwrp);
    		break;
    	case ERROR_RESPONSE:
    		handleErrorResponse(msgwrp);
    		break;
    	case REGISTER_RESPONSE:
    		handleRegisterResponse(msgwrp);
    		break;
    	default:
    		System.out.println("Uknown Type of message received!");
    		return -1;
    	}
    	return 0;
    }
    
    private void handleChatMessage(MessageWrapper msgwrp) {
    	Message.ChatMessage chatMessage = (Message.ChatMessage) msgwrp.content;
    	
    	
    	int msgLen = chatMessage.getMessage().length() * 2;
    	if(msgLen - 4000 > 0) {
    		// Send back data overflow to the server
    		try {
				inQueue.put(new MessageWrapper(msgwrp.sType, msgwrp.remoteAddr, msgwrp.remotePort, new Message.ChatMessage(chatMessage.getRecipient(), Integer.toString(msgLen - 4000))));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		return;
    	}
    	
    	
        System.out.printf("Received ChatMessage over %s from %s:%d : %s\n", msgwrp.sType, msgwrp.remoteAddr, msgwrp.remotePort, chatMessage.toString());
        try {
			inQueue.put(msgwrp);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    private void handleErrorResponse(MessageWrapper msgwrp) {
    	Message.ErrorResponse errorResponse = (Message.ErrorResponse) msgwrp.content;
    	System.out.printf("Received ErrorResponse from %s:%d : %s\n", msgwrp.remoteAddr, msgwrp.remotePort, errorResponse.toString());
    	if (errorResponse.toString().contains("Lease renewal triggered too soon")) {
    		try {
				((TCP_Client) workers.get().get(Worker.Type.TCP_CLIENT)).refresh();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	if (errorResponse.toString().contains("no active lease")) {
    		((LeaseManager) workers.get().get(Worker.Type.LEASE_MANAGER)).renew();    		
    	}    	
    }
    
    private void handleRegisterResponse(MessageWrapper msgwrp) {
    	Message.RegisterResponse registerResponse = (Message.RegisterResponse) msgwrp.content;
    	System.out.printf("Received RegisterResponse from %s:%d : \n", msgwrp.remoteAddr, msgwrp.remotePort, registerResponse.toString());
    }
    
    public void refresh() throws IOException {
    }
    
    public void clean() throws IOException {
    	if (bufferedIn != null) bufferedIn.close();
    	if (inputStream != null) inputStream.close();
    }
}