package networker;
// author: Luka Pacar

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
/*
 * TODO Client-Reconnection with UID
 */
/**
 * A Server is a Program that listens for incoming connections on a specific port
 * It can be used to receive data from multiple clients, while also being able to send data to them
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

    /** The map of Clients that are connected to the Server */
    private final HashMap<Long, Client> clientIdentifierMap;

    /** The counter to determine the amount of clients that have connected */
    private int clientCounter = 0;

    /** The flag to determine if the Server is listening for incoming connections */
    private boolean listeningForClients = false;

    /** The Clients that are currently connected to the Server */
    private final Set<Client> clients;

    /**
     * Creates a new Server that listens on a random-free port
     * @throws IOException If the Server could not be created
     */
    public Server() throws IOException {
        serverSocket = initializeSocket(-1);
        clients = new HashSet<>();
        clientIdentifierMap = new HashMap<>();
    }

    /**
     * Creates a new Server that listens on the specified port
     * @param port The port to listen on
     */
    public Server (int port) throws IOException {
        serverSocket = initializeSocket(port);
        clients = new HashSet<>();
        clientIdentifierMap = new HashMap<>();
    }

    /**
     * Listens for incoming connections on the current Thread
     * @return The Client that has connected
     * @throws IOException If the Server had an error during the connection
     */
    public Client accept() throws IOException {
        Socket socket = serverSocket.accept();
        return new Client(socket);
    }

    /**
     * Starts listening for connections on a separate Thread
     * @param clients The maximum amount of clients that can connect
     */
    public void listen(int clients) {
        listeningForClients = true;
        new Thread(() -> listenForConnection(clients)).start();
    }

    /** Stops listening for incoming connections */
    public void stopListening() {
        listeningForClients = false;
    }

    /** Listens for incoming connections <br>
     * Foreach incoming connection, a new Client is created and added to the list of clients
     * @param clients The maximum amount of clients that can connect
     */
    private void listenForConnection(int clients) {
        int amountOfConnectedClients = 0;
        while (listeningForClients && amountOfConnectedClients < clients) {
            try {
                Socket socket = serverSocket.accept();
                addClient(socket);
                amountOfConnectedClients++;
            } catch (IOException e) {
                if (listeningForClients) {
                    throw new RuntimeException("Error while listening for client connections", e);
                }
                break;
            }
        }
    }

    /**
     * Adds a new Client to the Server
     * @param socket The Socket of the Client
     */
    private synchronized Client addClient(Socket socket) throws IOException {
        Client client = new Client(socket);
        clients.add(client);
        client.startListening();
        long UID = clientCounter + System.currentTimeMillis();
        client.sendString("UID: " + UID);
        clientIdentifierMap.put(UID, client);
        clientCounter++;
        return client;
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
