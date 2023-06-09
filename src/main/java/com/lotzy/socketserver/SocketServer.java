package com.lotzy.socketserver;

import com.lotzy.socketserver.ClientThread.ClientListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SocketServer extends Thread implements ClientListener {
    public interface ServerListener {
        default public void onClientConnection(ClientThread client) {};
        default public void onClientDisconnection(ClientThread client) {};
        default public void onReceivePacketFromClient(ClientThread client, Object object) {};
        default public void onSendPacketToClient(ClientThread client,Object object, boolean sended) {};
        default public void onInvalidPacketFromClient(ClientThread client) {};
        default public void onClientClose(ClientThread client) {};
        default public void onClose() {};
    }
    
    ServerSocket server;
    List<ClientThread> clients = new ArrayList();
    
    List<ServerListener> listeners = new ArrayList();
    
    public void addListener(ServerListener toAdd) {
        listeners.add(toAdd);
    }
    
    public SocketServer(int port) throws IOException {
        server = new ServerSocket(port);
    }
    
    @Override
    public void run() {
        while(true) {
            try {
                Socket socket = server.accept();
                ClientThread client = new ClientThread(socket);
                client.addListener(this);
                clients.add(client);
                client.start();
            } catch (IOException ex) {}  
        }
    }
    
    public ServerSocket getServerSocket() {
        return this.server;
    }
    
    public List<ClientThread> getClients() {
        return this.clients;
    }
    
    public void close() {
        if (!this.isInterrupted()) this.interrupt();
        for(ClientThread client : clients) client.close();
        if (server != null) try {
            server.close();
        } catch (IOException ex) {}
        for(ServerListener listener : this.listeners)
            listener.onClose();
    }
    
    @Override
    public void onDisconnection(ClientThread client) {
        this.clients.remove(client);
        for(ServerListener listener : this.listeners)
            listener.onClientDisconnection(client);
    }
    
    @Override
    public void onClose(ClientThread client) {
        this.clients.remove(client);
        for(ServerListener listener : this.listeners)
            listener.onClientClose(client);
    }
    
    @Override
    public void onConnection(ClientThread client) {
        for(ServerListener listener : this.listeners)
            listener.onClientConnection(client);
    }
    
    @Override
    public void onReceivePacket(ClientThread client, Object object) {
        for(ServerListener listener : this.listeners)
            listener.onReceivePacketFromClient(client, object);
    }
    
    @Override
    public void onSendPacket(ClientThread client,Object object, boolean sended) {
        for(ServerListener listener : this.listeners)
            listener.onSendPacketToClient(client, object, sended);
    }
    
    @Override
    public void onInvalidPacket(ClientThread client) {
        for(ServerListener listener : this.listeners)
            listener.onInvalidPacketFromClient(client);
    };
    
    public static InetAddress getExternalAddress() {
        try {
            URL url = new URL("http://checkip.amazonaws.com/");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String ip = br.readLine();
            return InetAddress.getByName(ip);
        } catch (IOException ex) {}
        return null;
    }
    public static List<InetAddress> getLocalV4Addresses() {
        List<InetAddress> addresses = new ArrayList<>();
        Enumeration networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            return addresses;
        }
        while(networkInterfaces.hasMoreElements()){
            NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();
            Enumeration inetAddresses = networkInterface.getInetAddresses();
            
            while(inetAddresses.hasMoreElements()) {
                InetAddress inetAddress= (InetAddress) inetAddresses.nextElement();
                if (inetAddress instanceof Inet6Address) continue;
                addresses.add(inetAddress);
            }
        }
        return addresses;
    }
    
    public static InetAddress getIpFromDomain(String domain) {
        try {
            return InetAddress.getByName(domain);
        } catch (UnknownHostException ex) {}
        return null;
    }    
    
}
