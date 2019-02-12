
/*...........................................................

Name: Prachi Vachhani
Albany ID: 001346588
Filename: server_java_udp.java

References:
Reference ...https://stackoverflow.com/questions/4884681/how-to-use-cd-command-using-java-runtime

..............................................................*/



import java.io.*;
import java.net.*;

public class server_java_udp {

	private static DatagramSocket socket = null;
	private static InetAddress ipaddress; 
	private static int port;
	private static int serverport;
	private static String command[] = new String[2];
	
	public static void cdCommand() throws ClassNotFoundException, IOException
	{
//		System.out.println("...........in cd command...............");
		byte[] msgToUser = new byte[1024];
		File dir = new File(command[1]);
		
        if(dir.isDirectory()==true) {
            System.setProperty("user.dir", dir.getAbsolutePath());
            String pwd = System.getProperty("user.dir");
            msgToUser = pwd.getBytes();
            //System.out.println("Current directory path is " + pwd);
        } else {
        	msgToUser = "No such directory".getBytes();
        }

		DatagramPacket sendpacket = new DatagramPacket(msgToUser, msgToUser.length, ipaddress, port);
		socket.send(sendpacket);
		
	}
	
	public static void lsCommand() throws ClassNotFoundException, IOException
	{

	//	System.out.println("..........in ls command.............");		

		byte[] msgToUser = new byte[1024];
		String showlistof = command[1];
		if(showlistof.equals(""))
		{
			showlistof = System.getProperty("user.dir");
		}
		
		File dir = new File(showlistof);
		File child[] = dir.listFiles();

		String sendlist= "";
		if(child==null)
		{
			msgToUser = "No such Directory".getBytes();
		}
		else
		{
	        for(int i=0; i<child.length; i++)
	        {
	        	sendlist = sendlist + child[i].getName() + "\n";
	            //System.out.println(child[i].getName());
	        }
	        msgToUser = sendlist.getBytes();
		}    
		DatagramPacket sendpacket = new DatagramPacket(msgToUser, msgToUser.length, ipaddress, port);
        socket.send(sendpacket);
	}
	
	public static void putCommand() throws ClassNotFoundException, IOException
	{
	//	System.out.println("...........................put command......................");
	
		byte[] filname_byte = new byte[1024];
		DatagramPacket recpacket = new DatagramPacket(filname_byte, filname_byte.length);
		socket.receive(recpacket);
		String filename = new String(recpacket.getData(), 0, recpacket.getLength());
		System.out.println(filename);
		if(filename.equals("No such file or directory"))
		{
			return;
		}
		String filedata[] = filename.split(" ");
		long filesize = Long.parseLong((filedata[1]));	//storing file size in server variable filesize
		long totalsentbytes = 0;
//		System.out.println("FILESIZE: " + filesize);
		String filecontent = "";
		byte[] filcontent_byte = new byte[1024];
		
		while(totalsentbytes+1 != filesize)
		{

			DatagramPacket recpacket2 = new DatagramPacket(filcontent_byte, filcontent_byte.length);
			socket.receive(recpacket2);
			filecontent = filecontent + new String(recpacket2.getData(), 0, recpacket2.getLength());
			totalsentbytes = totalsentbytes + recpacket2.getLength();
		}
		
	
		File file = new File("ClientFileUDP.txt");
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(filecontent);
		fileWriter.flush();
		fileWriter.close();
/*
		String ackno = "";
		if(totalsentbytes+1 == filesize)
		{
			ackno = "ACK";
		}
		else {
			ackno = "Did Not Receive valid data from Client. Terminating.";
		}
		byte ack[] = new byte[50];
		ack = ackno.getBytes();
		DatagramPacket sendpacket3 = new DatagramPacket(ack, ack.length, ipaddress, port);
		socket.send(sendpacket3);
		*/

	}
	

