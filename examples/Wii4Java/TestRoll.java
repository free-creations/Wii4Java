/*
 * Copyright 2013 Harald <Harald at free-creations.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package Wii4Java;

import java.io.IOException;

/**
 *
 * @author Harald <Harald at free-creations.de>
 */
public class TestRoll {

  private static class Listener extends WiiListener {

    int prevX = 0;
    int prevZ = 0;
    boolean btnAdown = false;
    int roll = 0; //the roll angle in arbitrary units

    @Override
    public void connectionChanged(int connectionStatus) {
      switch (connectionStatus) {
        case WiiListener.CONNECTED:
          System.out.println("Connection succeeded.");
          return;
        case WiiListener.ABORTED:
          System.out.println("Connection aborted.");
          return;
        case WiiListener.ENDED:
          System.out.println("Connection ended.");
          return;
        default:
          System.out.println("Connection unknown status.");
      }
    }

    @Override
    public void buttonAChanged(boolean down) {
      btnAdown = down;
      roll = 0;
    }

    @Override
    public void buttonBChanged(boolean down) {
    }

    @Override
    public void buttonEvent(int previousState, int newState) {
    }

    @Override
    public void accelerometerEvent(int accX, int ignore, int accZ) {
      if (btnAdown) {
        calculateRoll(
                accX,
                accZ,
                prevX - accX,
                prevZ - accZ);
      }
      prevX = accX;
      prevZ = accZ;
    }
    
    private void calculateRoll(int X, int Z, int deltaX, int deltaZ){
      int deltaR = deltaRoll( X,  Z,  deltaX,  deltaZ);
      roll += deltaR;
      System.out.println(" Roll = "+roll);
      
    }
    private int deltaRoll(int X, int Z, int deltaX, int deltaZ){
      return X*deltaZ -Z*deltaX;
      
    }
  }
  
  

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.println("Wii test...");
    Listener listener = new Listener();
    Manager.connect(listener);

    Thread.sleep(60000);

    Manager.disconnect(listener);
  }
}
