package eu.eurostat.trackingships.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.asift.ASIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.Pair;

public class ASIFMatcher
{
   private static final Logger LOGGER = Logger.getLogger(ASIFMatcher.class.getName());

   public static void main(String[] args)
   {
      try
      {
         //read the images from two streams
         final String dirName = "C:\\NetBeansProjects\\WPE\\TrackingShips\\TrackingShips\\files\\vesselfinder\\photos";
         final String fileName1 = dirName + File.separator + "IMO-7108930_MMSI-311166000_2.jpg";
         final String fileName2 = dirName + File.separator + "IMO-7108930_MMSI-311166000_4.jpg";
         File file1 = new File(fileName1);
         File file2 = new File(fileName2);
         InputStream stream1 = new FileInputStream(file1);
         InputStream stream2 = new FileInputStream(file2);
         final FImage fImage1 = ImageUtilities.readF(stream1);
         final FImage fImage2 = ImageUtilities.readF(stream2);

         //prepare the engine
         final ASIFTEngine engine = new ASIFTEngine(false, 7);

         //extract the keypoints from both images
         final LocalFeatureList<Keypoint> features1 = engine.findKeypoints(fImage1);
         LOGGER.log(Level.INFO, "Extracted input image1 {0}", features1.size());
         final LocalFeatureList<Keypoint> features2 = engine.findKeypoints(fImage2);
         LOGGER.log(Level.INFO, "Extracted input image2 {0}", features2.size());

         //prepare the matcher
         //basic matcher
         //LocalFeatureMatcher<Keypoint> matcher = createFastBasicMatcher();
         //homographic consistency matcher
         final LocalFeatureMatcher<Keypoint> matcher = createConsistentRANSACHomographyMatcher();
         
         //find features in image 1
         matcher.setModelFeatures(features1);
         //find features in image 2
         matcher.findMatches(features2);     
         
         //get the matches
         final List<Pair<Keypoint>> matches = matcher.getMatches();
         LOGGER.log(Level.INFO, "Number of Matches {0}", matches.size());

         //display the results
         final MBFImage inp1MBF = fImage1.toRGB();
         final MBFImage inp2MBF = fImage2.toRGB();
         DisplayUtilities.display(MatchingUtilities.drawMatches(inp1MBF, inp2MBF, matches, RGBColour.RED));      
         
         //close file stream
         stream1.close();
         stream2.close();
      }
      catch (IOException ex)
      {
         LOGGER.log(Level.SEVERE, null, ex);
      }
   }

   private static LocalFeatureMatcher<Keypoint> createConsistentRANSACHomographyMatcher()
   {
      final ConsistentLocalFeatureMatcher2d<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<>(createFastBasicMatcher());
      matcher.setFittingModel(new RobustHomographyEstimator(10.0, 1000, new RANSAC.BestFitStoppingCondition(), HomographyRefinement.NONE));

      return matcher;
   }

   private static LocalFeatureMatcher<Keypoint> createFastBasicMatcher()
   {
      return new FastBasicKeypointMatcher<>(8);
   }

}
