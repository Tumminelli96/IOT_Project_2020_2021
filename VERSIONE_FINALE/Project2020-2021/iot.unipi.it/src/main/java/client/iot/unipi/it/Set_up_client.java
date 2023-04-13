package client.iot.unipi.it;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
public class Set_up_client {
	public static void main(String[] args) 
	{
		String command="";
		InetAddress host;
		ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        System.out.println("please enter a comand: type help to get the command list, type exit to close the program");
		try {
			host = InetAddress.getLocalHost();
			try
			{
				Scanner scanner = new Scanner(System.in);
				while(command.compareTo("exit")!=0)
				{
					Socket socket = new Socket(host.getHostName(), 9796);
					
					oos = new ObjectOutputStream(socket.getOutputStream());
		            
		            System.out.print(">");
					command=scanner.nextLine();
					
					oos.writeObject(command);
					ois = new ObjectInputStream(socket.getInputStream());
					String message = (String) ois.readObject();
					System.out.print("Reply from server: " + message);
					ois.close();
		            oos.close();
		            socket.close();
				}
				scanner.close();
				
			} catch (UnknownHostException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				
				e.printStackTrace();
			}
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		}
		

		
	}
}
