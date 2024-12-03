package de.luh.vss.chat.client;


import java.net.InetAddress;

import de.luh.vss.chat.common.Message;

public abstract class Wrapper<T> {
	public NetClient.SocketType sType;
	public InetAddress remoteAddr;
	public int remotePort;
	public T content;
	
	public Wrapper(NetClient.SocketType sType, InetAddress remoteAddr, int remotePort, T content) {
		this.sType = sType;
		this.remoteAddr = remoteAddr;
		this.remotePort = remotePort;
		this.content = content;
	}
	public static class MessageWrapper extends Wrapper<Message> {
	    public MessageWrapper(NetClient.SocketType sType, InetAddress remoteAddr, int remotePort, Message content) {
	        super(sType, remoteAddr, remotePort, content);
	    }
	}
	public static class ByteArrWrapper extends Wrapper<byte[]> {
	    public ByteArrWrapper(NetClient.SocketType sType, InetAddress remoteAddr, int remotePort, byte[] content) {
	        super(sType, remoteAddr, remotePort, content);
	    }
	}
}

