package eu.eurostat.trackingships.dao.marinetraffic;

import eu.eurostat.trackingships.bean.ShipPortVoyageInfo;
import java.sql.SQLException;
import java.util.ArrayList;

public interface ShipEUPortVoyageInfoInterface
{

   public int setShip(ArrayList<ShipPortVoyageInfo> shipBeans)
      throws SQLException;   

}
