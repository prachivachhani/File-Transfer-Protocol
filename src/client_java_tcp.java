
/*...........................................................

Name: Prachi Vachhani
Albany ID: 001346588
Filename: client_java_tcp.java

References:
Reference ...https://stackoverflow.com/questions/4884681/how-to-use-cd-command-using-java-runtime
		  ...http://www.java2novice.com/java-collections-and-util/regex/ip-validation/
          ...http://silveiraneto.net/2008/10/07/example-of-unix-commands-implemented-in-java/
..............................................................*/


import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class client_java_tcp 
{
	static Socket clientSocket;
	static ObjectOutputStream msgToServer; 
	static ObjectInputStream msgFromServer;
	static InetAddress ipaddress;
	static int port;
	static String command[] = new String[2];
	
	public static boolean isValidIP(String ip)
    {		  
    	Pattern pattern = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
    	Matcher matcher = pattern.matcher(ip);
    	return matcher.find();
    }
	
	public static void cdCommand() throws IOException, ClassNotFoundException
	{
//		System.out.println("...........in cd command...............");

		msgToServer.writeObject(command[1]);
		String newpath = msgFromServer.readObject().toString();
		if(newpath.equals("No such directory"))
		{
			System.out.println(newpath + " " + command[1]);
		}
		else {
			System.out.println("Current directory path is "+newpath);
		}
		
	}
	
	public static void lsCommand() throws IOException, ClassNotFoundException
	{
//		System.out.println("..........in ls command.............");		

		//String showlistof = System.getProperty("user.dir");
		
		msgToServer.writeObject(command[1]);
		String showlist = msgFromServer.readObject().toString();
		System.out.println(showlist);
		
	}
	
	public static void putCommand() throws IOException
	{
//		System.out.println("..............put command.................");

		Socket clientSocket2 = new Socket (ipaddress, port+1);
		ObjectOutputStream msgToServer2 = new ObjectOutputStream(clientSocket2.getOutputStream()); 
		ObjectInputStream msgFromServer2 = new ObjectInputStream(clientSocket2.getInputStream());
		Scanner sc = new Scanner(System.in);

/*		System.out.println("Enter file name: ");
		String fileName = sc.nextLine(); 
		
*/
		File file = new File(command[1]);
		msgToServer.writeObject(command[1]);		//Sending file name to the server to open...
		
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("No such file or directory");
			msgFromServer2.close();
			msgToServer2.close();
			clientSocket2.close();
			return;
		}
		DataInputStream din = new DataInputStream(fin);

		byte[] b = new byte[din.available ()];
		din.readFully (b);
		din.close ();
		String result = new String (b, 0, b.length);
		//System.out.println(result);
		msgToServer2.writeObject(result);
		//System.out.println("File sent on server side");
		
		msgFromServer2.close();
		msgToServer2.close();
		clientSocket2.close();
	}
	
	public static void getCommand() throws IOException, ClassNotFoundException
	{
//		System.out.println("...............get command................");
		
//		System.out.println("Enter the file name: ");
//		String fileName = sc.nextLine(); 
		msgToServer.writeObject(command[1]);
		String filecontent ="";
		
		Socket clientSocket2 = new Socket (ipaddress, port+1);
		ObjectOutputStream msgToServer2 = new ObjectOutputStream(clientSocket2.getOutputStream()); 
		ObjectInputStream msgFromServer2 = new ObjectInputStream(clientSocket2.getInputStream());
		
		filecontent = msgFromServer2.readObject().toString();
		if(filecontent.equals("No such file or directory"))
		{
			System.out.println(filecontent);
		}
		else
		{
			File file = new File("ServerFileTCP.txt");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(filecontent);
			fileWriter.flush();
			fileWriter.close();
		}
		
		msgFromServer2.close();
		msgToServer2.close();
		clientSocket2.close();
		
	}

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException 
	{
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter server name or IP address: ");
		String stringAddress = sc.nextLine();
		ipaddress = InetAddress.getByName(stringAddress);	

		if(stringAddress.equals("localhost") || stringAddress.equals("csi516-fa18.arcc.albany.edu") || client_java_udp.isValidIP(stringAddress))
		{

			System.out.println("Enter port: ");
			port = Integer.parseInt(sc.nextLine());
			if(port<0 ||  port>65535)
			{
				System.out.println("Invalid port number. Terminating.");
			}
			else
			{
				clientSocket = new Socket (ipaddress, port);
				msgToServer = new ObjectOutputStream(clientSocket.getOutputStream()); 
				msgFromServer = new ObjectInputStream(clientSocket.getInputStream());
			
				String confirmation = msgFromServer.readObject().toString(); 
				System.out.println(confirmation);
				
				String usercommand;
			
				while(true)
				{
					System.out.println("Enter command: ");
					usercommand = sc.nextLine();
					command = usercommand.split(" ");
					
					msgToServer.writeObject(command[0]);

					
					if(command[0].equalsIgnoreCase("exit"))
					{
						clientSocket.close();
						break;
					}
				
					if(command[0].equalsIgnoreCase("cd"))
					{
						cdCommand();
					}
					
					if(command[0].equalsIgnoreCase("ls"))
					{
						lsCommand();
					}
					
					if(command[0].equalsIgnoreCase("get"))
					{
						getCommand();
					}
					
					if(command[0].equalsIgnoreCase("put"))
					{
						putCommand();
					}

				//... code reference http://www.shouttoworld.com/file-transfer-protocol-ftp-implementation-java/		

				
				}//End while
			}//End of port validating if
		}//End if
		
		
		else
		{
			System.out.println("Could not connect to server. Terminating."); 
		}
	}
}