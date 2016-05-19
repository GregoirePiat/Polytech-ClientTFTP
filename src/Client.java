/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.*;

public class Client{
    private static final String TFTP_SERVER_IP = "127.0.0.1";
    private static final int TFTP_DEFAULT_PORT = 69;


    // Codes informant sur les types de paquet
    private static final byte OP_RRQ = 1;
    private static final byte OP_WRQ = 2;
    private static final byte OP_DATA = 3;
    private static final byte OP_ACK = 4;
    private static final byte OP_ERROR = 5;

    // Taille max des paquets
    private final static int PACKET_SIZE = 516;

    private DatagramSocket datagramSocket = null;
    private InetAddress inetAddress = null;
    private byte[] requestByteArray;
    private byte[] bufferByteArray;
    private DatagramPacket sendDatagramPacket;
    private DatagramPacket receiveDatagramPacket;



    public Client(){

        try{
            String file = "/Users/GregoirePiat/Documents/yolo.rtf";
            openFile(file, 0);
        }
        catch (Exception exce){
            exce.printStackTrace();
        }
    }

    private static void openFile(String file, int offset) {
        InputStream is;
        byte[] partOfFile = null;

        try {
            is = new FileInputStream(file);
            is.read(partOfFile, offset, 512);
            System.out.println(partOfFile.toString());
            System.out.println("Total file size to read (in bytes) : "
                    + is.available());

            int content;
            while ((content = is.read()) != -1) {
                // convert to char and display it
                System.out.print((char) content);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private byte[] createRequest(final byte opCode, final String fileName,
                                 final String mode) {
        byte zeroByte = 0;
        int rrqByteLength = 2 + fileName.length() + 1 + mode.length() + 1;
        byte[] rrqByteArray = new byte[rrqByteLength];

        int position = 0;
        rrqByteArray[position] = zeroByte;
        position++;
        rrqByteArray[position] = opCode;
        position++;
        for (int i = 0; i < fileName.length(); i++) {
            rrqByteArray[position] = (byte) fileName.charAt(i);
            position++;
        }
        rrqByteArray[position] = zeroByte;
        position++;
        for (int i = 0; i < mode.length(); i++) {
            rrqByteArray[position] = (byte) mode.charAt(i);
            position++;
        }
        rrqByteArray[position] = zeroByte;
        return rrqByteArray;
    }

    public void sendWRQ(){

    }

    public void sendAck(byte[] blockNumber) {

        byte[] ACK = { 0, OP_ACK, blockNumber[0], blockNumber[1] };

        DatagramPacket dpAck = new DatagramPacket(ACK, ACK.length, inetAddress,
                receiveDatagramPacket.getPort());
        try {
            datagramSocket.send(dpAck);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}