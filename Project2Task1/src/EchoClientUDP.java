import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class EchoClientUDP {
    public static void main(String args[]) {
        // DatagramSocket to send and receive packets
        DatagramSocket aSocket = null;

        try {
            // Client is starting
            System.out.println("The UDP client is running.");
            // Hardcoded address to localhost
            InetAddress aHost = InetAddress.getByName("localhost");

            // Reader to take user input for the server port
            BufferedReader typed = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter the port number of the server: ");
            // Parse the input port number
            int serverPort = Integer.parseInt(typed.readLine());
            // Create a new DatagramSocket
            aSocket = new DatagramSocket();

            String nextLine;
            // Loop to read input and send messages until "halt!" is received
            while ((nextLine = typed.readLine()) != null) {
                // Check if the input is "halt!"
                if ("halt!".equals(nextLine)) {
                    // Convert "halt!" message to bytes
                    byte[] m = nextLine.getBytes();
                    // Create request packet with "halt!" message
                    DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
                    // Sending the request packet
                    aSocket.send(request);
                    System.out.println("UDP Client side quitting");
                    break;
                }

                // Convert the input to bytes
                byte[] m = nextLine.getBytes();
                // Create request packet with the user input
                DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
                // Send the request packet
                aSocket.send(request);

                // Buffer to store the incoming data
                byte[] buffer = new byte[1000];
                // Create reply packet to receive the response
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                // Receive the reply packet
                aSocket.receive(reply);
                // Display the reply message, adjusting the length to avoid extra data
                System.out.println("Reply from server: " + new String(reply.getData(), 0, reply.getLength()));
            }
        } catch (SocketException e) {
            System.err.println("Socket Exception in Client: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Exception in Client: " + e.getMessage());
        } finally {
            if (aSocket != null) {
                aSocket.close();
            }
        }
    }
}
