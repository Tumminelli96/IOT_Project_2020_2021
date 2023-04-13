package iot.unipi.it;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
public class CommunicationWithClient implements Runnable {
    //static ServerSocket variable
    
    //socket server port on which it will listen
    private static int port = 9796;
    private ConcurrentHashMap<String,ParameterRoom> room_parameter;
    public CommunicationWithClient(ConcurrentHashMap<String,ParameterRoom> room_parameter)
    {
    	this.room_parameter=room_parameter;
    }
	@Override
	public void run() {
		 //create the socket server object
        try(ServerSocket server = new ServerSocket(port);)
        {
        	System.out.println("Waiting for the client request");
        	
            while(true){
                
                //creating socket and waiting for client connection
            	Socket socket = server.accept();
                //read from socket to ObjectInputStream object
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                //convert ObjectInputStream object to String
                String command= (String) ois.readObject();
                String reply=compute_command(command);
                //create ObjectOutputStream object
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                //write object to Socket
                oos.writeObject(reply);
                //close resources
                
                ois.close();
                oos.close();
                socket.close();
                //terminate the server if client sends exit request
                if(command.compareTo("exit")==0)
                {
                	
                	//socket.close();
                	//server.accept();
                	
                }
            }
        }
        catch (ClassNotFoundException|IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
	}
    private String compute_command(String command)
    {
    	if(command.split(" ").length==0)
    	{
    		return "No command found";
    	}
    	switch(command.split(" ")[0])
    	{
    	case "help":
    		return "List of command:\n show_parameter [room] :show the actual parameter for a specific room\n"
    				+ "change_parameter [room] [parameter] [new_value] :change the parameter for a specific room with a correct value\n";
    		
    	case "show_parameter":
    		if(command.split(" ").length<2)
    		{
    			return "Wrong command format:\n";
    			
    		}
    		else if(room_parameter.containsKey(command.split(" ")[1]))
    		{
    			return room_parameter.get(command.split(" ")[1]).toString();
    		}
    		else
    		{
    			return "room doesn't exist";
    		}
    	case "change_parameter":
    		if(command.split(" ").length<4)
    		{
    			return "Wrong command format:\n";
    			
    		}
    		else if(room_parameter.containsKey(command.split(" ")[1]))
    		{
    			if(room_parameter.get(command.split(" ")[1]).setParameter(command.split(" ")[2], command.split(" ")[3]))
    			{
    				return "parameter is set correctly\n";
    			}
    			return "error, unknow parameter\n";
    			
    		}
    	case "exit":
    		return "shut down client";
    		default:
    			return "error, unknow command\n";
    	}

    		
    	
    	
    }
		
	
}
