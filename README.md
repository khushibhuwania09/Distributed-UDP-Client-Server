# Distributed UDP Client-Server Applications

## Overview
This repository contains a collection of UDP-based client-server applications implemented in Java as part of Project 2 for the CMU course 95702. The project demonstrates the principles of distributed systems, focusing on client-server communication, proxy design, and neural network applications.

## Features
The project is divided into multiple tasks, each highlighting a different aspect of UDP communication and distributed systems:

0. **Echo Server and Client**:
   - Implements a simple UDP echo server and client.
   - The client sends messages to the server, which echoes them back.
   - Supports halting the communication with a special "halt!" command.

1. **Eavesdropper/Malicious Proxy**:
   - Intercepts and modifies UDP messages between the client and the server.
   - Demonstrates the vulnerability of UDP communication to man-in-the-middle attacks.

2. **Adding Server and Client**:
   - Implements a server that maintains a running total of integers sent by the client.
   - Clients can send integers to the server and retrieve the updated sum.

3. **Remote Variable Server**:
   - Introduces multiple client support with unique IDs.
   - Clients can perform operations such as adding, subtracting, or retrieving their specific variable values on the server.

4. **Distributed Neural Network**:
   - Implements a simple feedforward neural network.
   - The server handles training, testing, and configuration of the network.
   - Clients interact with the server to modify or test the neural network using a JSON-based protocol.

## Prerequisites
- **Java Development Kit (JDK)**: Ensure JDK 8 or later is installed.
- **Gson Library**: Required for JSON serialization and deserialization in Task 4.
- **Network Setup**: Ensure localhost communication is enabled on the system.

## Usage
### Compilation
Compile all Java files in the project using the following command:
```bash
javac -cp .:gson-2.8.8.jar *.java
