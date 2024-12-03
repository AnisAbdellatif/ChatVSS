package de.luh.vss.chat.client;

import java.io.*;

import java.util.Scanner;

import de.luh.vss.chat.client.Wrapper.MessageWrapper;
import de.luh.vss.chat.common.*;

public class ChatClient {
	
	private final Worker.AtomicRefWM workers = new Worker.AtomicRefWM();

    public static void main(String... args) {
        try {
            new ChatClient().start();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException, InterruptedException {
        System.out.println("Congratulation for successfully setting up your environment for Assignment 1!");

        // Define the IP and port
        final String serverAddress = "130.75.202.197"; // Replace with the server IP address
        final int serverPort = 4444; // Replace with the server port number
        final int PIN = 5621;
        
        // Define your user ID
        User.UserId userId = new User.UserId(PIN); // Replace with your actual ErgebnisPIN
        
        try {        
        	TCP_Client       tcpc = new TCP_Client(workers, userId, serverAddress, serverPort);
        	UDP_Client       udpc = new UDP_Client(workers, userId);
            MessageSender    ms   = new MessageSender(workers);
            MessageReceiver  mr   = new MessageReceiver(workers);
            MessageForwarder fd   = new MessageForwarder(workers);
            LeaseManager     lm   = new LeaseManager(workers);


            Worker.WorkersMap hashMap = new Worker.WorkersMap();
            hashMap.put(Worker.Type.TCP_CLIENT, tcpc);
            hashMap.put(Worker.Type.UDP_CLIENT, udpc);
            hashMap.put(Worker.Type.SENDER, ms);
            hashMap.put(Worker.Type.RECEIVER, mr);
            hashMap.put(Worker.Type.FORWARDER, fd);
            hashMap.put(Worker.Type.LEASE_MANAGER, lm);
            workers.set(hashMap);
            
            new Thread(tcpc, "TCP_Client").start();
            new Thread(udpc, "UDP_Client").start();
            new Thread(lm, "LeaseManager").start();
            new Thread(ms, "MessageSender").start();
            new Thread(mr, "MessageReceiver").start();
            new Thread(fd, "MessageForwarder").start();
                                   
            Thread.sleep(2000);
            
            // Assignment01 Tests
            /*
            // TEST 1
            outQueue.put(new Message.ChatMessage(userId, "TEST 1 USER ID CORRECTNESS"));

            Thread.sleep(1000);
            
            // TEST 2
        	outQueue.put(new Message.ChatMessage(userId, "TEST 2 OUT OF BAND PROTOCOL MESSAGE"));

        	Thread.sleep(1000);
        	
            // TEST 3
            outQueue.put(new Message.ChatMessage(userId, "TEST 3 EXCEEDING MAX MESSAGE LENGTH"));

            Thread.sleep(1000);
            
            // TEST 4
            outQueue.put(new Message.ChatMessage(userId, "TEST 4 HANDLING ERROR MESSAGE"));
            
            Thread.sleep(1000);
            */
            
            // Assignment02 Tests
            

            // TEST 1
            //System.out.println("RUNNING TEST1!");
            //outQueue.put(new MessageWrapper(MessageWrapper.SocketType.TCP, new Message.ChatMessage(userId, "TEST 1 SEND MESSAGE WHILE HAVING AN ACTIVE LEASE")));
            //Thread.sleep(1000);

            // TEST 2
            //System.out.println("RUNNING TEST2!");
            //ms.send(new MessageWrapper(Client.SocketType.UDP, tcpc.remoteAddr, 5252, new Message.ChatMessage(new User.UserId(7777), "TEST 2 ECHO MESSAGE FROM USER")));
        	//Thread.sleep(1000);
        	
            // TEST 3
            //System.out.println("RUNNING TEST3!");
            //ms.send(new MessageWrapper(Client.SocketType.TCP, tcpc.remoteAddr, tcpc.remotePort, new Message.ChatMessage(userId, "TEST 3 RENEW LEASE")));
            //Thread.sleep(1000);
            
            // TEST 4
            //System.out.println("RUNNING TEST4!");
            //ms.send(new MessageWrapper(Client.SocketType.TCP, tcpc.remoteAddr, tcpc.remotePort, new Message.ChatMessage(userId, "TEST 4 LISTEN TO INCOMING MESSAGES AND ECHO SPECIAL MESSAGE")));
            //Thread.sleep(1000);
            
            
            Scanner sc = new Scanner(System.in);
            
            while (true) {
            	String msg = sc.nextLine();
            	if (msg.equals("q")) break;
            	else if(msg.startsWith("tcp: ")) ms.send(new MessageWrapper(NetClient.SocketType.TCP, tcpc.remoteAddr, tcpc.remotePort, new Message.ChatMessage(userId, msg.substring(5))));
            	else if(msg.startsWith("udp: ")) ms.send(new MessageWrapper(NetClient.SocketType.UDP, tcpc.remoteAddr, 6006, new Message.ChatMessage(new User.UserId(9999), msg.substring(5))));
            	else if (msg.equals("pause send")) ms.pauseThread();
            	else if (msg.equals("resume send")) ms.resumeThread();
            	else if (msg.equals("renew")) lm.renew();
            	else if (msg.length() > 0) ms.send(new MessageWrapper(NetClient.SocketType.TCP, tcpc.remoteAddr, tcpc.remotePort, new Message.ChatMessage(userId, msg)));
            	else {}
            }
            
            sc.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Connection closed.");
        
        
    }
}
