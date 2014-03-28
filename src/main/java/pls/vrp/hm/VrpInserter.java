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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import pls.vrp.VrpProblem;

public class VrpInserter {
  private static final Logger LOG = Logger.getLogger(VrpInserter.class);
  
  private VrpProblem problem;
  
  public VrpInserter(VrpProblem problem) {
    this.problem = problem;
  }

  /**
   * 
   * @param beforeNode
   *     Insert the given customer after the given beforeNode
   * @param custId
   *     ID of the customer to be inserted
   * @return
   */
  public InsertionEffects insert(RouteNode beforeNode, int custId, CustInsertionPoints[] custInsertionPoints) {
    int[] serviceTimes = problem.getServiceTimes();
    int[] windowStartTimes = problem.getWindowStartTimes();
    int[] windowEndTimes = problem.getWindowEndTimes();
    boolean consistent = true;
    
    //reduce route capacity by demand
    beforeNode.route.remainingCapacity -= problem.getDemands()[custId];

    RouteNode afterNode = beforeNode.next;
    
    RouteNode newNode = new RouteNode(custId, afterNode, beforeNode, beforeNode.route);
    newNode.minDepartTime = VrpUtils.calcMinDepartTime(beforeNode.minDepartTime, 
        problem.getDistance(beforeNode.custId, custId), windowStartTimes[custId], serviceTimes[custId]);
    newNode.maxArriveTime = VrpUtils.calcMaxArriveTime(afterNode.maxArriveTime, 
        problem.getDistance(afterNode.custId, custId), windowEndTimes[custId], serviceTimes[custId]);
    
    afterNode.prev = newNode;
    beforeNode.next = newNode;

    //TODO: save this somewhere
    //TODO: need to remove custId from beforeNode.insertableAfter.  can do this while copying
    
    //find all cities that can still be inserted after city
    //update insertion lists for those cities
    
    newNode.insertableAfter = VrpUtils.validateInsertableCusts(
        beforeNode.insertableAfter.iterator(), custId, afterNode.custId, newNode.minDepartTime, 
        afterNode.maxArriveTime, problem, false);
    
    //need to add custId to insertion point lists of all nodes remaining in newNode.insertableAfter
    for (int insertableCustId : newNode.insertableAfter) {
      double cost = VrpUtils.costOfInsertion(custId, afterNode.custId, insertableCustId, problem);
      custInsertionPoints[insertableCustId].add(newNode, cost);
    }
    
    //find all cities that can still be inserted before city
    //update insertion lists for those cities
      //the cost of insertion at this point will change for the customer
      //that means we need to move the insertion point around in the customer's insertion list
      //for now when we revert we will just calculate again, but in the future we can save old position
    
    Set<Integer> removedFromBeforeNode = VrpUtils.validateInsertableCusts(
        beforeNode.insertableAfter.iterator(), beforeNode.custId, custId, beforeNode.minDepartTime, 
        newNode.maxArriveTime, problem, true);
        
    //for all customers still in beforeNode.insertableAfter, need to update cost of insertion (by taking into account)
    //new successor for the beforeNode entry in their insertion point lists
    for (int insertableCustId : beforeNode.insertableAfter) {
      double newCost = VrpUtils.costOfInsertion(beforeNode.custId, custId, insertableCustId, problem);
      custInsertionPoints[insertableCustId].update(beforeNode, newCost);
    }
    //for all customers in removedFromBeforeNode, need to remove beforeNode from their insertion point lists
    for (int insertableCustId : removedFromBeforeNode) {
      if (!custInsertionPoints[insertableCustId].remove(beforeNode)) {
        consistent = false;
//        LOG.info("inconsistent, " + insertableCustId + " has no insertion points");
      }
    }
    
    //propagate minVisitTime and maxDepartTime
    List<RouteNode> minDepartTimeChangedNodes = VrpUtils.propagateMinDepartTime(newNode, problem);
    List<RouteNode> maxArriveTimeChangedNodes = VrpUtils.propagateMaxVisitTime(newNode, problem);
    //remove insertion points due to new bounds
    List<RemovedCustomers> removedCustsLists = new ArrayList<RemovedCustomers>();
    for (RouteNode routeNode : minDepartTimeChangedNodes) {
      Set<Integer> removed = VrpUtils.validateInsertableCusts(routeNode.insertableAfter.iterator(),
          routeNode.custId, routeNode.next.custId, routeNode.minDepartTime, routeNode.next.maxArriveTime,
          problem, true);
      removedCustsLists.add(new RemovedCustomers(routeNode, removed));
    }
    for (RouteNode routeNode : maxArriveTimeChangedNodes) {
      Set<Integer> removed = VrpUtils.validateInsertableCusts(routeNode.prev.insertableAfter.iterator(),
          routeNode.prev.custId, routeNode.custId, routeNode.prev.minDepartTime, routeNode.maxArriveTime,
          problem, true);
      removedCustsLists.add(new RemovedCustomers(routeNode.prev, removed));
    }
    for (RemovedCustomers removedAtPoint : removedCustsLists) {
      for (int insertableCustId : removedAtPoint.custIds) {
        custInsertionPoints[insertableCustId].remove(removedAtPoint.insertAfter);
      }
    }
    
    removedCustsLists.add(new RemovedCustomers(beforeNode, removedFromBeforeNode));
    return new InsertionEffects(newNode, removedCustsLists, consistent);
  }
}