	public static void getCommand() throws IOException, ClassNotFoundException
	{
//		System.out.println("...............get command................");

		byte[] msgFromUser = new byte[1024];
		byte[] msgToUser = new byte[1024];

		DatagramPacket recpacket = new DatagramPacket(msgFromUser, msgFromUser.length);
		socket.receive(recpacket);		//receive filename from user, open it on server, and send the file name and size to user
		String filename = new String(recpacket.getData(), 0, recpacket.getLength());
		command[1] = filename;
		File file = new File(command[1]);
		
		if(file.exists())
		{
			msgToUser = (file.getName() + " " + file.length()).getBytes(); 
		}
		else
		{
			msgToUser = "No such file or directory".getBytes();
		}
		
		DatagramPacket sendpacket = new DatagramPacket(msgToUser, msgToUser.length, ipaddress, port);
		socket.send(sendpacket);			//Sending filename and filesize size to server..
				
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("No such file or directory");
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
		long sizeOfMsgToUser = msgToUser.length;
		
		while(bytessent != bytestosend || offset==bytessent)
		{
/*			System.out.println(
					"\n\nFILE LENGTH :" + file.length() + 
					"\nBYTES TO SEND : " + bytestosend + 
					"\nOFFSET: " + offset +
					"\nsizeOfMsgToServer : "+ sizeOfMsgToServer);
*/
			System.arraycopy(b, (int) offset, msgToUser, 0, (int) sizeOfMsgToUser);
			sendpacket = new DatagramPacket(msgToUser, (int) sizeOfMsgToUser , ipaddress, port);
//			System.out.println("\n\tUSER DATA SENT: " + new String(msgToServer, 0, (int) sizeOfMsgToServer ));
			socket.send(sendpacket);

			bytessent = bytessent + sizeOfMsgToUser;
			offset = bytessent + 1;
			if(sizeOfMsgToUser == 0)
			{
				break;
			}
			if((bytestosend - offset) < sizeOfMsgToUser)
			{
				sizeOfMsgToUser = bytestosend - offset; 
			}
		}
/*
		byte[] ack = new byte[50];
		DatagramPacket recpacket3 = new DatagramPacket(ack, ack.length);
		String ackno = new String(recpacket3.getData(), 0, recpacket3.getLength());
		System.out.println(ackno);		
*/
	}


	public static void main(String[] args) throws SocketException, IOException, ClassNotFoundException 
	{
		serverport = Integer.parseInt(args[0]);
		try {
			socket = new DatagramSocket(serverport);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Result transmission failed. Terminating.");
			e.printStackTrace();
		}
		System.out.println("Server ready to receive Input");
		
   		byte[] buf = new byte[256];
   		DatagramPacket recpacket = new DatagramPacket(buf, buf.length);
   		socket.receive(recpacket);
   		
   		ipaddress = recpacket.getAddress();
        port = recpacket.getPort();
   		
        byte[] confirmation = "data path ready".getBytes();
        DatagramPacket sendpacket = new DatagramPacket(confirmation, confirmation.length, ipaddress, port);
        socket.send(sendpacket);
        System.out.println("Server ready to receive Input");
        
        while(true) 
        	{
          
        	byte[] msgFromClient = new byte[1024];
        	recpacket = new DatagramPacket(msgFromClient, msgFromClient.length);
        	socket.receive(recpacket);

			String usercommand = new String(recpacket.getData(), 0, recpacket.getLength());
		    System.out.println("Client command: "+ usercommand);
			
		    command = usercommand.split(" ");
		    
			if(command[0].equalsIgnoreCase("exit"))
			{
				System.out.println("Client disconnected");
				socket.close();
				break;
			}
		
			if(command[0].equalsIgnoreCase("get"))
			{
				getCommand();
			}
					
			if(command[0].equalsIgnoreCase("put"))
			{
				putCommand();
			}
			
			if(command[0].equalsIgnoreCase("cd"))
			{
				cdCommand();
			}

			if(command[0].equalsIgnoreCase("ls"))
			{
				lsCommand();
			}

		}
        System.out.println("------------------------------");          
      	socket.close();	
    }         
}
