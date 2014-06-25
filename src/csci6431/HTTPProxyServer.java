package csci6431;

import java.io.IOException;
import java.net.ServerSocket;

public class HTTPProxyServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		
        if (args.length != 1) {
            System.err.println("Usage: java csci6431.HTTPProxyServer <port number>");
            System.exit(1);
        }
         
        int port = Integer.parseInt(args[0]);
        
        try {
        	serverSocket = new ServerSocket(port);
            System.out.println("Server started on port: " + port + ", awaiting connections ...");
        } catch (IOException e) {
        	System.err.println("Failed to establish socket on port: " + port);
        	System.exit(-1);
        }
        
        while (true) {
        	try {
				new ProxyThread(serverSocket.accept()).start();
			} catch (IOException e) {
				System.err.println("Socket failed to accept connection!");
				break;
			}
        }
        try {
			serverSocket.close();
		} catch (IOException e) {
            System.err.println("Socket failed to close!");
            System.exit(1);
		}
	}
}
