package cmu.edu.ds;

import java.io.*;
import java.net.*;
import java.util.*;
import com.google.gson.*;

public class NeuralNetworkServer {

    /**
     *
     * Description:
     * This class represents the server side of a distributed neural network application.
     * The server listens for requests from clients over UDP, processes those requests
     * (such as training the neural network, setting the current range, or testing the network),
     * and sends responses back to the client in JSON format. The neural network is maintained
     * entirely on the server, which handles requests for multiple clients and trains the network
     * based on truth table data for simple logic gates.
     */

    private static NeuralNetwork neuralNetwork;
    private static ArrayList<Double[][]> trainingSets;
    private static final Gson gson = new Gson();

    // Initialize the neural network and training sets
    static {
        initializeDefaultTrainingData();
    }

    // Initialize default neural network and training data
    private static void initializeDefaultTrainingData() {
        neuralNetwork = new NeuralNetwork(2, 5, 1, null, null, null, null);
        trainingSets = new ArrayList<>(Arrays.asList(
                new Double[][]{{0.0, 0.0}, {0.0}},
                new Double[][]{{0.0, 1.0}, {0.0}},
                new Double[][]{{1.0, 0.0}, {0.0}},
                new Double[][]{{1.0, 1.0}, {0.0}}
        ));
    }

    public static void main(String[] args) {
        final int port = 6789;
        byte[] buffer = new byte[1024];

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String jsonRequest = new String(packet.getData(), 0, packet.getLength());

                String jsonResponse = processRequest(jsonRequest);
                byte[] sendData = jsonResponse.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                socket.send(sendPacket);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Process the client request and generate a response
    private static String processRequest(String jsonRequest) {
        JsonObject requestObj = gson.fromJson(jsonRequest, JsonObject.class);
        String requestType = requestObj.get("request").getAsString();

        switch (requestType) {
            case "getCurrentRange":
                return getCurrentRange();
            case "setCurrentRange":
                return setCurrentRange(requestObj);
            case "train":
                int iterations = requestObj.has("iterations") ? requestObj.get("iterations").getAsInt() : 10000;
                return train(iterations);
            case "test":
                double input1 = requestObj.get("val1").getAsDouble();
                double input2 = requestObj.get("val2").getAsDouble();
                return test(input1, input2);
            default:
                return gson.toJson(new Response("error", "Invalid request", null));
        }
    }

    private static String getCurrentRange() {
        Double[] currentRange = new Double[4];
        for (int i = 0; i < trainingSets.size(); i++) {
            currentRange[i] = trainingSets.get(i)[1][0];
        }
        System.out.println(gson.toJson(new Response("getCurrentRange", "OK", currentRange)));
        return gson.toJson(new Response("getCurrentRange", "OK", currentRange));
    }

    private static String setCurrentRange(JsonObject requestObj) {
        Double val1 = requestObj.get("val1").getAsDouble();
        Double val2 = requestObj.get("val2").getAsDouble();
        Double val3 = requestObj.get("val3").getAsDouble();
        Double val4 = requestObj.get("val4").getAsDouble();

        trainingSets = new ArrayList<>(Arrays.asList(
                new Double[][]{{0.0, 0.0}, {val1}},
                new Double[][]{{0.0, 1.0}, {val2}},
                new Double[][]{{1.0, 0.0}, {val3}},
                new Double[][]{{1.0, 1.0}, {val4}}
        ));
        neuralNetwork = new NeuralNetwork(2, 5, 1, null, null, null, null); // Reset network with new data
        System.out.println(gson.toJson(new Response("setCurrentRange", "OK", null)));
        return gson.toJson(new Response("setCurrentRange", "OK", null));
    }

    private static String train(int iterations) {
        Random rand = new Random();
        for (int i = 0; i < iterations; i++) {
            int choice = rand.nextInt(trainingSets.size());
            List<Double> inputs = Arrays.asList(trainingSets.get(choice)[0]);
            List<Double> outputs = Arrays.asList(trainingSets.get(choice)[1]);
            neuralNetwork.train(inputs, outputs);
        }
        double totalError = neuralNetwork.calculateTotalError(trainingSets);
        System.out.println(gson.toJson(new Response("train", "OK", new Double[]{totalError})));
        return gson.toJson(new Response("train", "OK", new Double[]{totalError}));
    }

    private static String test(double input1, double input2) {
        List<Double> outputs = neuralNetwork.feedForward(Arrays.asList(input1, input2));
        System.out.println(gson.toJson(new Response("test", "OK", new Double[]{outputs.get(0)})));
        return gson.toJson(new Response("test", "OK", new Double[]{outputs.get(0)}));
    }

    // Simple Response class for serializing responses
    private static class Response {
        String response;
        String status;
        Double[] values;

        public Response(String response, String status, Double[] values) {
            this.response = response;
            this.status = status;
            this.values = values;
        }
    }
}


// The following code is adapted from NeuralNetwork.java with some modifications.
// The main method and menu() method have been removed to integrate with the client-server architecture.

class Neuron {
    private double bias;
    public List<Double> weights;
    public List<Double> inputs;
    double output;
    // Construct a neuron with a bias and reserve memory for its weights.
    public Neuron(double bias) {
        this.bias = bias;
        weights = new ArrayList<Double>();
    }
    //Calculate the output by using the inputs and weights already provided.
    //Squash the result so the output is between 0 and 1.
    public double calculateOutput(List<Double> inputs) {

        this.inputs = inputs;

        output = squash(calculateTotalNetInput());
        return output;
    }
    // Compute the total net input from the input, weights, and bias.
    public double calculateTotalNetInput() {

        double total = 0.0;
        for (int i = 0; i < inputs.size(); i++) {
            total += inputs.get(i) * weights.get(i);
        }
        return total + bias;
    }

