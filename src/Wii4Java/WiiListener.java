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

import javax.swing.SwingUtilities;

/**
 *
 * @author Harald <Harald at free-creations.de>
 */
public abstract class WiiListener {

  public final static int CONNECTED = 0; // the connection has successfully been established
  public final static int ABORTED = 1; // the connection failed for some reason
  public final static int ENDED = 2; // the connection ended normally

  /**
   * Informs the listener about changes in the status of the Bluetooth
   * connection.
   *
   * @param status the new status of the connection.
   */
  private void connectionChangedNative(final int connectionStatus) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        connectionChanged(connectionStatus);
      }
    });
  }

  /**
   * Informs the listener that the Button A has been pushed or released.
   *
   * @param down true if the button has been pushed, false if the button has
   * been released.
   */
  private void buttonAChangedNative(final boolean down) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        buttonAChanged(down);
      }
    });
  }

  /**
   * Informs the listener that the Button B has been pushed or released.
   *
   * @param down true if the button has been pushed, false if the button has
   * been released.
   */
  private void buttonBChangedNative(final boolean down) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        buttonBChanged(down);
      }
    });
  }

  /**
   * Informs the listener that a button has been pushed or released.
   *
   * @param previousState a bitmap describing the state of all buttons before
   * the event occurred.
   * @param newState a bitmap describing the state of all buttons after the
   * event occurred.
   */
  private void buttonEventNative(final int previousState, final int newState) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        buttonEvent(previousState, newState);
      }
    });
  }

  private void accelerometerEventNative(final int accX, final int accY, final int accZ) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        accelerometerEvent(accX, accY, accZ);
      }
    });
  }

  /**
   * Informs the listener about changes in the status of the Bluetooth
   * connection.
   *
   * @param status the new status of the connection.
   */
  public abstract void connectionChanged(int connectionStatus);

  /**
   * Informs the listener that the Button A has been pushed or released.
   *
   * @param down true if the button has been pushed, false if the button has
   * been released.
   */
  public abstract void buttonAChanged(boolean down);

  /**
   * Informs the listener that the Button B has been pushed or released.
   *
   * @param down true if the button has been pushed, false if the button has
   * been released.
   */
  public abstract void buttonBChanged(boolean down);

  /**
   * Informs the listener that a button has been pushed or released.
   *
   * @param previousState a bitmap describing the state of all buttons before
   * the event occurred.
   * @param newState a bitmap describing the state of all buttons after the
   * event occurred.
   */
  public abstract void buttonEvent(int previousState, int newState);

  /**
   * Informs the listener about the current accelerometer state.
   *
   * @param accX acceleration in the x direction (value ranging from -124 to 124)
   * @param accY acceleration in the y direction
   * @param accZ acceleration in the z direction
   */
  public abstract void accelerometerEvent(int accX, int accY, int accZ);
}
