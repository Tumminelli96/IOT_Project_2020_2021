package iot.unipi.it;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
public class MysqlAccess {
	private final static String URL = "jdbc:mysql://localhost:3306/sensors?";
    private final static String USER = "admin";
    private final static String PASSWORD = "admin";
    private final static String DRIVER = "com.mysql.cj.jdbc.Driver";
    private final static String INSERT = " insert into data (device_type, room, value, unit_of_measurement, timestamp_server)"
    	    + " values (?, ?, ?, ?, ?)";
    public static void insert_statment(String device_type,String room,double value,String unity_measurement,Timestamp timestamp) 
    {
    	try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
    		       Statement stmt = conn.createStatement();
    		       PreparedStatement pstmt = conn.prepareStatement(INSERT);) {
    		//Class.forName(DRIVER);
    		pstmt.setString (1, device_type);
    		  pstmt.setString (2, room);
    		  pstmt.setDouble  (3, value);
    		  pstmt.setString(4, unity_measurement);
    		  pstmt.setTimestamp(5, timestamp);
    		  pstmt.execute();
    		                
    		  } catch (SQLException ex) {
    		            Logger.getLogger(MysqlAccess.class.getName()).log(Level.SEVERE, null, ex);
    		} 
    }

}
