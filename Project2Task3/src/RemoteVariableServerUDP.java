/**
 * Author: Khushi Bhuwania
 * Last Modified: Oct 7, 2024
 *
 * This class represents a UDP server that manages remote variables for multiple clients.
 * Each client has a unique ID, and the server maintains a sum for each client.
 * The server processes requests from clients to add, subtract, or retrieve their sum.
 * The program runs continuously, listening for requests from clients.
 */
import java.net.*;
import java.io.*;
import java.util.TreeMap;

public class RemoteVariableServerUDP {
    /**
     * Main method to run the UDP server that manages remote variables for clients.
     *
     * @param args No command-line arguments expected.
     */
    // TreeMap to store each client ID and their respective sum
    private static TreeMap<Integer, Integer> idToSum = new TreeMap<>();

    public static void main(String[] args) {
        DatagramSocket aSocket = null;
        byte[] buffer = new byte[1000];

        try {
            System.out.println("Server started.");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter listening port number: ");
            int listeningPort = Integer.parseInt(br.readLine());

            // Bind the server to the provided port
            aSocket = new DatagramSocket(listeningPort);

            while (true) {
                // Buffer to store incoming data
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);

                // Receive request from client
                aSocket.receive(request);

                // Convert the received data to a string
                String requestString = new String(request.getData(), 0, request.getLength());

                // Split the request string to extract the ID, operation, and value
                String[] requestParts = requestString.split(",");
                int id = Integer.parseInt(requestParts[0]); // Client ID
                String operation = requestParts[1].trim(); // Operation to perform (add, subtract, or get)
                int value = requestParts.length > 2 ? Integer.parseInt(requestParts[2].trim()) : 0; // Value to add/subtract, if applicable

                // Ensure the ID is associated with a sum and if the ID is new, initialize it with a sum of 0
                idToSum.putIfAbsent(id, 0);

                // Perform the requested operation
                int currentSum = idToSum.get(id);
                int newSum = currentSum;

                switch (operation) {
                    case "add":
                        newSum += value;
                        idToSum.put(id, newSum);
                        System.out.println("ID: " + id + " | Adding: " + value + " | New Sum: " + newSum);
                        break;
                    case "subtract":
                        newSum -= value;
                        idToSum.put(id, newSum);
                        System.out.println("ID: " + id + " | Subtracting: " + value + " | New Sum: " + newSum);
                        break;
                    case "get":
                        System.out.println("ID: " + id + " | Getting Sum: " + newSum);
                        break;
                }

                // Send the result back to the client
                String sumString = String.valueOf(newSum);
                byte[] replyData = sumString.getBytes();
                DatagramPacket reply = new DatagramPacket(replyData, replyData.length, request.getAddress(), request.getPort());
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
