package de.luh.vss.chat.client;


import java.io.IOException;

import de.luh.vss.chat.client.Wrapper.MessageWrapper;
import de.luh.vss.chat.common.MessageType;

public class MessageForwarder extends Worker {

	
	public MessageForwarder(Worker.AtomicRefWM workers) {
		super(workers);
	}
	
	@Override
	public int task() {
		try {
			MessageReceiver receiver = (MessageReceiver) workers.get().get(Worker.Type.RECEIVER);
			MessageSender sender = (MessageSender) workers.get().get(Worker.Type.SENDER);
			
			MessageWrapper msgwrp = receiver.getInQueue().take();
			if (msgwrp.content.getMessageType() != MessageType.CHAT_MESSAGE) return 0;
			
			//Message.ChatMessage msg = (Message.ChatMessage) msgwrp.content;
			//if (!msg.getMessage().startsWith("Timestamp: ")) return 0;
			if (!msgwrp.content.toString().contains("SPECIAL MESSAGE TEST 4")) return 0;
					
			System.out.printf("Got new Message to echo: %s\n", msgwrp.content.toString());
			sender.getOutQueue().put(msgwrp);	
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}	
		return 0;
	}
	
	protected void refresh() throws IOException {};
    
    protected void clean() throws IOException {};
	
}