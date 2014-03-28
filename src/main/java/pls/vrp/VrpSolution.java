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

import java.util.Iterator;
import java.util.List;

/**
 * Represents a solution or partial solution to the vehicle routing problem.
 */
public class VrpSolution {
  //values of -1 point to the depot
  private List<List<Integer>> routes;
  private List<Integer> unrouted;
  private int numVehicles;
  private VrpProblem problem;
  private double toursCost = -1;
  
  public VrpSolution(List<List<Integer>> routes, VrpProblem problem) {
    this.routes = routes;
    this.problem = problem;
    this.numVehicles = routes.size();
  }
  
  public VrpSolution(List<List<Integer>> routes, VrpProblem problem, double toursCost) {
    this(routes, problem);
    this.toursCost = toursCost;
  }
  
  public VrpSolution(List<List<Integer>> routes, List<Integer> unroutedNodes, VrpProblem problem) {
    this(routes, problem);
    this.unrouted = unroutedNodes;
  }
  
  private double calcToursCost(List<List<Integer>> routes, VrpProblem problem) {
    double[][] distances = problem.getDistances();
    double[] distancesFromDepot = problem.getDistancesFromDepot();
    
    double toursCost = 0;
    for (List<Integer> route : routes) {
      Iterator<Integer> iter = route.iterator();
      if (!iter.hasNext()) {
        continue;
      }
      int prev = iter.next();
      toursCost += distancesFromDepot[prev];
      while (iter.hasNext()) {
        int cur = iter.next();
        toursCost += distances[prev][cur];
        prev = cur;
      }
      toursCost += distancesFromDepot[prev];
    }
    return toursCost;
  }
  
  public VrpProblem getProblem() {
    return problem;
  }
  
  public List<List<Integer>> getRoutes() {
    return routes;
  }
  
  public List<Integer> getUninsertedNodes() {
    return unrouted;
  }
  
  public int getNumVehicles() {
    return numVehicles;
  }
  
  /**
   * Verify that the solution satisfied the constraints for the given problem,
   * and that the reported values for the objective function are correct.
   */
  public boolean verify(VrpProblem problem) {
    double[][] distances = problem.getDistances();
    double[] distancesFromDepot = problem.getDistancesFromDepot();
    
//    int toursCost = calcToursCost(routes, problem);
    
    int[] windowStartTimes = problem.getWindowStartTimes();
    int[] windowEndTimes = problem.getWindowEndTimes();
    int[] serviceTimes = problem.getServiceTimes();
    int[] demands = problem.getDemands();
    boolean[] visited = new boolean[problem.getNumCities()];
    
    //follow the paths and make sure that the time constraints hold
    for (List<Integer> route : routes) {
      Iterator<Integer> iter = route.iterator();
      if (route.isEmpty()) {
        System.out.println("EMPTY ROUTE!!!");
        continue;
      }
      int prev = iter.next();
      if (windowEndTimes[prev] < distancesFromDepot[prev]) {
        System.out.println("first node violated time constraint: endTime=" + 
            windowEndTimes[prev] + ", dist=" + distancesFromDepot[prev]);
        return false;
      }
      visited[prev] = true;
      int remCapacity = problem.getVehicleCapacity() - demands[prev];
      double minVisitTime = Math.max(windowStartTimes[prev], distancesFromDepot[prev]);
      
      while (iter.hasNext()) {
        int cur = iter.next();
        visited[cur] = true;
        double nextMinVisitTime = Math.max(minVisitTime + serviceTimes[prev] + distances[prev][cur], windowStartTimes[cur]);
        if (nextMinVisitTime > windowEndTimes[cur]) {
          System.out.println(minVisitTime + "\t" + serviceTimes[prev] + "\t" + distances[prev][cur]);
          System.out.println("violated time constraint for " + prev + "->" + cur + 
              ": endTime=" + windowEndTimes[cur] + ", visitTime=" + nextMinVisitTime);
          return false;
        }
        minVisitTime = nextMinVisitTime;
        
        remCapacity -= demands[cur];
        if (remCapacity < 0) {
          System.out.println("violated capacity constraint");
          return false;
        }
        prev = cur;
      }
    }  
    
    for (boolean b : visited) {
      if (!b) {
        System.out.println("one of the nodes not visited");
        return false;
      }
    }
    
//    if (toursCost != this.toursCost) {
//      System.out.println("tour costs do not match: " + toursCost + " != " + this.toursCost);
//      return false;
//    }
    
    return true;
  }
  
  public double getToursCost() {
    if (toursCost >= 0) {
      return toursCost;
    } else {
      return toursCost = calcToursCost(routes, problem);
    }
  }
}
