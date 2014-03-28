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
 * An insertion point and a set of customers removed at it.
 */
public class RemovedCustomers {
  public RouteNode insertAfter;
  public Set<Integer> custIds;
  
  public RemovedCustomers(RouteNode insertAfter, Set<Integer> custIds) {
    this.insertAfter = insertAfter;
    this.custIds = custIds;
  }
}
