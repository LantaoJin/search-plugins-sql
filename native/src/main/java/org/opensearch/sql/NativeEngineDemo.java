/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql;

public class NativeEngineDemo {

  public static void main(String[] args) {
    System.out.println("=== OpenSearch SQL Native Engine Demo ===");

    try {
      NativeEngine engine = new NativeEngine();

      System.out.println("\n1. Testing Java -> C++ calls:");
      int sum = engine.testAdd(10, 20);
      System.out.println("   Native add(10, 20) = " + sum);

      int product = engine.testMultiply(7, 8);
      System.out.println("   Native multiply(7, 8) = " + product);

      System.out.println("\n2. Testing C++ -> Java callback:");
      engine.testCallback();

      System.out.println("\n3. Testing Substrait plan printing:");
      // Test with mock protobuf bytes
      byte[] testPlan = {0x08, 0x01, 0x12, 0x04, 0x74, 0x65, 0x73, 0x74};
      System.out.println("Mock plan:");
      String result1 = NativeEngine.printSubstraitPlan(testPlan);
      System.out.println("Returned JSON length: " + result1.length());

      // Test with a more complex mock plan (simulating real Substrait structure)
      byte[] complexPlan = {
        0x0A,
        0x10, // Field 1 (version), length 16
        0x08,
        0x01, // major_number = 1
        0x10,
        0x02, // minor_number = 2
        0x18,
        0x03, // patch_number = 3
        0x22,
        0x08,
        0x69,
        0x73,
        0x74,
        0x68,
        0x6D,
        0x75,
        0x73, // producer = "isthmus"
        0x12,
        0x06, // Field 2 (relations), length 6
        0x0A,
        0x04, // RelRoot, length 4
        0x08,
        0x01,
        0x10,
        0x02 // mock relation data
      };
      System.out.println("\nComplex mock plan:");
      String result2 = NativeEngine.printSubstraitPlan(complexPlan);
      System.out.println("Returned JSON length: " + result2.length());

      System.out.println("\n=== Demo completed successfully! ===");

    } catch (Exception e) {
      System.err.println("Demo failed: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
