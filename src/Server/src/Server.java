package Server.src;

import javax.swing.text.Utilities;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.IllegalBlockingModeException;
import Utilitats.*;

public class Server {

    public static final String INIT_ERROR = "Server should be initialized with -p <port>";

    public static void main(String[] args) {

        if (args.length != 4) {
            throw new IllegalArgumentException("Wrong amount of arguments.\n" + INIT_ERROR);
        }

        if (!args[0].equals("-p")) {
            throw new IllegalArgumentException("Wrong argument keyword.\n" + INIT_ERROR);
        }

        int port;

        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("<port> should be an Integer. Use 0 for automatic allocation.");
        }

        ServerSocket ss = null;

        try {
            ss = new ServerSocket(port);
            System.out.println("Server up & listening on port " + port + "...\nPress Cntrl + C to stop.");
        } catch (IOException e) {
            throw new RuntimeException("I/O error when opening the Server Socket:\n" + e.getMessage());
        }


        /*
        TO DO:
        Create a new GameHandler for every client.
         */

        int m;
        if (!args[2].equals("-m")) {
            throw new IllegalArgumentException("Wrong argument keyword.\n" + INIT_ERROR);
        }

        m = Integer.parseInt(args[3]);

        if (m != 0 && m != 1) {
            throw new IllegalArgumentException("-m should be a 1 or 0. \n" + INIT_ERROR);
        }


        while (true) {
            Socket socket = null;
            if (m == 0) {

                try {
                    socket = ss.accept();
                    System.out.println("Client accepted\n");
                    Util util = new Util(socket);
                    GameHandler gameHandler = new GameHandler(util);
                    Thread t = new Thread(gameHandler);
                    t.start();
                } catch (IOException e) {
                    throw new RuntimeException("I/O error when accepting a client:\n" + e.getMessage());
                } catch (SecurityException e) {
                    throw new RuntimeException("Operation not accepted:\n" + e.getMessage());
                } catch (IllegalBlockingModeException e) {
                    throw new RuntimeException("There is no connection ready to be accepted:\n" + e.getMessage());
                } catch (Exception e) {
                    throw new RuntimeException("Could not init the game:\n" + e.getMessage());
                }
            } else {
                Socket socket_2 = null;

                try {
                    socket = ss.accept();
                    System.out.println("Client 1 accepted\n");
                    socket_2 = ss.accept();
                    System.out.println("Client 2 accepted\n");
                    Util util = new Util(socket);
                    MultiplayerGameHandler gameHandler = new MultiplayerGameHandler(socket, socket_2);
                    Thread t = new Thread(gameHandler);
                    t.start();
                } catch (IOException e) {
                    throw new RuntimeException("I/O error when accepting a client:\n" + e.getMessage());
                } catch (SecurityException e) {
                    throw new RuntimeException("Operation not accepted:\n" + e.getMessage());
                } catch (IllegalBlockingModeException e) {
                    throw new RuntimeException("There is no connection ready to be accepted:\n" + e.getMessage());
                } catch (Exception e) {
                    throw new RuntimeException("Could not init the game:\n" + e.getMessage());
                }
            }

        }
    }
}



