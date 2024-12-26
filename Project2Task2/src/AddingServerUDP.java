import java.net.*;
import java.io.*;

public class AddingServerUDP {

    // Initialising the running sum
    private static int sum = 0;

    public static void main(String[] args) {
        DatagramSocket aSocket = null;
        byte[] buffer = new byte[1000];

        try {
            System.out.println("Server started.");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter listening port number: ");
            int listeningPort = Integer.parseInt(br.readLine());
            aSocket = new DatagramSocket(listeningPort);

            while (true) {
                // Receive request from client
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);

                String requestString = new String(request.getData(), 0, request.getLength());

                // Check for "halt!"
                if (requestString.trim().equals("halt!")) {
                    System.out.println("Server quitting.");
                    break;
                }

                // Convert received data to an integer
                int receivedNumber = Integer.parseInt(requestString.trim());

                // Add to the running sum
                int previousSum = sum;
                sum = sum + receivedNumber;
                System.out.println("Adding: " + receivedNumber + " to " + previousSum);
                System.out.println("Returning sum of " + sum + " to client");

                // Convert the sum to a byte array and send it back to the client
                String sumString = String.valueOf(sum);
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
