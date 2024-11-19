package networker;
// author: Luka Pacar

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.net.Socket;
import java.util.*;

/**
 * A Client is a Program that connects to a Server on a specific port
 * It can be used to send data to the Server, while also being able to receive data from it
 * <pre>
 * {@code
 * Client client = new Client("Server-Address", PORT);
 * client.send("MESSAGE");
 * client.receive();
 * }
 * </pre>
 */
public class Client {
    /** The Socket that is responsible for handling the connection */
    private Socket socket;

    // Input

    /** The flag to determine if the Client is listening for incoming messages */
    private boolean listening;

    /** The InputStream to receive data from the Server */
    private InputStream inputStream;

    /** The Runnable to receive binary data from the Server */
    private final Runnable binaryInput;

    /** The Reader to read text-based data from the Server */
    private BufferedReader textReader;

    /** The Runnable to receive text data from the Server */
    private final Runnable textInput;

    /** The Queue of Text Messages that have been received from the Server */
    private final Queue<String> textMessages;

    /** The Queue of Text Messages that have been received from the Server */
    private final Queue<byte[]> binaryMessages;

    // Output
    /** The OutputStream to send data to the Server */
    private OutputStream outputStream;

    /** The Writer to send text-based data to the Server */
    private BufferedWriter textWriter;

    /** The flag to determine if the Client is sending binary data */
    private boolean isBinary;

    /**
     * Creates a new Client that connects to a Server on a specific address and port
     * @param address The address of the Server
     * @param port The port of the Server
     * @throws IOException If the Client could not connect to the Server
     */
    Client(String address, int port) throws IOException {
        this(initializeSocket(address, port));
    }

    /**
     * Creates a new Client that connects to a Server on a specific address and port
     * @param socket The socket-connecting to the Server
     * @throws IOException If the Client could not connect to the Server
     */
    Client (Socket socket) throws IOException {
        textMessages = new LinkedList<>();
        binaryMessages = new LinkedList<>();

        binaryInput = () -> {
            try {
                byte[] buffer = new byte[1024]; // Fixed-size buffer
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byte[] data = Arrays.copyOf(buffer, bytesRead); // Copy only the read bytes
                    binaryMessages.add(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        textInput = () -> {
            try {
                String message;
                while ((message = textReader.readLine()) != null) {
                    textMessages.add(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        connect(socket);
    }

    // Messages

    /** Returns all Text Messages that have been received */
    public Collection<String> getAllTextMessages() {
        return new LinkedList<>(textMessages);
    }

    /** Returns all Binary Messages that have been received */
    public Collection<byte[]> getAllBinaryMessages() {
        return new LinkedList<>(binaryMessages);
    }

    /**
     * Receives the next message from the Server
     * @return The next message from the Server
     */
    public String receiveText() {
        return textMessages.poll();
    }

    /**
     * Receives the next message from the Server
     * @return The next message from the Server
     */
    public byte[] receiveBytes() {
        return binaryMessages.poll();
    }

    // Listening
    /** Starts listening for incoming messages. */
    public void startListening() {
        if (!listening) {
            listening = true;
            new Thread(() -> {
                if (isBinary) {
                    while (listening) binaryInput.run();
                } else {
                    while (listening) textInput.run();
                }
            }).start();
        }
    }

    /** Stops receiving messages */
    public void stopListening() {
        listening = false;
    }

    /** Restarts the Listening Function */
    public void restartListening() {
        stopListening();
        startListening();
    }

    // Sending Data
    /**
     * Sends a Binary Message to the Server
     * @param data The data to send
     * @throws IOException If the data could not be sent
     */
    public void sendBinary(byte[] data) throws IOException {
        outputStream.write(data);
    }

    /**
     * Sends a String to the Server
     * @param message The message to send
     */
    public void sendString(String message) throws IOException {
        textWriter.write(message);
    }

    // Status
    /**
     * Checks if the Client is still connected to the Server.
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Initializes the Client with the Socket
     * @param socket The socket to connect to
     * @throws IOException If the Client could not connect to the Server
     */
    public void connect(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = socket.getInputStream();
        textReader = new BufferedReader(new java.io.InputStreamReader(inputStream));

        outputStream = socket.getOutputStream();
        textWriter = new BufferedWriter(new java.io.OutputStreamWriter(outputStream));

        startListening();
    }

    /**
     * Gracefully disconnects the Client from the Server.
     * Closes all streams and the socket.
     * @throws IOException If an error occurs during disconnection
     */
    public void disconnect() throws IOException {
        listening = false;
        if (textReader != null) textReader.close();
        if (textWriter != null) textWriter.close();
        if (inputStream != null) inputStream.close();
        if (outputStream != null) outputStream.close();
        if (socket != null && !socket.isClosed()) socket.close();
    }

    /**
     * Reconnects the Client to the same Server.
     * @throws IOException If the reconnection fails
     */
    public void reconnect() throws IOException {
        disconnect();
        connect(initializeSocket(socket.getInetAddress().getHostName(), socket.getPort()));
    }


    // Receiver Settings
    /**
     * Sets the Client to expect Binary Data <br>
     * Restart of the Listening Function is required to apply the changes
     * <pre>
     * restartListening();
     * </pre>
     */
    public void setReceiverToBinary() {
        this.isBinary = true;
    }

    /**
     * Sets the Client to expect Text Data <br>
     * Restart of the Listening Function is required to apply the changes
     * <pre>
     * restartListening();
     * </pre>
     */
    public void setReceiverToText() {
        this.isBinary = false;
    }

    // Basic Methods
    /**
     * Initializes the Socket
     * @param address The address to connect to
     * @param port The port to use
     * @return The created Socket
     * @throws IOException If the Socket could not be created
     */
    private static Socket initializeSocket(String address, int port) throws IOException {
        return new Socket(address, port);
    }

    @Override
    public String toString() {
        return "Client-" + (socket.isConnected() ? "A" : "C") + ":[" + socket.getInetAddress().toString() + ":" + socket.getPort() + "]";
    }

    @Override
    public int hashCode() {
        return socket.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != this.getClass()) return false;
        Client client = (Client) obj;
        return socket.equals(client.socket);
    }
}
