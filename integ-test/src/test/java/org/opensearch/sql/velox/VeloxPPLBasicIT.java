/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.velox;

import static org.opensearch.sql.legacy.TestsConstants.TEST_INDEX_ACCOUNT;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.opensearch.sql.calcite.standalone.VeloxPPLIntegTestCase;

public class VeloxPPLBasicIT extends VeloxPPLIntegTestCase {
  @Override
  public void init() throws IOException {
    super.init();
    enableCalcite();
    loadIndex(Index.ACCOUNT);
  }

  @Test
  public void testExplain() throws IOException {
    String actual =
        explainQuery(
            String.format(
                "source=%s | where age >= 1.0 and age < 10 | fields age", TEST_INDEX_ACCOUNT));
    System.out.println(actual);
  }
}
