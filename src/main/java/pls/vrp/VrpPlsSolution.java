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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class VrpPlsSolution {
  private int maxIter;
  private int maxEscalation;
  private int relaxationRandomness;
  private int maxDiscrepancies;
  
  private int curIter;
  private int curEscalation;
  
  private int solId;
  private int parentSolId;
  
  private VrpSolution sol;
  
  private boolean readProblem = true;
  
  public VrpPlsSolution() {
  }
  
  public VrpPlsSolution(boolean readProblem) {
    this.readProblem = readProblem;
  }
  
  public VrpPlsSolution(VrpSolution sol, int maxIter, int maxEscalation, int relaxationRandomness, int maxDiscrepancies, 
      int solId, int parentSolId) {
    this.sol = sol;
    this.maxIter = maxIter;
    this.maxEscalation = maxEscalation;
    this.maxDiscrepancies = maxDiscrepancies;
    this.relaxationRandomness = relaxationRandomness;

    this.solId = solId;
    this.parentSolId = parentSolId;
  }
  
  public int getCurEscalation() {
    return curEscalation;
  }
  
  public void setCurEscalation(int curEscalation) {
    this.curEscalation = curEscalation;
  }
  
  public int getMaxEscalation() {
    return maxEscalation;
  }
  
  public int getMaxDiscrepancies() {
    return maxDiscrepancies;
  }
  
  public int getCurIteration() {
    return curIter;
  }
  
  public void setCurIteration(int curIter) {
    this.curIter = curIter;
  }
  
  public int getMaxIterations() {
    return maxIter;
  }
  
  public int getRelaxationRandomness() {
    return relaxationRandomness;
  }
  
  public VrpSolution getSolution() {
    return sol;
  }
  
  public void setSolution(VrpSolution sol) {
    this.sol = sol;
  }
  
  public int getSolutionId() {
    return solId;
  }
  
  public void setSolutionId(int id) {
    this.solId = id;
  }
  
  public int getParentSolutionId() {
    return parentSolId;
  }
  
  public void setParentSolutionId(int id) {
    this.parentSolId = id;
  }
  
  public double getCost() {
    return sol.getToursCost();
  }
  
  private VrpProblem buildProblemFromStream(DataInput dis) throws IOException {
    int numCities = dis.readShort();
    int depotX = dis.readShort();
    int depotY = dis.readShort();
    int vehicleCapacity = dis.readShort();
    int[] serviceTimes = new int[numCities];
    int[] demands = new int[numCities];
    int[] windowStartTimes = new int[numCities];
    int[] windowEndTimes = new int[numCities];
    int[] xCoors = new int[numCities];
    int[] yCoors = new int[numCities];
    for (int i = 0; i < numCities; i++) {
      demands[i] = dis.readShort();
      serviceTimes[i] = dis.readShort();
      windowStartTimes[i] = dis.readShort();
      windowEndTimes[i] = dis.readShort();
      xCoors[i] = dis.readShort();
      yCoors[i] = dis.readShort();
    }
    
    return new VrpProblem(demands, xCoors, yCoors, serviceTimes, windowStartTimes, windowEndTimes,
        depotX, depotY, vehicleCapacity);
  }
  
  public void writeProblemToStream(VrpProblem problem, DataOutput dos) throws IOException {
    dos.writeShort(problem.getDemands().length);
    dos.writeShort(problem.getDepotX());
    dos.writeShort(problem.getDepotY());
    dos.writeShort(problem.getVehicleCapacity());
    for (int i = 0; i < problem.getNumCities(); i++) {
      dos.writeShort(problem.getDemands()[i]);
      dos.writeShort(problem.getServiceTimes()[i]);
      dos.writeShort(problem.getWindowStartTimes()[i]);
      dos.writeShort(problem.getWindowEndTimes()[i]);
      dos.writeShort(problem.getXCoors()[i]);
      dos.writeShort(problem.getYCoors()[i]);
    }
  }
  
  
  public boolean equals(Object o) {
    VrpPlsSolution other = (VrpPlsSolution)o;
    VrpSolution otherSol = other.getSolution();
    if (!otherSol.getRoutes().equals(sol.getRoutes())) {
      return false;
    }
    return true;
  }
}
