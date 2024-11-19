package networker;
// author: Luka Pacar

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A Server is a Program that listens for incoming connections on a specific port
 * It can be used to receive data from multiple clients, while also being able to send data to them
 * Foreach incoming connection, a new Thread is created to handle the client
 * <pre>
 * {@code
 * Server server = new Server(15001);
 * server.start(); // Starts listening for connections
 * }
 * </pre>
 */
public class Server {
    /**
     * The Socket that is responsible for handling connections
     */
    private final ServerSocket serverSocket;

    /**
     * Creates a new Server that listens on a random-free port
     * @throws IOException If the Server could not be created
     */
    public Server() throws IOException {
        serverSocket = initializeSocket(-1);
    }

    /**
     * Creates a new Server that listens on the specified port
     * @param port The port to listen on
     */
    public Server (int port) throws IOException {
        serverSocket = initializeSocket(port);
    }

    /**
     * Initializes the ServerSocket
     * If the port is less than 0, a random free port is used
     * @param port The port to listen on
     * @return The created ServerSocket
     * @throws IOException If the ServerSocket could not be created
     */
    private ServerSocket initializeSocket(int port) throws IOException {
        return port < 0 ? new ServerSocket() : new ServerSocket(port);
    }
}
