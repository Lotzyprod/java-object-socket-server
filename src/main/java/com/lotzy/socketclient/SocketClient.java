package com.lotzy.socketclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketClient extends Thread {
    public interface ClientListener {
        default public void onConnection(InetSocketAddress address) {};
        default public void onDisconnection() {};
        default public void onReceivePacket(Object object) {};
        default public void onSendPacket(Object object, boolean sended) {};
        default public void onInvalidPacket() {};
        default public void onReconnection() {};
        default public void onClose() {};
    }
    
    String host;
    int port;
    
    Socket socket = null;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
    
    boolean connected = false;
    
    int reconnectionTime;
    
    private List<ClientListener> listeners = new ArrayList();
    
    public SocketClient(String host, int port, int reconnectionTime) {
        this.host = host;
        this.port = port;
        this.reconnectionTime = reconnectionTime;
    }
    
    public void addListener(ClientListener toAdd) {
        listeners.add(toAdd);
    }
    
    public boolean isConnected() {
        return this.connected;
    }
    
    @Override
    public void run() {
        reconnect();
        while(true) {
            try {
                try {
                    Object packet = in.readObject();
                    for(ClientListener listener : listeners)
                        listener.onReceivePacket(packet);
                } catch (ClassNotFoundException ex) {
                    for(ClientListener listener : listeners)
                        listener.onInvalidPacket();
                }
            } catch (IOException ex) {
                for(ClientListener listener : listeners)
                    listener.onDisconnection();
                reconnect();
            }
        }
    }
    
    public void reconnect() {
        this.connected = false;
        try {
            if (this.out != null) this.out.close();
            if (this.in != null) this.in.close();
            if (this.socket != null) this.socket.close();
        } catch (IOException ex) {}
        while (true) {
            try {
                this.socket = new Socket(this.host, this.port);
                this.out = new ObjectOutputStream(socket.getOutputStream());
                this.in = new ObjectInputStream(socket.getInputStream());
                this.connected = true;
                for(ClientListener listener : listeners)
                    listener.onConnection((InetSocketAddress)this.socket.getRemoteSocketAddress());
                return;
            } catch (IOException ex) {
                try {
                    for(ClientListener listener : listeners)
                        listener.onReconnection();
                    Thread.sleep(this.reconnectionTime);
                } catch (InterruptedException ex1) {}
            }
        }
    }
    
    public boolean sendObject(Object object) {   
        if (this.socket != null && this.out != null) {
            try {
                this.out.writeObject(object);
                for(ClientListener listener : listeners)
                    listener.onSendPacket(object, true);
                return true;
            } catch (IOException ex) {} 
        }
        for(ClientListener listener : listeners)
            listener.onSendPacket(object, false);
        return false;
    }
    
    public void close() {
        try {
            if (out != null) this.out.close();
            if (in != null) this.in.close();
             if (socket != null) socket.close();
        } catch (IOException ex) {}
        for(ClientListener listener : listeners)
            listener.onClose();
        if (!this.isInterrupted()) this.interrupt();
    }
}

