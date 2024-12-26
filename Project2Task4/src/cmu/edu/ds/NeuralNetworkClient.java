package cmu.edu.ds;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import com.google.gson.*;

/**
 * Author: Khushi Bhuwania
 * Date: Oct 7, 2024
 *
 * Description:
 * This class implements the client side of a distributed neural network application.
 * The client communicates with a server using JSON messages over UDP to perform operations
 * on a neural network such as training, testing, and fetching current range values.
 * The client provides a menu for user interaction and sends corresponding requests
 * to the server based on the user input.
 */

public class NeuralNetworkClient {
    private final int serverPort;  // Port to communicate with the server
    private final DatagramSocket socket;  // Datagram socket for sending/receiving UDP packets
    private final InetAddress serverAddress;  // Server address (hostname or IP)

    /**
     * Constructor to initialize the client with server address and port.
     *
     * @param hostname The hostname of the server ("localhost")
     * @param port The port number the server is listening on
     * @throws UnknownHostException If the server address cannot be resolved
     * @throws SocketException If there is an error creating the socket
     */
    public NeuralNetworkClient(String hostname, int port) throws UnknownHostException, SocketException {
        this.serverPort = port;
        this.socket = new DatagramSocket();
        this.serverAddress = InetAddress.getByName(hostname);
    }

    /**
     * Sends a JSON request to the server and handles the response.
     *
     * @param requestJson The JSON object representing the request to be sent to the server
     * @throws IOException If there is an I/O error during communication
     */
    public void sendRequest(JsonObject requestJson) throws IOException {
        // Convert the JSON request to a byte array and send the packet
        byte[] requestData = requestJson.toString().getBytes();
        DatagramPacket sendPacket = new DatagramPacket(requestData, requestData.length, serverAddress, serverPort);
        socket.send(sendPacket);

        // Buffer to receive the server's response
        byte[] receiveBuffer = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        socket.receive(receivePacket);

        // Parse and handle the server's response
        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
        handleResponse(response);
    }

    /**
     * Handles the JSON response from the server and processes the returned values.
     *
     * @param response The JSON response string received from the server
     */
    private void handleResponse(String response) {
        // Parse the response JSON
        JsonObject responseObj = JsonParser.parseString(response).getAsJsonObject();
        String operation = responseObj.get("response").getAsString();

        // If the response contains values, process them based on the operation
        if (responseObj.has("values")) {
            JsonArray valuesArray = responseObj.getAsJsonArray("values");
            handleOperationResponse(operation, valuesArray);
        }
    }

    /**
     * Processes the operation response by printing the corresponding data.
     *
     * @param operation The operation type
     * @param valuesArray The array of values returned from the server
     */
    private void handleOperationResponse(String operation, JsonArray valuesArray) {
        switch (operation) {
            case "getCurrentRange":
            case "setCurrentRange":
                displayTable(valuesArray);  // Display the current truth table
                break;
            case "train":
                double trainingError = valuesArray.get(0).getAsDouble();  // Display training error
                System.out.println("Training completed. Error = " + trainingError);
                break;
            case "test":
                double predictedValue = valuesArray.get(0).getAsDouble();  // Display test result
                System.out.println("The predicted value is approximately " + predictedValue);
                break;
            default:
                System.out.println("Unknown operation: " + operation);
        }
    }

    /**
     * Displays the current truth table based on the values received from the server.
     *
     * @param values The array of truth table values
     */
    private void displayTable(JsonArray values) {
        System.out.println("Truth table:");
        System.out.println("0.0 0.0 " + values.get(0).getAsDouble());
        System.out.println("0.0 1.0 " + values.get(1).getAsDouble());
        System.out.println("1.0 0.0 " + values.get(2).getAsDouble());
        System.out.println("1.0 1.0 " + values.get(3).getAsDouble());
    }

