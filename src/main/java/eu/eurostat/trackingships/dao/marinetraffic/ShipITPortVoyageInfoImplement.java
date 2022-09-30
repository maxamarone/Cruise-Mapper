package eu.eurostat.trackingships.dao.marinetraffic;

import eu.eurostat.trackingships.bean.ShipPortVoyageInfo;
import eu.eurostat.trackingships.dao.ConnectionDB;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShipITPortVoyageInfoImplement implements ShipITPortVoyageInfoInterface
{
   private static final Logger LOGGER = Logger.getLogger(ShipITPortVoyageInfoImplement.class.getName());

   @Override
   public int setShip(ArrayList<ShipPortVoyageInfo> shipBeans)
      throws SQLException
   {
      int rowCommitted = 0;

      Connection conn = null;
      PreparedStatement stmt = null;

      try
      {
         conn = this.getConnection();

         conn.setAutoCommit(false);

         String stringSQL
            = "insert into TRACKING_SHIP_IT\n"
            + "(SEARCH_URL, FLAG, SHIPNAME, SHIPNAME_URL, URL_PHOTO, DESTINATION_PORT_NAME, DESTINATION_PORT_URL, REPORTED_ETA, REPORTED_DESTINATION,\n"
            + "CURRENT_PORT_NAME, CURRENT_PORT_URL, IMO, MMSI, SHIP_TYPE, LIVE_MAP_URL, CURRENT_LAT, CURRENT_LON, TIME_OF_LATEST_POSITION,\n"
            + "GLOBAL_AREA, LOCAL_AREA, LAT_OF_LATEST_POSITION, LON_OF_LATEST_POSITION, STATUS, ENI, SPEED, COURSE, DRAUGHT,\n"
            + "NAVIGATIONAL_STATUS, YEAR_OF_BUILD, LENGTH, WIDTH, DWT, CURRENT_PORT_UNLOCODE, CURRENT_PORT_COUNTRY, CALLSIGN,\n"
            + "SHIP_TYPE_GENERIC, SHIP_TYPE_SPECIFIC, GROSS_TONNAGE, VESSEL_FINDER_SEARCH_URL, VESSEL_FINDER_DETAIL_URL,\n"
            + "VESSEL_FINDER_LAT, VESSEL_FINDER_LON, TRACKING_DATE)\n"
            + "values\n"
            + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

         stmt = conn.prepareStatement(stringSQL);
             
         int rowInsereted = 0;
         for (ShipPortVoyageInfo shipBean : shipBeans)
         {
            rowInsereted++;
            
            stmt.setString(1, shipBean.getSearchUrl());
            stmt.setString(2, shipBean.getFlag());
            stmt.setString(3, shipBean.getShipName());
            stmt.setString(4, shipBean.getShipNameUrl());
            stmt.setString(5, shipBean.getUrlPhoto());
            stmt.setString(6, shipBean.getDestinationPortName());
            stmt.setString(7, shipBean.getDestinationPortUrl());
            stmt.setString(8, shipBean.getReportedEta());
            stmt.setString(9, shipBean.getReportedDestination());
            stmt.setString(10, shipBean.getCurrentPortName());
            stmt.setString(11, shipBean.getCurrentPortUrl());
            stmt.setString(12, shipBean.getImo());
            stmt.setString(13, shipBean.getMmsi());
            stmt.setString(14, shipBean.getShipType());
            stmt.setString(15, shipBean.getLiveMapUrl());
            stmt.setString(16, shipBean.getCurrentLat());
            stmt.setString(17, shipBean.getCurrentLon());
            stmt.setString(18, shipBean.getTimeOfLatestPosition());
            stmt.setString(19, shipBean.getGlobalArea());
            stmt.setString(20, shipBean.getLocalArea());
            stmt.setString(21, shipBean.getLatOfLatestPosition());
            stmt.setString(22, shipBean.getLonOfLatestPosition());
            stmt.setString(23, shipBean.getStatus());
            stmt.setString(24, shipBean.getEni());
            stmt.setString(25, shipBean.getSpeed());
            stmt.setString(26, shipBean.getCourse());
            stmt.setString(27, shipBean.getDraught());
            stmt.setString(28, shipBean.getNavigationalStatus());
            stmt.setString(29, shipBean.getYearOfBuild());
            stmt.setString(30, shipBean.getLength());
            stmt.setString(31, shipBean.getWidth());
            stmt.setString(32, shipBean.getDwt());
            stmt.setString(33, shipBean.getCurrentPortUnlocode());
            stmt.setString(34, shipBean.getCurrentPortCountry());
            stmt.setString(35, shipBean.getCallsign());
            stmt.setString(36, shipBean.getShipTypeGeneric());
            stmt.setString(37, shipBean.getShipTypeSpecific());
            stmt.setString(38, shipBean.getGrossTonnage());
            stmt.setString(39, shipBean.getVesselFinderSearchUrl());
            stmt.setString(40, shipBean.getVesselFinderDetailUrl());
            stmt.setString(41, shipBean.getVesselFinderLat());
            stmt.setString(42, shipBean.getVesselFinderLon());
            stmt.setString(43, shipBean.getTrackingDate());
            
            stmt.addBatch();
            
            if (rowInsereted == 1000)
            {
               rowInsereted = 0;
               int[] returnsSQL = stmt.executeBatch();
               rowCommitted += Arrays.stream(returnsSQL).sum();
               conn.commit();
            }
         }//for (ShipPortVoyageInfo shipBean : shipBeans)
         
         if (shipBeans.size() > rowCommitted)
         {
            int[] returnsSQL = stmt.executeBatch();
            rowCommitted += Arrays.stream(returnsSQL).sum();
            conn.commit();
         }
      }
      catch (SQLException ex)
      {
         LOGGER.log(Level.SEVERE, ex.getMessage());
         throw new SQLException();
      }
      catch (IOException ex)
      {
         LOGGER.log(Level.SEVERE, ex.getMessage());
      }
      finally
      {
         this.closePreparedStatement(stmt);
         this.closeConnection(conn);
      }

      return rowCommitted;
   }

   private Connection getConnection()
      throws SQLException, IOException
   {
      ConnectionDB connDB = new ConnectionDB();
      Connection conn = connDB.getConnection();

      return conn;
   }

   private void closePreparedStatement(PreparedStatement stmt)
   {
      if (stmt != null)
      {
         try
         {
            stmt.close();
         }
         catch (SQLException ex)
         {
            LOGGER.log(Level.SEVERE, ex.getMessage());
         }
      }
   }
   
   private void closeConnection(Connection conn)
   {
      if (conn != null)
      {
         try
         {
            conn.close();
         }
         catch (SQLException ex)
         {
            LOGGER.log(Level.SEVERE, ex.getMessage());
         }
      }
   }

}
