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

package pls.vrp.hm;

import org.apache.log4j.Logger;

public class VrpCpStats {
  
  private static final Logger LOG = Logger.getLogger(VrpCpStats.class);
  
  private int nEvaluated;
  private long insertTimeStart;
  private long maxInsertTime;
  private boolean quiet;
  
  public VrpCpStats(boolean quiet) {
    this.quiet = quiet;
  }
  
  public VrpCpStats() {
    quiet = true;
  }
  
  public void reportNodeEvaluated() {
    nEvaluated++;
  }
  
  public int getNumNodesEvaluated() {
    return nEvaluated;
  }
  
  public void reportAboutToInsert() {
    insertTimeStart = System.currentTimeMillis();
  }
  
  public void reportFinishedInsertion() {
    long time = System.currentTimeMillis() - insertTimeStart;
    if (time > maxInsertTime) {
      maxInsertTime = time;
    }
  }
  
  public void reportNewBestSolution(double cost) {
    if (!quiet) {
      LOG.info("found solution with cost " + cost);
    }
  }
  
  public long getMaxInsertTime() {
    return maxInsertTime;
  }
}
