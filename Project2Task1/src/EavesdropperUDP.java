/**
 * Author: Khushi Bhuwania
 * Last Modified: Oct 7, 2024
 *
 * This class represents an eavesdropper using UDP. It intercepts messages between a client and server,
 * modifies them if necessary, and forwards the modified messages to the server. It also forwards
 * the server's reply back to the client.
 */
import java.net.*;
import java.io.*;

public class EavesdropperUDP {
    /**
     * Main method to run the eavesdropper UDP intermediary.
     *
     * @param args No command-line arguments expected.
     */
    public static void main(String[] args) {
        // Socket to receive messages from the client
        DatagramSocket clientSocket = null;
        // Socket to send messages to the server
        DatagramSocket serverSocket = null;

        try {
            System.out.println("EavesdropperUDP is running");
            // Reader to take user input for ports
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            // Get the port to listen on for client
            System.out.print("Enter eavesdropper's listening port: ");
            int listeningPort = Integer.parseInt(input.readLine());
            // Get the server's port
            System.out.print("Enter the port number of the server: ");
            int serverPort = Integer.parseInt(input.readLine());

            // Hardcoded address to localhost
            InetAddress serverAddress = InetAddress.getByName("localhost");

            // Listen for client messages
            clientSocket = new DatagramSocket(listeningPort);
            // Send or receive messages to and from the server
            serverSocket = new DatagramSocket();

            // Buffer for receiving messages from the client
            byte[] receiveBuffer = new byte[1000];

            while (true) {
                // Receive message from client
                DatagramPacket clientPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                clientSocket.receive(clientPacket);

                // Convert the received byte array to a string
                String clientMessage = new String(clientPacket.getData(), 0, clientPacket.getLength());
                System.out.println("Message from client: " + clientMessage);

                // Modify the message if it contains like
                String modifiedMessage = clientMessage;
                if (clientMessage.contains(" like ")) {
                    modifiedMessage = clientMessage.replaceFirst(" like ", " dislike ");
                    System.out.println("Modified message sent to the server: " + modifiedMessage);
                }

                // Send the modified message to the server
                byte[] modifiedData = modifiedMessage.getBytes();
                DatagramPacket forwardToServerPacket = new DatagramPacket(modifiedData, modifiedData.length, serverAddress, serverPort);
                serverSocket.send(forwardToServerPacket);

                // Buffer for the server's response
                byte[] serverReplyBuffer = new byte[1000];
                DatagramPacket serverReplyPacket = new DatagramPacket(serverReplyBuffer, serverReplyBuffer.length);
                // Receive server's reply
                serverSocket.receive(serverReplyPacket);

                // Forward the server's reply to the client
                DatagramPacket forwardToClientPacket = new DatagramPacket(serverReplyPacket.getData(), serverReplyPacket.getLength(), clientPacket.getAddress(), clientPacket.getPort());
                clientSocket.send(forwardToClientPacket);

                // Log the server's reply
                String serverReply = new String(serverReplyPacket.getData(), 0, serverReplyPacket.getLength());
                System.out.println("Message from the server: " + serverReply);

                // Check for "halt!" command from the client while the eavesdropper continues running
                if (clientMessage.trim().equals("halt!")) {
                    System.out.println("Client requested halt");
                }
            }
        } catch (SocketException e) {
            System.err.println("Socket Exception in Eavesdropper: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Exception in Eavesdropper: " + e.getMessage());
        } finally {
            // Close the sockets when done
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}
