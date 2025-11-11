/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql;

public class NativeEngine {

  static {
    boolean loaded = false;
    try {
      // Try loading with absolute path first
      String libPath = System.getProperty("java.library.path");
      if (libPath != null && !libPath.isEmpty()) {
        String[] paths = libPath.split(System.getProperty("path.separator"));
        for (String path : paths) {
          String fullPath =
              path + System.getProperty("file.separator") + "opensearch-sql-native.dylib";
          java.io.File libFile = new java.io.File(fullPath);
          if (libFile.exists()) {
            System.load(fullPath);
            System.out.println("Successfully loaded native library from: " + fullPath);
            loaded = true;
            break;
          }
        }
      }
      // Fallback to system library loading
      if (!loaded) {
        System.loadLibrary("opensearch-sql-native");
      }
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Failed to load native library: " + e.getMessage());
      throw e; // Re-throw the exception instead of hiding it
    }
  }

  public static native int nativeAdd(int a, int b);

  public static native int nativeMultiply(int a, int b);

  public native void nativeCallJava();

  public static native String printSubstraitPlan(byte[] planBytes);

  public void onNativeCallback(String message) {
    System.out.println("Callback from C++: " + message);
  }

  public int testAdd(int a, int b) {
    return nativeAdd(a, b);
  }

  public int testMultiply(int a, int b) {
    return nativeMultiply(a, b);
  }

  public void testCallback() {
    nativeCallJava();
  }

  public static String printSubstraitPlan(io.substrait.proto.Plan plan) {
    return printSubstraitPlan(plan.toByteArray());
  }
}
