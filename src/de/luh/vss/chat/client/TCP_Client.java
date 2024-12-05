package de.luh.vss.chat.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import de.luh.vss.chat.client.Wrapper.*;
import de.luh.vss.chat.common.Message;
import de.luh.vss.chat.common.User.UserId;

public class TCP_Client extends NetClient {
    	
    	public final InetAddress remoteAddr;
    	public final int remotePort;
    	private Socket socket;
    	private DataInputStream inputStream;
    	private UserId userId;
        MessageWrapper renewMessage;
    	
    	public TCP_Client(Worker.AtomicRefWM workers, UserId userId, String remoteAddrString, int remotePort) throws UnknownHostException {
    		super(workers, userId);
    		this.remoteAddr = InetAddress.ofLiteral(remoteAddrString);
    		this.remotePort = remotePort;
    		this.userId = userId;
    		
    		try {
				refresh();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

    	public void send(ByteArrWrapper bufferwrp) {
			try {
				OutputStream out = socket.getOutputStream();
				out.write(bufferwrp.content, 0, bufferwrp.content.length);
				out.flush();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	protected ByteArrWrapper receive() {
    		ByteArrWrapper bufferwrp = null;

			try {
				byte[] buffer = inputStream.readNBytes(inputStream.available());
				bufferwrp = new ByteArrWrapper(NetClient.SocketType.TCP, socket.getInetAddress(), socket.getPort(), buffer);
			} catch (Exception e) {
				if (e instanceof SocketException || e instanceof SocketTimeoutException) {}
				else e.printStackTrace();
			}
			return bufferwrp;
    	}
    	
    	void renewLease() {
        	if (renewMessage == null) return;
        	try {
        		refresh();
        		MessageSender sender = (MessageSender) workers.get().get(Worker.Type.SENDER);
        		sender.send(renewMessage);
        		MessageWrapper prevMsg = sender.getPrevMsg();
        		
        		// FIXME: this resends the previous message on each renewal, although the message could be already sent successfully already.
				/*
        		if (prevMsg != null && prevMsg.content.getMessageType() == MessageType.CHAT_MESSAGE) {
					System.out.printf("Adding the last message to the Q: '%s'\n", prevMsg.content.toString());
					sender.send(prevMsg);					
				}
				*/
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

		@Override
		public void refresh() throws IOException {
			// TODO Auto-generated method stub
			clean();
			socket = new Socket(remoteAddr, remotePort);
			//socket.setOOBInline(true);
			//socket.setTcpNoDelay(true);
			socket.setSoTimeout(100);
			inputStream = new DataInputStream(socket.getInputStream());
			renewMessage = new MessageWrapper(NetClient.SocketType.TCP, remoteAddr, remotePort, new Message.RegisterRequest(userId, socket.getLocalAddress(), socket.getLocalPort()));
			System.out.printf("Local : %s:%d\n", socket.getLocalAddress(), socket.getLocalPort());
	        System.out.printf("Remote: %s:%d\n", socket.getInetAddress(), socket.getPort());
	        System.out.println("Connected to the server.");
		}
		
		@Override
		public void clean() throws IOException {
			if (socket != null && !socket.isClosed())
				socket.close();
			if (inputStream != null)
				inputStream.close();
			
		}
    }