    // This is the activation function, returning a value between 0 and 1.
    public double squash(double totalNetInput) {
        double v = 1.0 / (1.0 + Math.exp(-1.0 * totalNetInput));
        return v;
    }
    // Compute the partial derivative of the error with respect to the total net input.
    public Double calculatePDErrorWRTTotalNetInput(double targetOutput) {
        return calculatePDErrorWRTOutput(targetOutput) * calculatePDTotalNetInputWRTInput();
    }
    // Calculate error. How different are we from the target?
    public Double calculate_error(Double targetOutput) {
        double theError = 0.5 * Math.pow(targetOutput - output, 2.0);
        return theError;
    }
    // Compute the partial derivative of the error with respect to the output.
    public Double calculatePDErrorWRTOutput(double targetOutput) {
        return (-1) * ( targetOutput - output);
    }
    // Compute the partial derivative of the total net input with respect to the input.
    public Double  calculatePDTotalNetInputWRTInput() {
        return output * ( 1.0 - output);

    }
    // Calculate the partial derivative of the total net input with respect to the weight.
    public Double calculatePDTotalNetInputWRTWeight(int index) {
        return inputs.get(index);
    }
}

// The Neuron layer represents a collection of neurons.
// All neurons in the same layer have the same bias.
// We include in each layer the number of neurons and the list of neurons.
class NeuronLayer {
    private double bias;
    private int numNeurons;

    public List<Neuron> neurons;

    // Construct by specifying the number of neurons and the bias that applies to all the neurons in this layer.
    // If the bias is not provided, choose a random bias.
    // Create neurons for this layer and set the bias in each neuron.
    public NeuronLayer(int numNeurons, Double bias) {
        if(bias == null) {

            this.bias = new Random().nextDouble();
        }
        else {
            this.bias = bias;
        }
        this.numNeurons = numNeurons;
        this.neurons  = new ArrayList<Neuron>();
        for(int i = 0; i < numNeurons; i++) {
            this.neurons.add(new Neuron(this.bias));
        }
    }
    // Display the neuron layer by displaying each neuron.
    public String toString() {
        String s = "";
        s = s + "Neurons: " + neurons.size() + "\n";
        for(int n = 0; n < neurons.size(); n++) {
            s = s + "Neuron " + n + "\n";
            for (int w = 0; w < neurons.get(n).weights.size(); w++) {
                s = s + "\tWeight: " + neurons.get(n).weights.get(w) + "\n";
            }
            s = s + "\tBias " + bias + "\n";
        }

        return s;
    }

