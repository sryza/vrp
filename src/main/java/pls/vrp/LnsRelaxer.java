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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

public class LnsRelaxer {
  
  private int randomnessMeasure;
  private double maxDist; //used for normalizing distances for relatedness measure
  private final Random rand;
  
  public LnsRelaxer(int randomnessMeasure, double maxDist, Random rand) {
    this.randomnessMeasure = randomnessMeasure;
    this.maxDist = maxDist;
    this.rand = rand;
  }
  
  /**
   * The input solution is not modified.
   */
  public VrpSolution relaxShaw(VrpSolution sol, int numToRelax, int firstToRemove) {
    VrpProblem problem = sol.getProblem();
    ArrayList<Integer> removedCities = new ArrayList<Integer>(numToRelax);
    HashSet<Integer> remainingCities = new HashSet<Integer>();
    
    int[] cityVehicles = new int[problem.getNumCities()];
    int vehicle = 0;
    for (List<Integer> route : sol.getRoutes()) {
      for (int cityId : route) {
        cityVehicles[cityId] = vehicle;
      }
      vehicle++;
    }
    
    //choose first to remove
    if (firstToRemove == -1) {
      firstToRemove = (int)(rand.nextDouble() * problem.getNumCities());
    }
    for (int i = 0; i < problem.getNumCities(); i++) {
      if (i == firstToRemove) {
        removedCities.add(i);
      } else {
        remainingCities.add(i);
      }
    }
    //remove the rest
    for (int i = 1; i < numToRelax; i++) {
      //take a random removed node
      int removedCityId = removedCities.get((int)(rand.nextDouble() * removedCities.size()));
      int rank = (randomnessMeasure == Integer.MAX_VALUE) ? 0 :
          (int)(Math.pow(rand.nextDouble(), randomnessMeasure) * remainingCities.size());
      int cityId = chooseByRankAndRelatedness(remainingCities, sol, rank, removedCityId, cityVehicles);
      remainingCities.remove(cityId);
      removedCities.add(cityId);
    }
    //build the new solution
    List<List<Integer>> newRoutes = buildRoutesWithoutCusts(sol.getRoutes(), removedCities);
        
    return new VrpSolution(newRoutes, removedCities, problem);
  }
  
  public List<List<Integer>> buildRoutesWithoutCusts(List<List<Integer>> routes, List<Integer> toRemove) {
    HashSet<Integer> removedCitiesSet = new HashSet<Integer>(toRemove);
    List<List<Integer>> newRoutes = new ArrayList<List<Integer>>(routes.size());
    for (List<Integer> oldRoute : routes) {
      List<Integer> newRoute = new ArrayList<Integer>();
      newRoutes.add(newRoute);
      for (Integer cityId : oldRoute) {
        if (!removedCitiesSet.contains(cityId)) {
          newRoute.add(cityId);
        }
      }
    }
    
    return newRoutes;
  }
  
  /**
   * @param cityId
   *     the id of the city that we're determining relatedness in relation to
   */
  private int chooseByRankAndRelatedness(HashSet<Integer> remaining, VrpSolution sol, int rank, int cityId, int[] cityVehicles) {
    //the head is the least element, i.e. the one whose compareTo(any other element) is less than 0
    //we want the head of the queue to be the least related customer
    //thus, we want compareTo to return negative if the argument is more related than us
    PriorityQueue<CityRelatedness> heap = new PriorityQueue<CityRelatedness>(rank+1);
    for (int remainingCityId : remaining) {
      double relatedness = relatedness(cityId, remainingCityId, sol, cityVehicles);
      if (heap.size() < rank + 1 || relatedness > heap.peek().relatedness) {
        if (heap.size() == rank + 1) {
          heap.remove();
        }
        heap.add(new CityRelatedness(remainingCityId, relatedness));
      }
    }
    return heap.peek().cityId;
  }
  
  private double relatedness(int nodeId1, int nodeId2, VrpSolution sol, int[] cityVehicles) {
    double dist = sol.getProblem().getDistances()[nodeId1][nodeId2];
    double denom = dist / maxDist;
    if (cityVehicles[nodeId1] == cityVehicles[nodeId2]) {
      denom += 1.0;
    }
    return 1/denom;
  }
  
  public List<Integer> findDifferingNodes(VrpSolution sol1, VrpSolution sol2, List<Integer> neighborhood) {
    int[] preds1 = getPredecessors(sol1);
    int[] preds2 = getPredecessors(sol2);
    List<Integer> differing = new ArrayList<Integer>(neighborhood.size());
    for (int i = 0; i < preds1.length; i++) {
      if (preds1[i] != preds2[i]) {
        differing.add(i);
      }
    }
    return differing;
  }
  
  public List<Integer> findDifferingNodes2(VrpSolution sol1, VrpSolution sol2, List<Integer> neighborhood) {
    int[] preds1 = getPredecessors(sol1);
    int[] succs1 = getSuccessors(sol1);
    int[] preds2 = getPredecessors(sol2);
    int[] succs2 = getSuccessors(sol2);
    List<Integer> differing = new ArrayList<Integer>(neighborhood.size());
    for (int cust : neighborhood) {
      if (preds1[cust] != preds2[cust] || succs1[cust] != succs2[cust]) {
        differing.add(cust);
      }
    }
    return differing;
  }
  
  private int[] getSuccessors(VrpSolution sol) {
    int[] succs = new int[sol.getProblem().getNumCities()];
    for (List<Integer> route : sol.getRoutes()) {
      Iterator<Integer> iter = route.iterator();
      int prev = iter.next();
      while (iter.hasNext()) {
        int cur = iter.next();
        succs[prev] = cur;
        prev = cur;
      }
      succs[prev] = -1;
    }
    return succs;
  }
  
  private int[] getPredecessors(VrpSolution sol) {
    int[] preds = new int[sol.getProblem().getNumCities()];
    for (List<Integer> route : sol.getRoutes()) {
      Iterator<Integer> iter = route.iterator();
      int prev = iter.next();
      preds[prev] = -1;
      while (iter.hasNext()) {
        int cur = iter.next();
        preds[cur] = prev;
        prev = cur;
      }
    }
    return preds;
  }
  
  private class CityRelatedness implements Comparable<CityRelatedness> {
    public int cityId;
    public double relatedness;

    public CityRelatedness(int cityId, double relatedness) {
      this.cityId = cityId;
      this.relatedness = relatedness;
    }
    
    @Override
    public int compareTo(CityRelatedness other) {
      return (int)Math.signum(this.relatedness - other.relatedness);
    }
  }
}
