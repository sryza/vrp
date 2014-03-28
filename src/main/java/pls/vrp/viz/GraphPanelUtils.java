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

public class GraphPanelUtils {
  public static Color randomColor() {
    return new Color((int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256));
  }

  public static int cityToPix(int x, int offset, double scale, int padding) {
    return (int)((x - offset)*scale) + padding;
  }
}