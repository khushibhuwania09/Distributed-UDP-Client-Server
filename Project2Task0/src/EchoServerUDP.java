/**
 * Author: Khushi Bhuwania
 * Last Modified: Oct 7, 2024
 * This class represents a simple UDP server that listens for messages from clients.
 * It echoes the received messages back to the client. The server exits when it receives "halt!" from the client.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class EchoServerUDP {
    /**
     * Main method to run the UDP server.
     *
     * @param args No command-line arguments expected.
     */
    public static void main(String args[]) {
        // DatagramSocket to receive and send packets
        DatagramSocket aSocket = null;
        // Buffer to store incoming data
        byte[] buffer = new byte[1000];

        try {
            // Server is starting
            System.out.println("The UDP server is running.");
            // Reader to take user input for the listening port
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter listening port number: ");
            // Parse the input port number
            int listeningPort = Integer.parseInt(br.readLine());
            // Bind the socket to the given port
            aSocket = new DatagramSocket(listeningPort);

            while (true) {
                // DatagramPacket to receive data
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                // Receive the incoming packet
                aSocket.receive(request);

                // Extract the string from the received data with the correct length
                String requestString = new String(request.getData(), 0, request.getLength());
                // Display the received message from the client
                System.out.println("Echoing: " + requestString);

                // Check if the message is "halt!"
                if (requestString.trim().equals("halt!")) {
                    System.out.println("UDP Server side quitting");
                    // Convert "halt!" message to bytes
                    byte[] m = "halt!".getBytes();
                    // Prepare reply packet with "halt!" message
                    DatagramPacket haltReply = new DatagramPacket(m, m.length, request.getAddress(), request.getPort());
                    // Send the reply packet
                    aSocket.send(haltReply);
                    break;
                }

                // Reply to the client with the received data
                DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort());
                aSocket.send(reply);
            }
        } catch (SocketException e) {
            System.err.println("Socket Exception in Server: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Exception in Server: " + e.getMessage());
        } finally {
            if (aSocket != null) {
                aSocket.close();
            }
        }
    }
}
