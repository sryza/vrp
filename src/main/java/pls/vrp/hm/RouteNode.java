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

import java.util.Set;

public class RouteNode {
  public final int custId; //-1 if it's a depot node
  public RouteNode next;
  public RouteNode prev;
  //the earliest that the current path allows us to arrive at customer
  public double minDepartTime;
  //the latest that the current path allows us to depart and not violate
  //time windows in the future, - our service time
  public double maxArriveTime;
  public Route route;
  
  //ids of customers that can be inserted after this node
  public Set<Integer> insertableAfter;
  
  public RouteNode(int custId, RouteNode next, RouteNode prev, Route route) {
    this.custId = custId;
    this.next = next;
    this.prev = prev;
    this.route = route;
  }
  
  public int hashCode() {
    if (custId != -1) {
      return custId;
    } else {
      return super.hashCode();
    }
  }
}