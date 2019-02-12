
/*...........................................................

Name: Prachi Vachhani
Albany ID: 001346588
Filename: server_java_tcp.java

References:
Reference ...https://stackoverflow.com/questions/4884681/how-to-use-cd-command-using-java-runtime

..............................................................*/


import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class server_java_tcp {
	
	static int serverport;
	static ServerSocket serverSocket;
	static Socket socket;
	static ObjectOutputStream msgToUser;
	static ObjectInputStream msgFromUser;
	
	public static void cdCommand() throws ClassNotFoundException, IOException
	{
		String changedir = msgFromUser.readObject().toString();

		File dir = new File(changedir);
		
        if(dir.isDirectory()==true) {
            System.setProperty("user.dir", dir.getAbsolutePath());
            String pwd = System.getProperty("user.dir");
            msgToUser.writeObject(pwd);
        } else {
        	msgToUser.writeObject("No such directory");
        }

	}
	
	public static void lsCommand() throws ClassNotFoundException, IOException
	{

		String showlistof = msgFromUser.readObject().toString();
		System.out.println(showlistof);
		if(showlistof.equals(""))
		{
			showlistof = System.getProperty("user.dir");
		}
		
		File dir = new File(showlistof);
		File child[] = dir.listFiles();
		
		String sendlist= "";
		if(child==null)
		{
			msgToUser.writeObject("No such Directory");
		}
		else
		{
	        for(int i=0; i<child.length; i++)
	        {
	        	sendlist = sendlist + child[i].getName() + "\n";
	        }
	        msgToUser.writeObject(sendlist);
		}    
        
	}
	
	public static void putCommand() throws ClassNotFoundException, IOException
	{
		String filecontent ="";

		ServerSocket serverSocket2 = new ServerSocket(serverport+1);
		Socket socket2 = serverSocket2.accept();
		ObjectOutputStream msgToUser2 = new ObjectOutputStream(socket2.getOutputStream());
		ObjectInputStream msgFromUser2 = new ObjectInputStream(socket2.getInputStream());
		
		String filename = msgFromUser.readObject().toString();
		
		File file = new File("clientFileTCP.txt");
		
		filecontent = msgFromUser2.readObject().toString();
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(filecontent);
		fileWriter.flush();
		fileWriter.close();
				
		msgFromUser2.close();
		msgToUser2.close();
		socket2.close();
		serverSocket2.close();
	}	
	
	public static void getCommand() throws IOException, ClassNotFoundException
	{
		ServerSocket serverSocket2 = new ServerSocket(serverport+1);
		Socket socket2 = serverSocket2.accept();
		ObjectOutputStream msgToUser2 = new ObjectOutputStream(socket2.getOutputStream());
		ObjectInputStream msgFromUser2 = new ObjectInputStream(socket2.getInputStream());
		
		String fileName = msgFromUser.readObject().toString();		
		
		File file = new File(fileName);
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			msgToUser2.writeObject("No such file or directory");
			msgFromUser2.close();
			msgToUser2.close();
			socket2.close();
			serverSocket2.close();
			return;
		}
		DataInputStream din = new DataInputStream(fin);

		byte[] b = new byte[din.available ()];
		din.readFully (b);
		din.close ();
		String result = new String (b, 0, b.length);
		msgToUser2.writeObject(result);
		
		msgFromUser2.close();
		msgToUser2.close();
		socket2.close();
		serverSocket2.close();	
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException 
	{
		serverport = Integer.parseInt(args[0]);
		try {
			serverSocket = new ServerSocket(serverport);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Result transmission failed. Terminating.");
			e.printStackTrace();
		}
		System.out.println("Server ready to receive Input");

		socket = serverSocket.accept();
		msgToUser = new ObjectOutputStream(socket.getOutputStream());
		msgFromUser = new ObjectInputStream(socket.getInputStream());
		msgToUser.writeObject("data path ready");
		String getmessage;
		
		
		while(true)
		{
			getmessage = msgFromUser.readObject().toString();
			System.out.println("Message from client: " + getmessage);
		
			if(getmessage.equalsIgnoreCase("exit"))
			{
				System.out.println("Client disconnected");
				serverSocket.close();
				break;
			}
		
			if(getmessage.equalsIgnoreCase("cd"))
			{
				cdCommand();
			}
			
			if(getmessage.equalsIgnoreCase("ls"))
			{
				lsCommand();
			}
			
			if(getmessage.equalsIgnoreCase("get"))
			{
				getCommand();
			}
			
			if(getmessage.equalsIgnoreCase("put"))
			{
				putCommand();
			}

		}
	}

}