    // Feed the input data into the neural network and produce some output in the output layer.
    // Return a list of outputs. There may be a single output in the output list.
    List<Double> feedForward(List<Double> inputs) {

        List<Double> outputs = new ArrayList<Double>();

        for(Neuron neuron : neurons ) {

            outputs.add(neuron.calculateOutput(inputs));
        }

        return outputs;
    }
    // Return a list of outputs from this layer.
    // We do this by gathering the output of each neuron in the layer.
    // This is returned as a list of Doubles.
    // This is not used in this program.
    List<Double> getOutputs() {
        List<Double> outputs = new ArrayList<Double>();
        for(Neuron neuron : neurons ) {
            outputs.add(neuron.output);
        }
        return outputs;
    }
}

// The NeuralNetwork class represents two layers of neurons - a hidden layer and an output layer.
// We also include the number of inputs and the learning rate.
// The learning rate determines the step size by which the network’s weights are
// updated during each iteration of training. This is typically chosen experimentally.

class NeuralNetwork {

    // The learning rate is chosen experimentally. Typically, it is set between 0 and 1.
    private double LEARNING_RATE = 0.5;
    // This truth table example will have two inputs.
    private int numInputs;

    // This neural network will be built from two layers of neurons.
    private NeuronLayer hiddenLayer;
    private NeuronLayer outputLayer;

    // The neural network is constructed by specifying the number of inputs, the number of neurons in the hidden layer,
    // the number of neurons in the output layer, the hidden layer weights, the hidden layer bias,
    // the output layer weights and output layer bias.
    public NeuralNetwork(int numInputs, int numHidden, int numOutputs, List<Double> hiddenLayerWeights, Double hiddenLayerBias,
                         List<Double> outputLayerWeights, Double outputLayerBias) {
        // How many inputs to this neural network
        this.numInputs = numInputs;

        // Create two layers, one hidden layer and one output layer.
        hiddenLayer = new NeuronLayer(numHidden, hiddenLayerBias);
        outputLayer = new NeuronLayer(numOutputs, outputLayerBias);

        initWeightsFromInputsToHiddenLayerNeurons(hiddenLayerWeights);

        initWeightsFromHiddenLayerNeuronsToOutputLayerNeurons(outputLayerWeights);
    }

    // The hidden layer neurons have weights that are assigned here. If the actual weights are not
    // provided, random weights are generated.
    public void initWeightsFromInputsToHiddenLayerNeurons(List<Double> hiddenLayerWeights) {

        int weightNum = 0;
        for (int h = 0; h < hiddenLayer.neurons.size(); h++) {
            for (int i = 0; i < numInputs; i++) {
                if (hiddenLayerWeights == null) {
                    hiddenLayer.neurons.get(h).weights.add((new Random()).nextDouble());
                } else {
                    hiddenLayer.neurons.get(h).weights.add(hiddenLayerWeights.get(weightNum));
                }
                weightNum = weightNum + 1;
            }
        }
    }

    // The output layer neurons have weights that are assigned here. If the actual weights are not
    // provided, random weights are generated.
    public void initWeightsFromHiddenLayerNeuronsToOutputLayerNeurons(List<Double> outputLayerWeights) {
        int weightNum = 0;
        for (int o = 0; o < outputLayer.neurons.size(); o++) {
            for (int h = 0; h < hiddenLayer.neurons.size(); h++) {
                if (outputLayerWeights == null) {
                    outputLayer.neurons.get(o).weights.add((new Random()).nextDouble());
                } else {
                    outputLayer.neurons.get(o).weights.add(outputLayerWeights.get(weightNum));
                }
                weightNum = weightNum + 1;
            }
        }
    }

    // Display a NeuralNetwork object by calling the toString on each layer.
    public String toString() {
        String s = "";
        s = s + "-----\n";
        s = s + "* Inputs: " + numInputs + "\n";
        s = s + "-----\n";

        s = s + "Hidden Layer\n";
        s = s + hiddenLayer.toString();
        s = s + "----";
        s = s + "* Output layer\n";
        s = s + outputLayer.toString();
        s = s + "-----";
        return s;
    }

