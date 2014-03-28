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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import pls.vrp.VrpProblem;
import pls.vrp.VrpSolution;

public class VrpSearcher {
  private static final Logger LOG = Logger.getLogger(VrpSearcher.class);
  
  private VrpInserter inserter;
  private VrpReverter reverter;
  private VrpProblem problem;
  
  public VrpSearcher(VrpProblem problem) {
    this.problem = problem;
    inserter = new VrpInserter(problem);
    reverter = new VrpReverter(problem);
  }
  
  //TODO: worry about insertions making cost go above bestCost
  public VrpCpSearchNode initialize(VrpSolution partialSol) {
    int[] demands = partialSol.getProblem().getDemands();
    List<List<Integer>> routes = partialSol.getRoutes();
    List<Integer> unrouted = partialSol.getUninsertedNodes();
    
    //build routes
    List<RouteNode> routeStarts = new ArrayList<RouteNode>(routes.size());
    List<RouteNode> routeEnds = new ArrayList<RouteNode>(routes.size());
    for (List<Integer> routeCusts : routes) {
      Route route = new Route(problem.getVehicleCapacity());
      RouteNode prevNode = new RouteNode(-1, null, null, route);
      routeStarts.add(prevNode);
      for (int custId : routeCusts) {
        RouteNode node = new RouteNode(custId, null, prevNode, route);
        route.remainingCapacity -= demands[custId];
        prevNode.next = node;
        prevNode = node;
      }
      RouteNode endNode = new RouteNode(-1, null, prevNode, route);
      prevNode.next = endNode;
      routeEnds.add(endNode);
    }
    
    //calculate minDepartTimes and maxVisitTimes
    for (RouteNode routeStart : routeStarts) {
      routeStart.minDepartTime = 0;
      VrpUtils.propagateMinDepartTime(routeStart, problem);
    }
    for (RouteNode routeEnd : routeEnds) {
      routeEnd.maxArriveTime = Integer.MAX_VALUE;
      VrpUtils.propagateMaxVisitTime(routeEnd, problem);
    }

    //determine what's insertable where
    CustInsertionPoints[] custsInsertionPoints = new CustInsertionPoints[problem.getNumCities()];
    BoundRemaining boundRemaining = new BoundRemaining(problem.getNumCities());
    for (int unroutedCustId : unrouted) {
      custsInsertionPoints[unroutedCustId] = new CustInsertionPoints(boundRemaining, unroutedCustId, problem.getNumCities());
    }
    for (RouteNode routeStart : routeStarts) {
      RouteNode node = routeStart;
      do {
        node.insertableAfter = VrpUtils.validateInsertableCusts(unrouted.iterator(), node.custId, node.next.custId, 
            node.minDepartTime, node.next.maxArriveTime, problem, false);
        for (int insertableCustId : node.insertableAfter) {
          double cost = VrpUtils.costOfInsertion(node.custId, node.next.custId, insertableCustId, problem);
          custsInsertionPoints[insertableCustId].add(node, cost);
        }
        node = node.next;
      } while (node.custId != -1);
    }
    
    double curCost = partialSol.getToursCost();
    return new VrpCpSearchNode(new HashSet<Integer>(unrouted), custsInsertionPoints, boundRemaining, curCost, routeStarts);
  }
  
  public VrpSolution solve(VrpSolution partialSol, double bestCost, int discrepancies, VrpCpStats stats, boolean best) {
    VrpCpSearchNode root = initialize(partialSol);
    return search(root.unrouted, root.custsInsertionPoints, root.boundRemaining, bestCost, root.curCost, root.routeStarts,
        discrepancies, stats, best);
  }
  
  //TODO: we can include the sum of min insertion costs in our bound
  
