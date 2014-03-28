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

package pls.vrp.viz;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import pls.vrp.VrpProblem;
import pls.vrp.VrpSolution;

import static pls.vrp.viz.GraphPanelUtils.*;

public class VrpPanel extends JPanel {

    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 600;
    private static final int DOT_RADIUS = 3;
    private static final int DEPOT_RADIUS = 6;
    private static final int PADDING = 20;
    private static final Color NODES_COLOR = Color.black;

    private static final int MAX_ROUTES = 200;

    private Color[] routeColors = new Color[MAX_ROUTES];
    private VrpSolution sol;

    private int xOffset;
    private int yOffset;
    private double scale;

    public VrpPanel() {
        for (int i = 0; i < routeColors.length; i++) {
            routeColors[i] = GraphPanelUtils.randomColor();
        }
        setBackground(Color.white);
        setPreferredSize(new Dimension(MAX_WIDTH, MAX_HEIGHT));
    }

    public void setSolution(VrpSolution sol) {
        this.sol = sol;
        repaint();
    }

    public void setScale(VrpProblem problem) {
        int[] xCoors = problem.getXCoors();
        int[] yCoors = problem.getYCoors();

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int i = 0; i < problem.getNumCities(); i++) {
            minX = Math.min(minX, xCoors[i]);
            minY = Math.min(minY, yCoors[i]);
            maxX = Math.max(maxX, xCoors[i]);
            maxY = Math.max(maxY, yCoors[i]);
        }

        xOffset = minX;
        yOffset = minY;
        double xScale = (MAX_WIDTH-PADDING*2) / (double)(maxX - minX);
        double yScale = (MAX_HEIGHT-PADDING*2) / (double)(maxY - minY);
        scale = Math.min(xScale, yScale);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        List<List<Integer>> routes = sol.getRoutes();
        int[] xCoors = sol.getProblem().getXCoors();
        int[] yCoors = sol.getProblem().getYCoors();
        int depotX = sol.getProblem().getDepotX();
        int depotY = sol.getProblem().getDepotY();

        //draw routes
        int routeNum = 0;
        for (List<Integer> route : routes) {
            g.setColor(routeColors[routeNum]);
            Iterator<Integer> iter = route.iterator();
            if (route.isEmpty()) {
                continue;
            }
            int prev = iter.next();
            int x1 = cityToPix(xCoors[prev], xOffset, scale, PADDING);
            int y1 = cityToPix(yCoors[prev], yOffset, scale, PADDING);
            int x2 = cityToPix(depotX, xOffset, scale, PADDING);
            int y2 = cityToPix(depotY, yOffset, scale, PADDING);
            g.drawLine(x1, y1, x2, y2);

            while (iter.hasNext()) {
                int cur = iter.next();
                x1 = cityToPix(xCoors[prev], xOffset, scale, PADDING);
                y1 = cityToPix(yCoors[prev], yOffset, scale, PADDING);
                x2 = cityToPix(xCoors[cur], xOffset, scale, PADDING);
                y2 = cityToPix(yCoors[cur], yOffset, scale, PADDING);
                g.drawLine(x1, y1, x2, y2);

                int cityX = cityToPix(xCoors[cur], xOffset, scale, PADDING);
                int cityY = cityToPix(yCoors[cur], yOffset, scale, PADDING);
                g.fillOval(cityX-DOT_RADIUS, cityY-DOT_RADIUS, DOT_RADIUS*2, DOT_RADIUS*2);

                prev = cur;
            }
            x1 = cityToPix(xCoors[prev], xOffset, scale, PADDING);
            y1 = cityToPix(yCoors[prev], yOffset, scale, PADDING);
            x2 = cityToPix(depotX, xOffset, scale, PADDING);
            y2 = cityToPix(depotY, yOffset, scale, PADDING);
            g.drawLine(x1, y1, x2, y2);


            routeNum++;
        }

        //paint the cities
//		g.setColor(NODES_COLOR);
//		for (int i = 0; i < xCoors.length; i++) {
//			int x = cityToPix(xCoors[i], xOffset, scale, PADDING);
//			int y = cityToPix(yCoors[i], yOffset, scale, PADDING);
//			g.fillOval(x-DOT_RADIUS, y-DOT_RADIUS, DOT_RADIUS*2, DOT_RADIUS*2);
//		}
        //paint the depot
        int x = cityToPix(depotX, xOffset, scale, PADDING);
        int y = cityToPix(depotY, yOffset, scale, PADDING);
        g.fillOval(x-DEPOT_RADIUS, y-DEPOT_RADIUS, DEPOT_RADIUS*2, DEPOT_RADIUS*2);

    }
}
