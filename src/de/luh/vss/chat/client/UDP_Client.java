package de.luh.vss.chat.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import de.luh.vss.chat.client.Wrapper.*;
import de.luh.vss.chat.common.User.UserId;

public class UDP_Client extends NetClient {
    	
		private DatagramSocket socket;
		private final int localPort = 36666;
		private final int MAX_BUFF_SIZE = 4096;
    	private DatagramPacket recvPacket;
    	
    	public UDP_Client(AtomicRefWM workers, UserId userId) {
    		super(workers, userId);
    		
            try {
            	refresh();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

    	public void send(ByteArrWrapper bufferwrp) {
			try {           
				DatagramPacket payload = new DatagramPacket(bufferwrp.content, bufferwrp.content.length, bufferwrp.remoteAddr, bufferwrp.remotePort);
				socket.send(payload);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

		@Override
		public ByteArrWrapper receive() {
			ByteArrWrapper bufferwrp = null;
			try {
				socket.receive(recvPacket);
				byte[] buffer = recvPacket.getData();
				bufferwrp = new ByteArrWrapper(NetClient.SocketType.UDP, recvPacket.getAddress(), recvPacket.getPort(), buffer);
			} catch (Exception e) {
				if (e instanceof SocketException) {}
				else e.printStackTrace();
			}
			return bufferwrp;
    	}
		
		@Override
		public void refresh() throws IOException {
			clean();
			// Create a socket to listen on port 36666
			socket = new DatagramSocket(localPort);
			System.out.println("UDP Receiver is ready to receive data...");
			
			// Set a timeout of 5000 milliseconds (5 seconds)
			//socket.setSoTimeout(1000);
			
			// Buffer to hold incoming data
			byte[] buffer = new byte[MAX_BUFF_SIZE];
			
			// Create a packet to receive the data
			recvPacket = new DatagramPacket(buffer, buffer.length);
			
		}
		
		@Override
		public void clean() throws IOException {
			if (socket != null && !socket.isClosed()) socket.close();
		}
    }