
/*...........................................................

Name: Prachi Vachhani
Albany ID: 001346588
File name: client_java_udp.java

References:
Reference ...https://stackoverflow.com/questions/4884681/how-to-use-cd-command-using-java-runtime
          ...http://www.java2novice.com/java-collections-and-util/regex/ip-validation/
          ...http://silveiraneto.net/2008/10/07/example-of-unix-commands-implemented-in-java/
..............................................................*/

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class client_java_udp 
{
	static String command[] = new String[2];
	static InetAddress ipaddress;	
	static int port;
	private static DatagramSocket socket = null;
	private static Scanner sc;
	
	
	public static boolean isValidIP(String ip)
    {		  
    	Pattern pattern = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
    	Matcher matcher = pattern.matcher(ip);
    	return matcher.find();
    }
	
	public static void cdCommand() throws IOException, ClassNotFoundException
	{
		byte[] msgFromServer = new byte[1024];

		DatagramPacket recpacket = new DatagramPacket(msgFromServer, msgFromServer.length);
		socket.receive(recpacket);
		String server_msg = new String(recpacket.getData(), 0, recpacket.getLength());
		if(server_msg.equals("No such directory"))
		{
			System.out.println(server_msg + " " + command[1]);
			return;
		}
		System.out.println("Current directory path is " + server_msg);
		
	}
	
	public static void lsCommand() throws IOException, ClassNotFoundException
	{
		byte[] msgFromServer = new byte[1024];
		DatagramPacket recpacket = new DatagramPacket(msgFromServer, msgFromServer.length);
		socket.receive(recpacket);
		String serverOutput = new String(recpacket.getData(), 0, recpacket.getLength());
		System.out.println(serverOutput);
		
	}
	
	public static void putCommand() throws IOException, InterruptedException
	{
		byte[] msgToServer = new byte[1024];
		String filedata = ""; 
		File file = new File(command[1]);
		if(file.isFile() && file.exists())
		{
			filedata = file.getName() + " " +  file.length();
		}
		else {
			filedata = "No such file or directory";
			System.out.println(filedata);
		}
		
		msgToServer = filedata.getBytes();
		DatagramPacket sendpacket = new DatagramPacket(msgToServer, msgToServer.length, ipaddress, port);
		socket.send(sendpacket);		//Sending filename and filesize size to server..

		
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return;
		}

		DataInputStream din = new DataInputStream(fin);
		byte[] b = new byte[din.available ()];
		din.readFully (b);
		din.close ();
		String result = new String (b, 0, b.length);
		long bytestosend = file.length();
		long bytessent = 0;
		long offset = 0;
		long sizeOfMsgToServer = msgToServer.length;
		
		while(bytessent != bytestosend || offset==bytessent)
		{
			System.arraycopy(b, (int) offset, msgToServer, 0, (int) sizeOfMsgToServer);
			sendpacket = new DatagramPacket(msgToServer, (int) sizeOfMsgToServer , ipaddress, port);
			socket.send(sendpacket);

			bytessent = bytessent + sizeOfMsgToServer;
			offset = bytessent + 1;
			if(sizeOfMsgToServer == 0)
			{
				break;
			}
			if((bytestosend - offset) < sizeOfMsgToServer)
			{
				sizeOfMsgToServer = bytestosend - offset; 
			}
		}
	}
	
	
	public static void getCommand() throws IOException, ClassNotFoundException
	{
		
		byte[] msgToServer = new byte[1024];
		msgToServer = command[1].getBytes();
		DatagramPacket sendpacket = new DatagramPacket(msgToServer, msgToServer.length, ipaddress, port);
		socket.send(sendpacket);		//send the filename to the server, and get the filename and filesize back from the server
				
		byte[] fildetails = new byte[1024];
		DatagramPacket recpacket = new DatagramPacket(fildetails, fildetails.length);
		socket.receive(recpacket);
		String filenameAndSize = new String(recpacket.getData(), 0, recpacket.getLength());
		if(filenameAndSize.equals("No such file or directory"))
		{
			System.out.println(filenameAndSize);
			return;
		}
		else
		{	
			System.out.println(filenameAndSize);
			String filedata[] = filenameAndSize.split(" ");
			long filesize = Long.parseLong((filedata[1]));	//storing file size in user variable filesize
			long totalsentbytes = 0;
			String filecontent = "";
			byte[] filcontent_byte = new byte[1024];
			
			//Run while loop still all the bytes from the server is not received... and then open a new file
			//at client side to store the data
			
			while(totalsentbytes+1 != filesize)
			{
				DatagramPacket recpacket2 = new DatagramPacket(filcontent_byte, filcontent_byte.length);
				socket.receive(recpacket2);
				filecontent = filecontent + new String(recpacket2.getData(), 0, recpacket2.getLength());
				totalsentbytes = totalsentbytes + recpacket2.getLength();
			}	
			
			File file = new File("ServerFileUDP.txt");
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(filecontent);
			fileWriter.flush();
			fileWriter.close();
	
		}
	}


	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException 
	{

		sc = new Scanner(System.in);
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
			
				socket = new DatagramSocket();
				socket.connect(ipaddress, port);
				byte[] sendConfirmation = "setting up connection".getBytes();
				byte[] recConfirmation = new byte[1024];
				
				DatagramPacket sendpacket = new DatagramPacket(sendConfirmation, sendConfirmation.length, ipaddress, port);
				socket.send(sendpacket);		// Sending user packet to server to let server know the ipaddress and port of the user
				
				DatagramPacket recpacket = new DatagramPacket(recConfirmation, recConfirmation.length);
				socket.receive(recpacket);
				String connectionConfirmation = new String(recpacket.getData(), 0, recpacket.getLength());				
				System.out.println(connectionConfirmation);		// Receive the confirmation of the first packet sent
				
				String usercommand;
			
				// Continue executing the loop unless the user give command: Exit..
				while(true)
				{
					
					System.out.println("Enter command: ");
					usercommand = sc.nextLine();
					byte[] sendbuf = usercommand.getBytes();
					sendpacket = new DatagramPacket(sendbuf, sendbuf.length, ipaddress, port);
					socket.send(sendpacket);
					
					command = usercommand.split(" ");

					if(command[0].equalsIgnoreCase("exit"))
					{
						socket.close();	//close socket
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
			//...................................................................CODE NEEDED TO TERMINATE
		}
	}

}

