package eu.eurostat.trackingships.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.jdbc.OracleDriver;

public class ConnectionDB
{

   private static final Logger LOGGER = Logger.getLogger(ConnectionDB.class.getName());

   private static final String USERNAME = "db.username";
   private static final String PASSWORD = "db.password";
   private static final String URL = "db.url";
   
   private Connection conn = null;

   public ConnectionDB() throws SQLException, IOException
   {
      //read properties files
      InputStream propsFile = ConnectionDB.class.getClassLoader().getResourceAsStream("database.properties");
      Properties props = new Properties();
      props.load(propsFile);
      String username = props.getProperty(USERNAME);
      String password = props.getProperty(PASSWORD);
      String url = props.getProperty(URL);

      Driver oracleDriver = new OracleDriver();
      DriverManager.registerDriver(oracleDriver);
      conn = DriverManager.getConnection(url, username, password);
   }

   public Connection getConnection()
   {
      return conn;
   }

   public void closeConnection()
   {
      try
      {
         if (conn != null)
         {
            conn.close();
         }
      }
      catch (SQLException ex)
      {
         LOGGER.log(Level.SEVERE, ex.getMessage());
      }
   }

}
