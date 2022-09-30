package eu.eurostat.trackingships.dao.marinetraffic;

import eu.eurostat.trackingships.bean.ShipPortVoyageInfo;
import java.sql.SQLException;
import java.util.ArrayList;

public interface ShipPortVoyageInfoInterface
{

   public int setShip(ArrayList<ShipPortVoyageInfo> shipBeans)
      throws SQLException;   

}
