package com.lotzy.socketclient;

import com.lotzy.socketclient.SocketClient.ClientListener;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Main implements ClientListener {
    public Main() {}
    
    public static void main(String[] args) throws InterruptedException  {
        SocketClient client = new SocketClient("localhost",1337,5000);
        client.addListener(new Main());
        
        new Thread(client).start();
        
        while (true) {
            while(!client.isConnected()) {
                Thread.sleep(1000);
            }
            Scanner keyboard = new Scanner(System.in);
            System.out.println("enter an integer");
            int myint = keyboard.nextInt();
            client.sendObject(myint);
        }
    }

    @Override
    public void onConnection(InetSocketAddress address) {
        System.out.println("Connected to socket server at address " + address.getAddress().getHostAddress() + ":"+address.getPort());
    }

    @Override
    public void onDisconnection() {
        System.out.println("Disconnected from socket server");
    }

    @Override
    public void onReceivePacket(Object object) {
        System.out.println("Got packet: "+object.toString());
    }

    @Override
    public void onReconnection() {
        System.out.println("Recconection to server socket");
    }

}