    // Feed the inputs provided into the network and get outputs.
    // The inputs are provided to the hidden layer. The hidden layer's outputs
    // are provided as inputs the output layer. The outputs of the output layer
    // are returned to the caller as a list of outputs. That number of outputs may be one.
    // The feedForward method is called on each layer.
    public List<Double> feedForward(List<Double> inputs) {

        List<Double> hiddenLayerOutputs = hiddenLayer.feedForward(inputs);
        return outputLayer.feedForward(hiddenLayerOutputs);
    }

    // Training means to feed the data forward - forward propagation. Compare the result with the target(s), and
    // use backpropagation to update the weights. See the blog post to review the math.
    public void train(List<Double> trainingInputs, List<Double> trainingOutputs) {

        // Update state of neural network and ignore the return value
        feedForward(trainingInputs);
        // Perform backpropagation
        List<Double> pdErrorsWRTOutputNeuronTotalNetInput =
                new ArrayList<Double>(Collections.nCopies(outputLayer.neurons.size(), 0.0));
        for (int o = 0; o < outputLayer.neurons.size(); o++) {
            pdErrorsWRTOutputNeuronTotalNetInput.set(o, outputLayer.neurons.get(o).calculatePDErrorWRTTotalNetInput(trainingOutputs.get(o)));
        }
        List<Double> pdErrorsWRTHiddenNeuronTotalNetInput =
                new ArrayList<Double>(Collections.nCopies(hiddenLayer.neurons.size(), 0.0));
        for (int h = 0; h < hiddenLayer.neurons.size(); h++) {
            double dErrorWRTHiddenNeuronOutput = 0;
            for (int o = 0; o < outputLayer.neurons.size(); o++) {
                dErrorWRTHiddenNeuronOutput +=
                        pdErrorsWRTOutputNeuronTotalNetInput.get(o) * outputLayer.neurons.get(o).weights.get(h);
                pdErrorsWRTHiddenNeuronTotalNetInput.set(h, dErrorWRTHiddenNeuronOutput *
                        hiddenLayer.neurons.get(h).calculatePDTotalNetInputWRTInput());
            }
        }
        for (int o = 0; o < outputLayer.neurons.size(); o++) {
            for (int wHo = 0; wHo < outputLayer.neurons.get(o).weights.size(); wHo++) {
                double pdErrorWRTWeight =
                        pdErrorsWRTOutputNeuronTotalNetInput.get(o) *
                                outputLayer.neurons.get(o).calculatePDTotalNetInputWRTWeight(wHo);
                outputLayer.neurons.get(o).weights.set(wHo, outputLayer.neurons.get(o).weights.get(wHo) - LEARNING_RATE * pdErrorWRTWeight);
            }
        }
        for (int h = 0; h < hiddenLayer.neurons.size(); h++) {
            for (int wIh = 0; wIh < hiddenLayer.neurons.get(h).weights.size(); wIh++) {
                double pdErrorWRTWeight =
                        pdErrorsWRTHiddenNeuronTotalNetInput.get(h) *
                                hiddenLayer.neurons.get(h).calculatePDTotalNetInputWRTWeight(wIh);
                hiddenLayer.neurons.get(h).weights.set(wIh, hiddenLayer.neurons.get(h).weights.get(wIh) - LEARNING_RATE * pdErrorWRTWeight);
            }
        }
    }

    // Perform a feed forward for each training row and total the error.
    public double calculateTotalError(ArrayList<Double[][]> trainingSets) {

        double totalError = 0.0;

        for (int t = 0; t < trainingSets.size(); t++) {
            List<Double> trainingInputs = Arrays.asList(trainingSets.get(t)[0]);
            List<Double> trainingOutputs = Arrays.asList(trainingSets.get(t)[1]);
            feedForward(trainingInputs);
            for (int o = 0; o < trainingOutputs.size(); o++) {
                totalError += outputLayer.neurons.get(o).calculate_error(trainingOutputs.get(o));
            }
        }
        return totalError;
    }
}