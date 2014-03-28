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

import java.util.Random;

import org.apache.log4j.Logger;

import pls.vrp.hm.VrpCpStats;
import pls.vrp.hm.VrpSearcher;

public class VrpLnsRunner {
  
  private static final Logger LOG = Logger.getLogger(VrpLnsRunner.class);

  public VrpPlsSolution[] run(VrpPlsSolution solAndStuff, long timeToFinish, Random rand) {
    long startTime = System.currentTimeMillis();
    
    VrpSolution sol = solAndStuff.getSolution();
    VrpProblem problem = sol.getProblem();
    LnsRelaxer relaxer = new LnsRelaxer(solAndStuff.getRelaxationRandomness(), problem.getMaxDistance(), rand);
    VrpSearcher solver = new VrpSearcher(problem);

    int numTries = 0;
    int numSuccesses = 0;
    double beforeBestCost = sol.getToursCost();
    long regStartTime = System.currentTimeMillis();
    outer:
    while (true) {
      for (int n = solAndStuff.getCurEscalation(); n <= solAndStuff.getMaxEscalation(); n++) { 
        for (int i = solAndStuff.getCurIteration(); i < solAndStuff.getMaxIterations(); i++) {
          if (System.currentTimeMillis() >= timeToFinish) {
            break outer;
          }
          
          VrpCpStats stats = new VrpCpStats();
          VrpSolution partialSol = relaxer.relaxShaw(sol, n, -1);
          
          VrpSolution newSol = solver.solve(partialSol, sol.getToursCost(), solAndStuff.getMaxDiscrepancies(), stats, true);
          if (newSol != null && Math.abs(newSol.getToursCost() - sol.getToursCost()) > .001) {
            sol = newSol;
            solAndStuff.setSolution(sol);
            i = 0;
            numSuccesses++;
          }
          solAndStuff.setCurEscalation(n);
          solAndStuff.setCurIteration(i);
          numTries++;
        }
      }
      //LOG.info("Starting new search");
      solAndStuff.setCurEscalation(1);
      solAndStuff.setCurIteration(0);
    }
    long regEndTime = System.currentTimeMillis();
    int regTime = (int)(regEndTime - regStartTime);
    // extraData.setRegularStats(numSuccesses, numTries, beforeBestCost - sol.getToursCost(), regTime);
    
    long endTime = System.currentTimeMillis();
    LOG.info("VrpLnsRunner took " + (endTime - startTime) + " ms");
    
    return new VrpPlsSolution[] {solAndStuff};
  }
  
}
