/**
 * Created by GregoirePiat on 19/05/16.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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


    public Client() throws IOException{

        String fileName = "";
        Client client = new Client();
        client.load(fileName);

    }

    private void load(String fileName) throws IOException {
        inetAddress = InetAddress.getByName(TFTP_SERVER_IP);
        datagramSocket = new DatagramSocket();
        requestByteArray = createRequest(OP_RRQ, fileName, "octet");
        sendDatagramPacket = new DatagramPacket(requestByteArray,
                requestByteArray.length, inetAddress, TFTP_DEFAULT_PORT);
        datagramSocket.send(sendDatagramPacket);
        ByteArrayOutputStream byteOutOS = receiveFile();
        writeFile(byteOutOS, fileName);
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