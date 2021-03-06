package src;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import com.sun.xml.internal.fastinfoset.sax.SystemIdResolver;
import com.sun.xml.internal.ws.api.message.stream.InputStreamMessage;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.*;
import java.util.Arrays;

public class Client {

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
    private final static int DATA_SIZE = 512;

    private DatagramSocket datagramSocket = null;
    private DatagramSocket datagramSocketReception = null;
    private InetAddress inetAddress = null;
    private byte[] requestByteArray;
    private byte[] bufferByteArray;
    private DatagramPacket sendDatagramPacket;
    private DatagramPacket receiveDatagramPacket;
    public static InputStream is;

    public static int sizeOfFile;
    public static int remainingBytes;
    public static int sentBytes;
    public static int currentNumPacket;
    private boolean isRunning;

    public Client(String ip){
        try {
            inetAddress = InetAddress.getByName(ip);
            is = null;
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }

    public int prepareSendFile(String fileName) {
        int crem = 0;
        try {
            File currentFile = new File(fileName);
            sizeOfFile = (int)currentFile.length(); // en octets
            System.out.println("Taille du fichier : "+sizeOfFile);
            System.out.println("String : -- "+ currentFile.toString());
            System.out.println("Total space : -- "+ currentFile.getTotalSpace());
            System.out.println("Absolute path: -- "+ currentFile.getAbsolutePath());
            remainingBytes = sizeOfFile; // en bits

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
                    String message = new String(receiveDatagramPacket.getData(),"ASCII");
                    System.out.println(message);
                    //datagramSocket = new DatagramSocket(receiveDatagramPacket.getPort());
                    sendFile(fileName);
                }
                else {
                    String message = new String(receiveDatagramPacket.getData(),"ASCII");
                    System.out.println("Fonctionne pas ..." + fileName);
                    System.out.println(message);
                }
        } catch (SocketException ex) {
            crem = 1;
            ex.printStackTrace();
        } catch (IOException ex) {
            crem = -1;
            ex.printStackTrace();
        }
        
        return crem;
    }

    private boolean waitServerResponse() {
        // prepare packet to receive
        byte[] buffer = new byte[512]; // en octets
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

        try {
            // receive packet from TFTP server
            datagramSocket.receive(receivePacket);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Getting the first 4 characters (op_code) from the TFTP packet
        byte[] opCode = {buffer[0], buffer[1]};
        if (opCode[1] == OP_ACK) {
            return true;
        }
        return false;
    }

    private void sendFile(String fileName) {
        byte[] nextPartOfFile = new byte[512];
        int numBloc = 1;
        int nbBits = 0;
        sentBytes = 0;
        try {
            is = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(sentBytes < sizeOfFile) {
            try {
                nbBits = is.read(nextPartOfFile, 0, DATA_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendPartOfFile(nextPartOfFile, numBloc, nbBits);
            // ATTENDRE REPONSE serveur
            waitServerResponse();
            //sendPartOfFile(nextPartOfFile, numBloc);
            numBloc++;
            sentBytes += nbBits;
        }
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

    private void sendPartOfFile(byte[] partOfFile, int numBloc, int nbRead) {
        byte[] datas = createRequest(OP_DATA, numBloc, partOfFile, nbRead);
        System.out.println(Arrays.toString(datas));

        DatagramPacket dpData = new DatagramPacket(datas, datas.length, inetAddress,
                receiveDatagramPacket.getPort());
        try{
            datagramSocket.send(dpData);
        }catch(Exception exc){
            exc.getStackTrace();
        }
    }

    private byte[] createRequest(byte opCode, int numBloc, byte[] datas, int nbRead) {
        byte[] blockNumber = ByteBuffer.allocate(4).putInt(numBloc).array();
        byte[] request = new byte[4+nbRead];
        request[0]= 0;
        request[1]= OP_DATA;
        request[2] = blockNumber[2];
        request[3] = blockNumber[3];
        for(int index = 0; index< nbRead; index ++){
            request[4+index] = datas[index];
        }
		
        return request;

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