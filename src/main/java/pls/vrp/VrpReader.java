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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads vehicle routing problems in the Solomon format from input files
 */
public class VrpReader {
  public static VrpProblem readSolomon(File f, int numCities) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(f));
    
    String line = br.readLine();
    int capacity = Integer.parseInt(line.trim());
    line = br.readLine();
    String[] tokens = line.trim().split("\\s+");
    int depotX = (int)(Double.parseDouble(tokens[1]));
    int depotY = (int)(Double.parseDouble(tokens[2]));
    
    List<String> lines = new ArrayList<String>();
    for (int i = 0; i < numCities && (line = br.readLine()) != null; i++) {
      lines.add(line);
    }
    
    numCities = lines.size();
    int[] xCoors = new int[numCities];
    int[] yCoors = new int[numCities];
    int[] demands = new int[numCities];
    int[] windowStarts = new int[numCities];
    int[] windowEnds = new int[numCities];
    int[] serviceTimes = new int[numCities];

    for (int i = 0; i < numCities; i++) {
      tokens = lines.get(i).trim().split("\\s+");
      //CUST NO.   XCOORD.   YCOORD.    DEMAND   READY TIME   DUE DATE   SERVICE TIME
      int x = (int)(Double.parseDouble(tokens[1]));
      xCoors[i] = (x);
      int y = (int)(Double.parseDouble(tokens[2]));
      yCoors[i] = (y);
      int demand = (int)(Double.parseDouble(tokens[3]));
      demands[i] = (demand);
      int windowStart = (int)(Double.parseDouble(tokens[4]));
      windowStarts[i] = (windowStart);
      int windowEnd = (int)(Double.parseDouble(tokens[5]));
      windowEnds[i] = (windowEnd);
      int serviceTime = (int)(Double.parseDouble(tokens[6]));
      serviceTimes[i] = (serviceTime);
    }
    
    VrpProblem problem = new VrpProblem(demands, xCoors, yCoors, serviceTimes, 
        windowStarts, windowEnds, depotX, depotY, capacity);
    return problem;
  }
}
