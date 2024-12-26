/**
 * Author: Khushi Bhuwania
 * Last Modified: Oct 7, 2024
 *
 * This class represents a UDP client that interacts with a server to perform operations
 * on remote variables. Each client has a unique ID, and the server maintains a sum for each client.
 * The client can add to, subtract from, or retrieve their sum.
 *
 * The client interacts with the server through UDP, sending requests and receiving responses.
 * The program exits when the user chooses the option to quit.
 */
import java.io.*;
import java.net.*;

public class RemoteVariableClientUDP {

    /**
     * Main method to run the UDP client for remote variable operations.
     *
     * @param args No command-line arguments expected.
     */
    private static int serverPort;

    public static void main(String[] args) {
        try {
            // Client is starting
            System.out.println("The client is running.");

            // User provides the server's port
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Please enter server port: ");
            serverPort = Integer.parseInt(br.readLine());

            String input;
            while (true) {
                System.out.println("1. Add a value to your sum.");
                System.out.println("2. Subtract a value from your sum.");
                System.out.println("3. Get your sum.");
                System.out.println("4. Exit client.");

                // Read the user's choice
                input = br.readLine();

                if ("4".equals(input.trim())) {
                    System.out.println("Client side quitting.");
                    break;
                }

                int id = 0, value = 0;
                String operation = "";

                // Determine which operation the user chose
                switch (input.trim()) {
                    case "1":  // Add operation
                        System.out.print("Enter value to add: ");
                        value = Integer.parseInt(br.readLine());
                        System.out.print("Enter your ID: ");
                        id = Integer.parseInt(br.readLine());
                        operation = "add";
                        break;
                    case "2":  // Subtract operation
                        System.out.print("Enter value to subtract: ");
                        value = Integer.parseInt(br.readLine());
                        System.out.print("Enter your ID: ");
                        id = Integer.parseInt(br.readLine());
                        operation = "subtract";
                        break;
                    case "3":  // Get operation
                        System.out.print("Enter your ID: ");
                        id = Integer.parseInt(br.readLine());
                        operation = "get";
                        break;
                    default:
                        System.out.println("Invalid option. Please choose a valid menu option.");
                        continue;
                }

                // Call the remote operation on the server
                int result = remoteOperation(id, operation, value);
                System.out.println("The result is: " + result);
            }
        } catch (IOException e) {
            System.err.println("IO Exception in Client: " + e.getMessage());
        }
    }

    /**
     * Sends a request to the server for the specified operation (add, subtract, or get)
     * and returns the result received from the server.
     *
     * @param id        The unique ID of the client.
     * @param operation The operation to perform (add, subtract, or get).
     * @param value     The value to add or subtract (ignored for "get" operation).
     * @return          The sum returned by the server.
     * @throws IOException If there is a network error.
     */

    // Proxy method to encapsulate communication with the server
    // The client calls this method to handle sending a request and receiving a response
    // This method abstracts the underlying network communication
    public static int remoteOperation(int id, String operation, int value) throws IOException {
        DatagramSocket aSocket = null;

        try {
            // Open the socket for communication with the server
            aSocket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName("localhost");

            // Create a request string with ID, operation, and value
            String requestString = id + "," + operation;
            if (!"get".equals(operation)) {
                requestString += "," + value;
            }
            byte[] requestData = requestString.getBytes();

            // Send the request to the server
            DatagramPacket request = new DatagramPacket(requestData, requestData.length, serverAddress, serverPort);
            aSocket.send(request);

            // Buffer to store the incoming sum from the server
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);

            // Convert the received sum back to an integer and return it
            String sumString = new String(reply.getData(), 0, reply.getLength()).trim();
            return Integer.parseInt(sumString);

        } finally {
            if (aSocket != null) {
                aSocket.close();
            }
        }
    }
}
