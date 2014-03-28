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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import pls.vrp.VrpProblem;

public class VrpUtils {
  
  /**
   * @param newNode
   *     should have minDepartTime set correctly
   * @return
   *     a list of affected nodes
   */
  public static List<RouteNode> propagateMinDepartTime(RouteNode newNode, VrpProblem problem) {
    int[] serviceTimes = problem.getServiceTimes();
    int[] windowStartTimes = problem.getWindowStartTimes();
    
    List<RouteNode> affectedList = new ArrayList<RouteNode>();
    
    RouteNode curNode = newNode.next;
    while (curNode.custId != -1) { //while we haven't reached depot
      double newCurNodeMinDepartTime = Math.max(curNode.prev.minDepartTime + 
          problem.getDistance(curNode.prev.custId, curNode.custId), windowStartTimes[curNode.custId]) +
          serviceTimes[curNode.custId];
      if (Math.abs(newCurNodeMinDepartTime - curNode.minDepartTime) < .001) {
        //if nothing's changed here, nothing's gonna change in the future
        break;
      }
      curNode.minDepartTime = newCurNodeMinDepartTime;
      affectedList.add(curNode);
      curNode = curNode.next;
    }
    
    return affectedList;
  }
  
  /**
   * 
   * @param newNode
   *     Should have its maxVisitTime set correctly
   * @param problem
   * @return
   */
  public static List<RouteNode> propagateMaxVisitTime(RouteNode newNode, VrpProblem problem) {
    int[] serviceTimes = problem.getServiceTimes();
    int[] windowEndTimes = problem.getWindowEndTimes();
    
    List<RouteNode> affectedList = new ArrayList<RouteNode>();
    
    RouteNode curNode = newNode.prev;
    while (curNode.custId != -1) {
      //not sure this is right
      double newCurNodeMaxDepartTime = curNode.next.maxArriveTime - problem.getDistance(curNode.next.custId, curNode.custId);
      double newCurNodeMaxArriveTime = Math.min(windowEndTimes[curNode.custId], 
          newCurNodeMaxDepartTime - serviceTimes[curNode.custId]);
      if (Math.abs(newCurNodeMaxArriveTime - curNode.maxArriveTime) < .001) {
        break;
      }
      curNode.maxArriveTime = newCurNodeMaxArriveTime;
      affectedList.add(curNode);
      curNode = curNode.prev;
    }
    return affectedList;
  }
  
  public static double costOfInsertion(int custIdBefore, int custIdAfter, int custIdToInsert, VrpProblem problem) {
    return problem.getDistance(custIdBefore, custIdToInsert) + 
      problem.getDistance(custIdToInsert, custIdAfter) - 
      problem.getDistance(custIdBefore, custIdAfter);
  }
  
  /**
   * Validates the customers that are insertable at a given point.
   * 
   * @param custsIter
   * @param custBefore
   *     The predecessor to a would be inserted node. -1 if it's the depot.
   * @param custAfter
   *     The successor to a would be inserted node. -1 if it's the depot.
   * @param minDepartTime
   *     The new minimum depart time for predecessor.
   * @param minVisitTime
   *     The new maximum visit time for successor.
   * @param remove
   *     If true, then pruned custs will be removed from the original list, and the removed list will be returned.
   *     Otherwise, the returned list will be those that remain.
   * @return
   */
  public static Set<Integer> validateInsertableCusts(Iterator<Integer> custsIter, int custBefore, int custAfter, double minDepartTime, 
      double maxVisitTime, VrpProblem problem, boolean remove) {
    
    int[] serviceTimes = problem.getServiceTimes();
    int[] windowStartTimes = problem.getWindowStartTimes();
    int[] windowEndTimes = problem.getWindowEndTimes();
    double[][] distances = problem.getDistances();
    double[] distsFromBefore = (custBefore == -1) ? problem.getDistancesFromDepot() : distances[custBefore];
    double[] distsFromAfter = (custAfter == -1) ? problem.getDistancesFromDepot() : distances[custAfter];
    Set<Integer> list = new HashSet<Integer>();
    
    while (custsIter.hasNext()) {
      int custId = custsIter.next();
      boolean insertable = true;
      double custMinArriveTime = minDepartTime + distsFromBefore[custId];
      if (custMinArriveTime > windowEndTimes[custId]) {
        insertable = false;
      } else {
        double custMinDepartTime = Math.max(windowStartTimes[custId], custMinArriveTime) + serviceTimes[custId];
        if (custMinDepartTime + distsFromAfter[custId] > maxVisitTime) {
          insertable = false;
        }
      }
      if (!insertable && remove) {
        custsIter.remove();
        list.add(custId);
      } else if (insertable && !remove) {
        list.add(custId);
      }
    }
    
    return list;
  }
  
  public static double calcMinDepartTime(double prevMinDepartTime, double dist, int windowStartTime, int serviceTime) {
    return Math.max(prevMinDepartTime + dist, windowStartTime) + serviceTime;
  }
  
  public static double calcMaxArriveTime(double nextMaxArriveTime, double dist, int windowEndTime, int serviceTime) {
    return Math.min(nextMaxArriveTime - dist - serviceTime, windowEndTime);
  }
}