    /**
     * Displays the main menu options for user interaction.
     */
    private static void displayMenu() {
        System.out.println("\nUsing a neural network to learn a truth table.\nMain Menu");
        System.out.println("0. Display the current truth table.");
        System.out.println("1. Set new truth table values.");
        System.out.println("2. Perform a single training step.");
        System.out.println("3. Perform multiple training steps.");
        System.out.println("4. Test the network with input values.");
        System.out.println("5. Exit.");
    }

    /**
     * Creates a JSON request based on the user's menu choice.
     *
     * @param choice The user's choice from the menu
     * @param scanner Scanner object to read user input
     * @return JsonObject representing the request
     */
    private JsonObject createRequest(String choice, Scanner scanner) {
        JsonObject requestObj = new JsonObject();
        switch (choice) {
            case "0":
                requestObj.addProperty("request", "getCurrentRange");
                break;
            case "1":
                handleSetRangeRequest(requestObj, scanner);
                break;
            case "2":
                requestObj.addProperty("request", "train");
                requestObj.addProperty("iterations", 1);
                break;
            case "3":
                handleTrainRequest(requestObj, scanner);
                break;
            case "4":
                handleTestRequest(requestObj, scanner);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                break;
        }
        return requestObj;
    }

    /**
     * Handles the "setCurrentRange" operation and collects truth table values from the user.
     *
     * @param requestObj The JSON object to store the request data
     * @param scanner Scanner object to read user input
     */
    private void handleSetRangeRequest(JsonObject requestObj, Scanner scanner) {
        requestObj.addProperty("request", "setCurrentRange");
        System.out.println("Enter the four results of a 4 by 2 truth table. Each value should be 0 or 1.");
        double val1 = scanner.nextDouble();
        double val2 = scanner.nextDouble();
        double val3 = scanner.nextDouble();
        double val4 = scanner.nextDouble();
        requestObj.addProperty("val1", val1);
        requestObj.addProperty("val2", val2);
        requestObj.addProperty("val3", val3);
        requestObj.addProperty("val4", val4);
        scanner.nextLine();
    }

    /**
     * Handles the "train" operation and collects the number of iterations from the user.
     *
     * @param requestObj The JSON object to store the request data
     * @param scanner Scanner object to read user input
     */
    private void handleTrainRequest(JsonObject requestObj, Scanner scanner) {
        System.out.print("Enter number of iterations: ");
        int iterations = scanner.nextInt();
        requestObj.addProperty("request", "train");
        requestObj.addProperty("iterations", iterations);
        scanner.nextLine();
    }

    /**
     * Handles the "test" operation and collects the test inputs from the user.
     *
     * @param requestObj The JSON object to store the request data
     * @param scanner Scanner object to read user input
     */
    private void handleTestRequest(JsonObject requestObj, Scanner scanner) {
        System.out.println("Enter a pair of inputs (e.g., 0 and 1):");
        double testVal1 = scanner.nextDouble();
        double testVal2 = scanner.nextDouble();
        requestObj.addProperty("request", "test");
        requestObj.addProperty("val1", testVal1);
        requestObj.addProperty("val2", testVal2);
        scanner.nextLine();
    }

    /**
     * Main method to start the client and provide the menu-driven interaction with the user.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            NeuralNetworkClient client = new NeuralNetworkClient("localhost", 6789);
            Scanner scanner = new Scanner(System.in);
            boolean isRunning = true;

            // Run the client until the user chooses to exit
            while (isRunning) {
                displayMenu();
                String choice = scanner.nextLine();
                if (choice.equals("5")) {
                    isRunning = false;
                    break;
                }
                // Create the request based on the user's choice and send it to the server
                JsonObject request = client.createRequest(choice, scanner);
                if (!request.has("request")) {
                    continue;
                }
                client.sendRequest(request);
            }
            System.out.println("Client exiting.");
        } catch (IOException e) {
            System.err.println("Client Error: " + e.getMessage());
        }
    }
}
// used Chatgpt for documentation