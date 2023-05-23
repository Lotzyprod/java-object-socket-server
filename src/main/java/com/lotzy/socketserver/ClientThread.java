package com.lotzy.socketserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class ClientThread extends Thread {
    public interface ClientListener {
        default public void onConnection(ClientThread client) {};
        default public void onDisconnection(ClientThread client) {};
        default public void onReceivePacket(ClientThread client, Object object) {};
        default public void onSendPacket(ClientThread client,Object object, boolean sended) {};
        default public void onInvalidPacket(ClientThread client) {};
        default public void onClose(ClientThread client) {};
    }
    
    Socket socket = null;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
    
    boolean connected = false;
    
    private List<ClientListener> listeners = new ArrayList();
    
    public void addListener(ClientListener  toAdd) {
        listeners.add(toAdd);
    }
    
    public boolean isConnected() {
        return this.connected;
    }
    
    public Socket getSocket() {
        return this.socket;
    }
    
    public ClientThread(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.connected = true;
    }
    
    @Override
    public void run() {
        for(ClientListener  listener : this.listeners)
            listener.onConnection(this);
        while(this.connected) {
            try {
                Object packet = in.readObject();
                for(ClientListener  listener : listeners)
                    listener.onReceivePacket(this,packet);
            } catch (ClassNotFoundException ex) {
                for(ClientListener  listener : listeners)
                    listener.onInvalidPacket(this);
            } catch (IOException ex) {
                disconnect();
                for(ClientListener listener : listeners)
                    listener.onDisconnection(this);
            }
        }
    }
    
    public boolean sendObject(Object object) {   
        if (this.socket != null) {
            try {
                this.out.writeObject(object);
                this.out.flush();
                for(ClientListener listener : listeners)
                    listener.onSendPacket(this, object, true);
                return true;
            } catch (IOException ex) {
                disconnect();
                for(ClientListener listener : listeners)
                    listener.onDisconnection(this);
            } 
        }
        for(ClientListener listener : listeners)
            listener.onSendPacket(this, object, false);
        return false;
    }
    
    private void disconnect() {
        this.connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException ex) { }
        
    }
    
    public void close() {
        disconnect();
        for(ClientListener listener : listeners)
            listener.onClose(this);
    }
    
    
}
