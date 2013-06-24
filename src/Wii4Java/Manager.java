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

import java.io.File;

/**
 *
 * @author Harald <Harald at free-creations.de>
 */
public class Manager {

  /**
   * The nativeLibLoaded flag is true if the native library has been
   * successfully loaded.
   */
  private static boolean nativeLibLoaded = false;
  /**
   * Protects the nativeLibLoaded flag from concurrent access.
   */
  private static final Object nativeLibLoadingLock = new Object();
  /**
   * The whileConnecting flag tell whether we are about to connect.
   */
  private static boolean whileConnecting = false;
  /**
   * Protects the whileConnecting flag from concurrent access.
   */
  private static final Object whileConnectingLock = new Object();
  /**
   * Internal identifier for the MS-Windows operating system.
   */
  private static final String WIN = "win";
  /**
   * Internal identifier for the Linux operating system.
   */
  private static final String LINUX = "linux";
  /**
   * Internal identifier for the Mac OS X operating system.
   */
  private static final String MAC = "mac";

  /**
   * Load the native library. The OS specific functions are accessed through a
   * native library. This function can be used to load the library from a user
   * defined directory. To be effective, this function must be called before any
   * other function. If no library directory is given (libDir = null) the native
   * library will be searched in the Java- System directory.
   *
   * @param libDir the directory where the library is supposed to be or null if
   * the library should be fetched from the Java- System directory.
   * @throws UnsatisfiedLinkError If the native library could not be loaded
   */
  static public void loadNativeLibray(File libDir) throws UnsatisfiedLinkError {
    synchronized (nativeLibLoadingLock) {
      if (nativeLibLoaded) {
        //already loaded
        return;
      }
      if (libDir == null) {
        System.loadLibrary(getNativeLibrayName());
      } else {
        if (!libDir.exists()) {
          throw new UnsatisfiedLinkError("Directory \"" + libDir.getPath() + "\" does not exist.");
        }
        if (!libDir.isDirectory()) {
          throw new UnsatisfiedLinkError("File \"" + libDir.getPath() + "\" is not a directory.");
        }
        File libFile = new File(libDir,
                System.mapLibraryName(getNativeLibrayName()));
        if (!libFile.exists()) {
          throw new UnsatisfiedLinkError("Library \"" + libFile.getPath() + "\" does not exist.");
        }
        System.load(libFile.getAbsolutePath());
      }
      nativeLibLoaded = true;
    }
  }

  /**
   * Indicates whether the native library has been successfully loaded.
   *
   * @return true if the native library is successfully loaded
   */
  static public boolean isNativeLibLoaded() {
    synchronized (nativeLibLoadingLock) {
      return nativeLibLoaded;
    }
  }

  /**
   * Makes sure that the native library has been successfully loaded. If no
   * library has been loaded so far, this function will attempt to load the
   * library from the system path.
   *
   * @throws UnsatisfiedLinkError If the native library could not be loaded
   */
  static private void checkLoadNativeLib() throws UnsatisfiedLinkError {
    loadNativeLibray(null);
  }

  /**
   * Tries to establish a Bluetooth connection to the Wii.
   *
   * This function returns immediately. Information about the status of the
   * connection must be acquired through the WiiListener interface.
   *
   * @param listener the listener which shall receive messages from the Wii.
   */
  static public void connect(final WiiListener listener) {
    checkLoadNativeLib();
    synchronized (whileConnectingLock) {
      // avoid starting two threads
      if (whileConnecting) {
        return;
      }
      whileConnecting = true;
      Thread connectingThread = new Thread(null, new Runnable() {
        @Override
        public void run() {
          nConnect(listener);
          whileConnecting = false;
        }
      }, "WiiConnection");
      connectingThread.start();
    }

  }

  private static native void nConnect(WiiListener listener);

  static public void disconnect(WiiListener listener) {
    checkLoadNativeLib();
    synchronized (whileConnectingLock) {
      nDisconnect(listener);
    }
  }

  private static native void nDisconnect(WiiListener listener);

  /**
   * Determine the name of the operating system.
   *
   * @return either "win" or "linux" or "mac".
   */
  private static String getOsName() {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.indexOf("windows") > -1) {
      return WIN;
    }
    if (osName.indexOf("linux") > -1) {
      return LINUX;
    }
    if (osName.indexOf("mac") > -1) {
      return MAC;
    }
    throw new RuntimeException("Unsupported Operating System \"" + System.getProperty("os.name") + "\".");
  }

  /**
   * Determine if we are running on a 64 bit or a 32 bit version of JRE.
   *
   * @return the string "32" or "64".
   */
  private static String getJreBitness() {
    //let's try with "sun.arch.data.model"
    String sun_arch_data_model = System.getProperty("sun.arch.data.model");
    if ("64".equals(sun_arch_data_model)) {
      return "64";
    }
    if ("32".equals(sun_arch_data_model)) {
      return "32";
    }
    //"sun.arch.data.model" did not succeed let's try with "os.arch"
    String os_arch = System.getProperty("os.arch");
    if (os_arch.endsWith("64")) {
      return "64";
    }
    //nothing succeeded let's assume 32 bit
    return "32";
  }

  private static String getNativeLibrayName() {
    final String name = ProjectInfo.PROJECT_NAME + "-"
            + getOsName() + getJreBitness() + "-"
            + ProjectInfo.VERSION_MAJOR + "."
            + ProjectInfo.VERSION_MINOR + "."
            + ProjectInfo.VERSION_PATCH;
    return name;
  }
}
