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
    private static final String MODE = "octet";

    // Taille max des paquets
    private final static int PACKET_SIZE = 516;

    private DatagramSocket datagramSocket = null;
    private DatagramSocket datagramSocketReception = null;
    private InetAddress inetAddress = null;
    private byte[] requestByteArray;
    private byte[] bufferByteArray;
    private DatagramPacket sendDatagramPacket;
    private DatagramPacket receiveDatagramPacket;

    public static int sizeOfFile;
    public static int remainingBytes;
    public static int currentNumPacket;
    private boolean isRunning;

    public Client(String ip){
        try {
            inetAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }

    public void prepareSendFile(String fileName) {

        try {
            //byte[] partOfFile = openFile(fileName, 0);

            datagramSocket = new DatagramSocket();
            receiveDatagramPacket = new DatagramPacket(new byte[516],516);
            requestByteArray = createRequest(OP_WRQ, fileName, MODE);

            sendDatagramPacket = new DatagramPacket(requestByteArray,
                    requestByteArray.length, inetAddress, SERVER_PORT);


            // STEP 1: sending request WRQ to TFTP server
            datagramSocket.send(sendDatagramPacket);

            // STEP 2: receive ACK from TFTP server
            datagramSocket.receive(receiveDatagramPacket);
                byte[] data = receiveDatagramPacket.getData();
                if((data[1]==4) && data[3] == 0){
                    System.out.println("Fonctionne" + receiveDatagramPacket.getData().toString());
                }
                else {

                    String message = new String(receiveDatagramPacket.getData(),"ASCII");
                    System.out.println("Fonctionne pas ...");
                    System.out.println(message);
                }


            //while(!waitServerResponse()) {
                //sendFile(fileName);
            //}
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
        int bloc = 0;
        int numOctet = 0;
        boolean over = false;

        while(!over) {
            byte[] partOfFile = openFile(fileName, numOctet);

            createRequest(OP_DATA,fileName,MODE);
            bloc++;
            numOctet+=512;
        }
    }

    private static byte[] openFile(String file, int offset) {
        File currentFile = new File("/Users/GregoirePiat/Documents/test.docx");
        sizeOfFile = (int)currentFile.length();
        InputStream is;
        byte[] partOfFile = null;
        if(remainingBytes>=512){
            partOfFile = new byte[516];
        }
        else{
            partOfFile = new byte[remainingBytes + 4];
        }

        try {
            is = new FileInputStream(file);
            is.read(partOfFile, offset, 512);
            System.out.println(Arrays.toString(partOfFile));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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