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
import java.util.Arrays;

public class Client{

    private static final int SERVER_PORT = 69;

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

    public Client(String ip){
        try {
            inetAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }

    public void prepareSendFile(String fileName) {

        try {
            openFile(fileName, 0);

            datagramSocket = new DatagramSocket();
            requestByteArray = createRequest(OP_WRQ, fileName, "octet");

            sendDatagramPacket = new DatagramPacket(requestByteArray,
                    requestByteArray.length, inetAddress, SERVER_PORT);


            // STEP 1: sending request WRQ to TFTP server
            datagramSocket.send(sendDatagramPacket);

            // STEP 2: receive ACK from TFTP server
            while(!waitServerResponse()) {

                // STEP 3: send file to the server
                sendFile(fileName);
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean waitServerResponse() {
        // prepare packet to receive
        byte[] buffer = new byte[516];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

        try {
            // receive packet from TFTP server
            datagramSocket.receive(receivePacket);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Getting the first 4 characters (op_code) from the TFTP packet
        byte[] opCode = { buffer[0], buffer[1] };

        if (opCode[1] == OP_ACK) {
            return true;
        }
        return false;
    }

    private void sendFile(String fileName) {
        int n = 0;
        boolean over = false;

        while(!over) {
            openFile(fileName, n);
            n++;
        }
    }

    private static byte[] openFile(String file, int offset) {
        InputStream is;
        byte[] partOfFile = new byte[516];

        try {
            is = new FileInputStream(file);
            is.read(partOfFile, offset, 512);
            System.out.println(Arrays.toString(partOfFile));

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return partOfFile;
    }

    private byte[] createRequest(byte opCode, String fileName, String mode) {
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