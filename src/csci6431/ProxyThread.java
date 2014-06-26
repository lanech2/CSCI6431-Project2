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
	private Socket socket = null;
	
	public ProxyThread(Socket socket) {
		super("ProxyThread");
		this.socket = socket;
	}
	
	public void run() {
		try {
			System.out.println("Running...");
			DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String inLine;
			int cnt = 0;
			String target = "";
			String httpStr = "";
			URI uri = null;
			String host = "";
			String path = "";
			String header = "";			
			
			while (((inLine = inFromClient.readLine()) != "\t")) {
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
					System.out.println("inLine added to header: " + inLine);
					header += inLine;
					header += "\r\n";
				}
				cnt++;
			}
			
			header += "\r\n";

			System.out.println("host = " + host);
			System.out.println("path = " + path);
			System.out.println("httpStr = " + httpStr);
			System.out.println("header = " + header);
//			String host = "www.google.com";
//			String path = "/movies";
//			String httpStr = "HTTP/1.1";
//			String header = "User-Agent: curl/7.37.0\r\n" +
//							"Host: www.google.com\r\n" +
//							"Accept: */*\r\n" +
//							"Proxy-Connection: Keep-Alive\r\n\r\n";
			
			Socket serverSocket = new Socket(host, 80);
			
			String requestString = "GET " + path + " " + httpStr + "\r\n";
			requestString += header;
			PrintWriter outToServer = new PrintWriter(serverSocket.getOutputStream());
			outToServer.print(requestString);
			outToServer.flush();
			
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			String inStr;
			while ((inStr = inFromServer.readLine()) != null) {
				System.out.println(inStr);
			}
			
			serverSocket.close();
			socket.close();
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
