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

import java.util.List;

import org.apache.log4j.Logger;

import pls.vrp.VrpProblem;

public class VrpReverter {
  private static final Logger LOG = Logger.getLogger(VrpReverter.class);
  
  private VrpProblem problem;
  
  public VrpReverter(VrpProblem problem) {
    this.problem = problem;
  }
  
  public void revert(InsertionEffects effects, CustInsertionPoints[] custInsertionPoints) {
    RouteNode insertedNode = effects.insertedNode;

    RouteNode beforeNode = insertedNode.prev;
    RouteNode afterNode = insertedNode.next;
    
    beforeNode.next = insertedNode.next;
    afterNode.prev = insertedNode.prev;

    //increase route capacity by demand
    beforeNode.route.remainingCapacity += problem.getDemands()[insertedNode.custId];
    
    //propagate minDepartTime and maxVisitTime
    VrpUtils.propagateMaxVisitTime(afterNode, problem);
    VrpUtils.propagateMinDepartTime(beforeNode, problem);
    
    //update costs for insertion for all nodes still in beforeNode.insertableAfter
    for (int insertableCustId : beforeNode.insertableAfter) {
      double cost = VrpUtils.costOfInsertion(beforeNode.custId, afterNode.custId, insertableCustId, problem);
      custInsertionPoints[insertableCustId].update(beforeNode, cost);
    }
    
    //remove insertedNode from all custInsertionPoints
    for (int insertableCustId : insertedNode.insertableAfter) {
      custInsertionPoints[insertableCustId].remove(insertedNode);
    }
    
    List<RemovedCustomers> removedCustsList = effects.removedCustsList;
    //put back removed insertion possibilities
    for (RemovedCustomers removedAtPoint : removedCustsList) {
      for (int insertableCustId : removedAtPoint.custIds) {
        RouteNode insertAfter = removedAtPoint.insertAfter;
        insertAfter.insertableAfter.add(insertableCustId);
        double cost = VrpUtils.costOfInsertion(insertAfter.custId, insertAfter.next.custId, insertableCustId, problem);
        custInsertionPoints[insertableCustId].add(insertAfter, cost);
      }
    }
  }
}
