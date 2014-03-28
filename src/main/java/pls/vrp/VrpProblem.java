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

public class VrpProblem {
  private int vehicleCapacity;
  private int[] demands;
  private int[] serviceTimes;
  private int[] windowStartTimes;
  private int[] windowEndTimes;
  private double[][] cityDists;
  private double[] distsFromDepot;
  
  private int depotX;
  private int depotY;
  private int[] xCoors;
  private int[] yCoors;
  
  private double maxDist;
  
  public VrpProblem(int[] demands, int[] xCoors, int[] yCoors, int[] serviceTimes,
      int[] windowStartTimes, int[] windowEndTimes, int depotX, int depotY, int capacity) {
    this.demands = demands;
    this.serviceTimes = serviceTimes;
    this.windowStartTimes = windowStartTimes;
    this.windowEndTimes = windowEndTimes;
    this.vehicleCapacity = capacity;
    this.xCoors = xCoors;
    this.yCoors = yCoors;
    this.depotX = depotX;
    this.depotY = depotY;
    
    buildDistsArrays();
  }
  
  private void buildDistsArrays() {
    cityDists = new double[demands.length][demands.length];
    distsFromDepot = new double[demands.length];
    for (int i = 0; i < demands.length; i++) {
      int xDiffFromDepot = xCoors[i] - depotX;
      int yDiffFromDepot = yCoors[i] - depotY;
      distsFromDepot[i] = Math.sqrt(xDiffFromDepot * xDiffFromDepot + yDiffFromDepot * yDiffFromDepot);
      
      for (int j = 0; j < demands.length; j++) {
        int xDiff = xCoors[i] - xCoors[j];
        int yDiff = yCoors[i] - yCoors[j];
        cityDists[i][j] = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
        if (cityDists[i][j] > maxDist) {
          maxDist = cityDists[i][j];
        }
      }
    }
  }
  
  public int getDepotX() {
    return depotX;
  }
  
  public int getDepotY() {
    return depotY;
  }
  
  public int[] getXCoors() {
    return xCoors;
  }
  
  public int[] getYCoors() {
    return yCoors;
  }
  
  public double getMaxDistance() {
    return maxDist;
  }
  
  public double[] getDistancesFromDepot() {
    return distsFromDepot;
  }
  
  public int[] getServiceTimes() {
    return serviceTimes;
  }
  
  public double[][] getDistances() {
    return cityDists;
  }
  
  public int[] getWindowStartTimes() {
    return windowStartTimes;
  }
  
  public int[] getWindowEndTimes() {
    return windowEndTimes;
  }
  
  public int[] getDemands() {
    return demands;
  }
  
  //if id's are negative, they refer to the depot
  public double getDistance(int custId1, int custId2) {
    if (custId1 >= 0 && custId2 >= 0) {
      return cityDists[custId1][custId2];
    } else if (custId1 >= 0) {
      return distsFromDepot[custId1];
    } else if (custId2 >= 0){
      return distsFromDepot[custId2];
    } else {
      return 0; //both depot
    }
  }
  
  public int getVehicleCapacity() {
    return vehicleCapacity;
  }
  
  public int getNumCities() {
    return demands.length;
  }
}
