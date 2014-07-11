package csci6431;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;

public class ProxyThread extends Thread {
	private Socket clientSocket = null;
	
	public ProxyThread(Socket socket) {
		super("ProxyThread");
		this.clientSocket = socket;
	}
	
	public void run() {
		try {
			System.out.println("--------------------------------------------------------------------------------------------");
			System.out.println("Running new ProxyThread ...");
			DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			String inLine;
			int cnt = 0;
			String target = "";
			String httpStr = "";
			URI uri = null;
			String host = "";
			String path = "";
			String header = "";			
			
			while (inFromClient.ready()) {
				inLine = inFromClient.readLine();
				//handle the first line, the rest goes to the header variable
				if (cnt == 0) {
					String[] tokens = inLine.split(" ");
					target = tokens[1];
					httpStr = tokens[2];
					uri = new URI(target);
					host = uri.getHost();
					path = uri.getRawPath();
					System.out.println("Request for host: " + host + " and path: " + path);
				} else {
					header += inLine;
					header += "\r\n";
				}
				cnt++;
			}

			// Add the final crlf indicating End of message
			header += "\r\n";
			
			Socket serverSocket = new Socket(host, 80);
			System.out.println("serverSocket established: " + serverSocket.getLocalPort() + ":" + serverSocket.getPort());
			
			String requestString = "GET " + path + " " + httpStr + "\r\n";
			requestString += header;
			PrintWriter outToServer = new PrintWriter(serverSocket.getOutputStream());
			outToServer.print(requestString);
			outToServer.flush();
			
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			String inStr;
//			System.out.println("--------------------------------------------------------------------------------------------");
			int i = 0, j = 0;
			//read the headers first
			System.out.println("\tProcesing Header");
			while ((inStr = inFromServer.readLine()) != null) {
				if (inStr.isEmpty()) break;
				i++;
				outToClient.writeChars(inStr);
				outToClient.flush();
			}
			//now read the body
			System.out.println("\tProcessing Body");
			while ((inStr = inFromServer.readLine()) != null) {
					if (inStr.isEmpty()) break;
					j++;
					outToClient.writeChars(inStr);
					outToClient.flush();
				}
			outToClient.writeChars("\n");
			System.out.println("Closing connections ...");
			System.out.println("--------------------------------------------------------------------------------------------");
			inFromServer.close();
			inFromClient.close();
			serverSocket.close();
			clientSocket.close();
//			System.exit(0);
		} catch (SocketException e) {
			System.out.println("Caught SocketException: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("Caught IOException: " + e.getMessage());
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
