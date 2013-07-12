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

/**
 *
 * @author Harald <Harald at free-creations.de>
 */
public interface WiiListener {

  public static int CONNECTED = 0; // the connection has successfully been established
  public static int ABORTED = 1; // the connection failed for some reason
  public static int ENDED = 2; // the connection ended normally

  /**
   * Informs the listener about changes in the status of the Bluetooth
   * connection.
   *
   * @param status the new status of the connection.
   */
  public void connectionChanged(int connectionStatus);

  /**
   * Informs the listener that the Button A has been pushed or released.
   *
   * @param down true if the button has been pushed, false if the button has
   * been released.
   */
  public void buttonAChanged(boolean down);

  /**
   * Informs the listener that the Button B has been pushed or released.
   *
   * @param down true if the button has been pushed, false if the button has
   * been released.
   */
  public void buttonBChanged(boolean down);

  /**
   * Informs the listener that a button has been pushed or released.
   *
   * @param previousState a bitmap describing the state of all buttons before
   * the event occurred.
   * @param newState a bitmap describing the state of all buttons after the
   * event occurred.
   */
  public void buttonEvent(int previousState, int newState);
}
