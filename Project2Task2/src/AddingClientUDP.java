/**
 * Author: Khushi Bhuwania
 * Last Modified: 0ct 7, 2024
 *
 * This class represents a UDP client that sends integers to the server for summing.
 * The server adds the values received from the client and returns the running total.
 * The client exits when "halt!" is entered.
 */
import java.io.*;
import java.net.*;

public class AddingClientUDP {
    private static int serverPort;

    public static void main(String[] args) {
        try {
            System.out.println("The client is running.");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Please enter server port: ");
            serverPort = Integer.parseInt(br.readLine());

            String input;
            // User input loop
            while (true) {
                input = br.readLine();

                // If user inputs "halt!", break the loop and stop the client
                if ("halt!".equals(input.trim())) {
                    System.out.println("Client side quitting.");
                    break;
                }

                // Call the add method to send the number to the server and receive the sum
                int i = Integer.parseInt(input.trim());
                // Only passing the integer to the method
                int result = add(i);
                System.out.println("The server returned: " + result);
            }
        } catch (IOException e) {
            System.err.println("IO Exception in Client: " + e.getMessage());
        }
    }
    /**
     * Sends an integer to the server and receives the updated sum.
     *
     * @param i The integer to be added on the server.
     * @return The updated sum from the server.
     * @throws IOException
     */

    // Method to handle communication with the server and return the updated sum
    public static int add(int i) throws IOException {
        DatagramSocket aSocket = null;

        try {
            aSocket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName("localhost");

            // Convert the integer to a byte array and send to server
            String numberString = String.valueOf(i);
            byte[] requestData = numberString.getBytes();
            DatagramPacket request = new DatagramPacket(requestData, requestData.length, serverAddress, serverPort);
            aSocket.send(request);

            // Buffer to store the incoming sum from the server
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);

            // Convert the received sum back to an integer
            String sumString = new String(reply.getData(), 0, reply.getLength()).trim();
            return Integer.parseInt(sumString);

        } finally {
            if (aSocket != null) {
                aSocket.close();
            }
        }
    }
}
