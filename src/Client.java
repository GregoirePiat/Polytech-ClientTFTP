/**
 * Created by GregoirePiat on 19/05/16.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client implements Runnable{
    private InetAddress adr1;
    private DatagramSocket ds;
    private DatagramPacket dp;
    private DatagramPacket dpr;

    private boolean isRunning;

    public Client() {
        try {
            ds = new DatagramSocket();
        }
        catch(Exception e) {
            System.out.println("udpserver.Client - erreur de DatagramSocket");
        }
    }

    public void connect(String ipServ) {
        try {
            adr1 = InetAddress.getByName(ipServ) ;
            System.out.println(adr1);
        }
        catch (UnknownHostException ex){
            System.out.println("udpserver.Client - could not find server");
            ex.printStackTrace();
        }
    }

    public void send(String message, int servPort){
        try {
            byte[] data = message.getBytes("ASCII");
            DatagramPacket dp = new DatagramPacket(data, data.length,
                    adr1, servPort);
            ds.send(dp);
            System.out.println("udpserver.Client - message envoyé");
        }
        catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run(){
        isRunning = true;
        while(isRunning == true){
            start();
        }
    }

    public void stop() {
        isRunning = false;
    }

    public void start() {
        dpr = new DatagramPacket(new byte[128],128);
        try {
            ds.receive(dpr);
            System.out.println("udpserver.Client try - message reçu");
            //this.stop();
            this.print();
        }
        catch (IOException ex){
            //System.err.println("Serveur - Aucune donnée reçue");
            ex.printStackTrace();
            System.out.println("udpserver.Client catch - message reçu");
        }
        //ds.close();
    }

    public void print() {
        String message = "";
        try {
            message = new String(dpr.getData(), "ASCII");
        }
        catch(Exception e) {
            System.err.println("Err décodage message");
        }
        System.out.println("message  = " + message);
    }
}