  private VrpSolution search(Set<Integer> remainingToInsert, CustInsertionPoints[] custsInsertionPoints, 
      BoundRemaining boundRemaining, double bestCost, double curCost, List<RouteNode> routeStarts, int discrepancies,
      VrpCpStats stats, boolean best) {
    
    int[] demands = problem.getDemands();
    
    //TODO: base case
    if (remainingToInsert.isEmpty()) {
      //we've found a solution
      //we could assert here that bound remaining is 0
      List<List<Integer>> solRoutes = new ArrayList<List<Integer>>();
      for (RouteNode routeStart : routeStarts) {
        RouteNode routeNode = routeStart.next;
        List<Integer> routeCustIds = new ArrayList<Integer>();
        while (routeNode.custId != -1) {
          routeCustIds.add(routeNode.custId);
          routeNode = routeNode.next;
        }
        if (routeCustIds.size() > 0) { //ignore empty routes
          solRoutes.add(routeCustIds);
        }
      }
      if (stats != null) {
        stats.reportNodeEvaluated();
      }
      VrpSolution sol = new VrpSolution(solRoutes, problem);
      if (Math.abs(sol.getToursCost() - curCost) > .001) {
        LOG.error("costs inconsistent! " + sol.getToursCost() + " != " + curCost);
        return null;
      } else if (Math.abs(curCost - bestCost) > .001) {
        if (stats != null) {
          stats.reportNewBestSolution(curCost);
        }
        return sol;
      } else {
        return null;
      }
    }
    
    if (discrepancies <= 0) {
      return null;
    }
    
    if (stats != null) {
      stats.reportNodeEvaluated();
    }
    
    //determine city whose minimum insert cost is the largest
    //its insertion points should be ordered
    int custToInsert = chooseCustToInsert(remainingToInsert, custsInsertionPoints);
    remainingToInsert.remove(custToInsert);
    boundRemaining.notifyCustInserted(custToInsert);
    
    CustInsertionPoints insertionPoints = custsInsertionPoints[custToInsert];
    custsInsertionPoints[custToInsert] = null; //just to catch bugs
    VrpSolution bestSol = null;
    
    //remove custToInsert from insertion point insertable-customer sets
    Iterator<RouteNode> iter = insertionPoints.inCostOrderIterator();
    while (iter.hasNext()) {
      RouteNode next = iter.next();
      if (!next.insertableAfter.remove(custToInsert)) {
        LOG.error("lists not in sync");
      }
    }
    
    iter = insertionPoints.inCostOrderIterator();
    while (iter.hasNext() && discrepancies > 0) {
      RouteNode insertAfter = iter.next();
      //make sure capacity not violated
      if (demands[custToInsert] > insertAfter.route.remainingCapacity) {
        continue;
      }
      //make sure bound not violated
      double costOfInsertion = VrpUtils.costOfInsertion(insertAfter.custId, insertAfter.next.custId, custToInsert, problem);
      if (curCost + costOfInsertion >= bestCost) {
        continue;
      }
      
//      stats.reportAboutToInsert();
      InsertionEffects changes = inserter.insert(insertAfter, custToInsert, custsInsertionPoints);
//      stats.reportFinishedInsertion();

      //TODO: if any custs have no insertion points after this, give up
      if (changes.consistent && curCost + costOfInsertion + boundRemaining.getBound() < bestCost) {
        VrpSolution sol = search(remainingToInsert, custsInsertionPoints, boundRemaining, bestCost, 
            curCost + costOfInsertion, routeStarts, discrepancies, stats, best);
        if (sol != null && (bestSol == null || sol.getToursCost() < bestSol.getToursCost())) {
          bestSol = sol;
          bestCost = sol.getToursCost(); // to bound other children
        }
        if (sol != null && !best) {
          return sol;
        }
        discrepancies--; //only count as discrepancy if we explore below
      }
      reverter.revert(changes, custsInsertionPoints);
    }
    
    boundRemaining.notifyCustReverted(custToInsert);
    remainingToInsert.add(custToInsert);
    custsInsertionPoints[custToInsert] = insertionPoints;
    
    //add custToInsert back to insertion point insertable-customer sets
    iter = insertionPoints.inCostOrderIterator();
    while (iter.hasNext()) {
      RouteNode next = iter.next();
      next.insertableAfter.add(custToInsert);
    }
    
    return bestSol;
  }
  
  private int chooseCustToInsert(Collection<Integer> remainingToInsert, CustInsertionPoints[] custsInsertionPoints) {
    Iterator<Integer> iter = remainingToInsert.iterator();
    double maxMinCost = Integer.MIN_VALUE;
    int bestCustId = -1;
    while (iter.hasNext()) {
      int custToInsertId = iter.next();
      double cost = custsInsertionPoints[custToInsertId].getMinCost();
      if (cost > maxMinCost) {
        maxMinCost = cost;
        bestCustId = custToInsertId;
      }
    }
    return bestCustId;
  }
  
  /**
   * Checks to see whether an insertion will violate constraints.
   */
  private void checkForConsistency() {
    //by our bookkeeping, it shouldn't violate time window constraints
    //so check to see whether it violates capacity constraints
    //or increases the cost over our lower bound
    
    //check to see whether any cities have no insertion points left and give up if so

  }
}
