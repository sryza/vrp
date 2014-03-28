/**
 * Copyright 2012 Sandy Ryza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pls.vrp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import org.apache.hadoop.fs.FileStatus;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class VrpHadoopMain {
    /*
  private static final Logger LOG = Logger.getLogger(VrpHadoopMain.class);
  
  private static final int DEFAULT_ROUND_TIME = 60 * 1000;
  
  public static void main(String[] args) throws IOException {
    //required args
    int numTasks = Integer.parseInt(args[0]);
    int k = Math.max(1, numTasks-1);
    int numRuns = Integer.parseInt(args[1]);
    int extraNeighbors = 0;
    int helperNeighbors = 0;
    //optional args
    boolean runLocal = false;
    int roundTime = DEFAULT_ROUND_TIME;
    File inputFile = new File("../vrptests/rc110_1.txt");
    boolean useBestForAll = true;
    boolean addFirstNeighbors = true;
    if (args.length > 2) {
      roundTime = Integer.parseInt(args[2]);
    }
    if (args.length > 3) {
      k = Integer.parseInt(args[3]);
    }
    if (args.length > 4) {
      if (args[4].matches("(true|false)")) {
        runLocal = Boolean.parseBoolean(args[4]);
      } else {
        inputFile = new File(args[4]);
      }
    }
    if (args.length > 5) {
      extraNeighbors = Integer.parseInt(args[5]);
    }
    if (args.length > 6) {
      helperNeighbors = Integer.parseInt(args[6]);
    }
    if (args.length > 7) {
      addFirstNeighbors = Boolean.parseBoolean(args[7]);
    }
    if (args.length > 8) {
      useBestForAll = Boolean.parseBoolean(args[8]);
    }
        
    final int maxDiscrepancies = 5;
    final int relaxationRandomness = 15;
    final int maxIter = 35;
    final int maxEscalation = 35;
    
    VrpProblem problem = VrpReader.readSolomon(inputFile, Integer.MAX_VALUE); 
    
    List<PlsSolution> initSols = new ArrayList<PlsSolution>();
    double bestStartCost = Double.MAX_VALUE;
    //create different initializations by varying weights on initializer
    for (int i = 0; i < numTasks; i++) {
      double timeDiffWeight = Math.random() * .3;
      double distanceWeight = Math.random() * .5;
      double urgencyWeight = Math.random() * .2;
      VrpGreedyInitializer initializer = new VrpGreedyInitializer(timeDiffWeight, distanceWeight, urgencyWeight);
      VrpSolution sol = initializer.nearestNeighborHeuristic(problem);
//      NumberFormat nf = NumberFormat.getPercentInstance();
//      System.out.println(nf.format(timeDiffWeight) + ", " + nf.format(distanceWeight) + ", " + nf.format(urgencyWeight)
//          + ": " + sol.getToursCost());
      if (sol.getToursCost() < bestStartCost) {
        bestStartCost = sol.getToursCost();
      }
      initSols.add(new VrpPlsSolution(sol, maxIter, maxEscalation, relaxationRandomness, maxDiscrepancies, i, -1));
    }
    
    PlsMaster master = new PlsMaster(runLocal);
    String dir = "/users/sryza/testdir/";
    long startTime = System.currentTimeMillis();
    
    PlsMetadata metadata = new PlsMetadata(k, bestStartCost, roundTime, useBestForAll, extraNeighbors, helperNeighbors, addFirstNeighbors);

    master.run(numRuns, initSols, dir, VrpMapper.class, VrpReducer.class, metadata, inputFile.getName());
    long endTime = System.currentTimeMillis();
    LOG.info("Total time: " + (endTime - startTime));
  }  */
}
