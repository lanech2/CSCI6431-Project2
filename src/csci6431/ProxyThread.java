package csci6431;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class ProxyThread extends Thread {
	private static final int BUFFER_SIZE = 65536;
	private Socket socket = null;
	
	public ProxyThread(Socket socket) {
		super("ProxyThread");
		this.socket = socket;
	}
	
	public void run() {
		try {
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
			
			while ((inLine = inFromClient.readLine()) != null) {
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
			
			Socket serverSocket = new Socket(host, 80);
			
			String requestString = "GET " + path + " " + httpStr + "\r\n";
			requestString += header;
			PrintWriter request = new PrintWriter(serverSocket.getOutputStream());
			request.print(requestString);
			request.flush();
			
			InputStream inStr = serverSocket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inStr));
			String inFromServer;
			while ((inFromServer = reader.readLine()) != null) {
				System.out.println(inFromServer);
			}
			
			serverSocket.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
