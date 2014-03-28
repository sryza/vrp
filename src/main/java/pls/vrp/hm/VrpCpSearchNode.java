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
import java.util.Set;

/**
 * A node in a vehicle routing problem CP search tree.
 */
public class VrpCpSearchNode {
  public Set<Integer> unrouted;
  public CustInsertionPoints[] custsInsertionPoints;
  public BoundRemaining boundRemaining;
  public double curCost;
  public List<RouteNode> routeStarts;
  
  public VrpCpSearchNode(Set<Integer> unrouted, CustInsertionPoints[] custsInsertionPoints, 
      BoundRemaining boundRemaining, double curCost, List<RouteNode> routeStarts) {
    this.unrouted = unrouted;
    this.custsInsertionPoints = custsInsertionPoints;
    this.curCost = curCost;
    this.boundRemaining = boundRemaining;
    this.routeStarts = routeStarts;
  }
}
