import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Comms {

	JFrame frame;

	ServerSocket serverSocket;
	Socket clientSocket;
	Server server;

	public Comms() {}

	public Comms(Server server) {
		this.server = server;
	}

	//waits for a connection on a certain port
	public void makeServer(int serverPortNumber) throws IOException {
		try {
			serverSocket = new ServerSocket(serverPortNumber);
			while (true) {
				clientSocket = serverSocket.accept();
				// once a connection is established a new thread is created with
				// the socket that has just connected and the server itself
				SocketThread socketThread = new SocketThread(clientSocket,server);
				socketThread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Socket clientPersonalSocket;
	ObjectOutputStream clientOut;
	ObjectInputStream clientIn;

	// creates input and output streams for the client and the socket for that
	// particular client
	public void makeClient(String hostName, int clientPortNumber) {
		try {
			clientPersonalSocket = new Socket(hostName, clientPortNumber);
			clientOut = new ObjectOutputStream(clientPersonalSocket.getOutputStream());
			clientIn = new ObjectInputStream(clientPersonalSocket.getInputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//writes a message object to the clients output stream
	public void clientSendMessage(Message message) {
		try {
			if (message != null) {
				clientOut.writeObject(message);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	//reads in a message object from the clients input stream
	public Message clientReceiveMessage() {
		Message messageReceived = null;
		try {
			messageReceived = (Message) clientIn.readObject();
			// if the message is of a certain type it invokes a dialog window to
			// display the content of the message
			if (messageReceived instanceof ErrorMessage|| messageReceived instanceof NotificationMessage) {
				JOptionPane.showMessageDialog(frame, messageReceived.message);
			}
			//the output stream is closed
			clientOut.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return messageReceived;
	}
}

class SocketThread extends Thread {

	Socket socket;
	Server server;
	ObjectOutputStream serverOut;
	ObjectInputStream serverIn;

	SocketThread(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
	}

	public void run() {
		try {
			//creates input and output streams for the particular socket passed in
			serverOut = new ObjectOutputStream(socket.getOutputStream());
			serverIn = new ObjectInputStream(socket.getInputStream());
			//the server takes in a message
			Message message = serverReceiveMessage();
			// if there is content in the message the information it represents
			// is processed
			if (message != null) {
				serverSendMessage(server.processInput(message));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Message serverReceiveMessage() {
		//receives a message object through the servers input stream
		try {
			Message m = (Message) serverIn.readObject();
			return m;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	public void serverSendMessage(Message message) {
		//sends a message object to the servers output stream
		try {
			serverOut.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}