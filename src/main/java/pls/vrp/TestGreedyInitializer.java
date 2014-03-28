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

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import pls.vrp.viz.VrpPanel;

public class TestGreedyInitializer {
  public static void main(String[] args) throws IOException {
    File f = new File("../vrptests/r1.txt");
    VrpProblem problem = VrpReader.readSolomon(f, 100);
    // seems like more for the first two and less for the last works
    VrpGreedyInitializer init = new VrpGreedyInitializer(1.0, 1.0, 0);
    VrpSolution sol = init.nearestNeighborHeuristic(problem);
    System.out.println(sol.getNumVehicles());
    System.out.println(sol.getToursCost());
    System.out.println(sol.verify(problem));
    
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    VrpPanel panel = new VrpPanel();
    panel.setScale(problem);
    panel.setSolution(sol);
    frame.getContentPane().add(panel);
    frame.pack();
    frame.setVisible(true);
  }
}
