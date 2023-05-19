package com.lotzy.socketserver;

import com.lotzy.socketserver.SocketServer.ServerListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Main implements ServerListener {
    public void Main() {}
    
    public static void main(String[] args) throws IOException {
        SocketServer server = new SocketServer(1337);
        server.addListener(new Main());
        server.start();
        while (true) {
            Scanner keyboard = new Scanner(System.in);
            System.out.println("enter an integer");
            int myint = keyboard.nextInt();
            server.getClients().forEach(client -> client.sendObject(myint));
        }
    }
    
    @Override
    public void onClientConnection(ClientThread client) {
        InetSocketAddress address = (InetSocketAddress)client.getSocket().getRemoteSocketAddress();
        System.out.println("Connected client with address: "+ address.getAddress().getHostAddress()+":"+address.getPort());
    }
    
    @Override
    public void onReceivePacketFromClient(ClientThread client, Object packet) {
        InetSocketAddress address = (InetSocketAddress)client.getSocket().getRemoteSocketAddress();
        System.out.println("Got packet { "+ packet.toString() +" } from client with address: "+ address.getAddress().getHostAddress()+":"+address.getPort());
    }
    
    @Override
    public void onClientDisconnection(ClientThread client) {
        InetSocketAddress address = (InetSocketAddress)client.getSocket().getRemoteSocketAddress();
        System.out.println("Disconnected client with address: "+ address.getAddress().getHostAddress()+":"+address.getPort());
    }
}
