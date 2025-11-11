/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.velox;

import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlExplainLevel;
import org.opensearch.sql.AbstractNativeExecutionEngine;
import org.opensearch.sql.ast.statement.Explain;
import org.opensearch.sql.calcite.CalcitePlanContext;
import org.opensearch.sql.calcite.utils.PlanUtils;
import org.opensearch.sql.common.response.ResponseListener;
import org.opensearch.sql.substrait.SubstraitConverter;

public class VeloxExecutionEngineAbstract extends AbstractNativeExecutionEngine {

  @Override
  public void execute(
      RelNode rel, CalcitePlanContext context, ResponseListener<QueryResponse> listener) {
    if (PlanUtils.containsAggregate(rel)) {
      io.substrait.proto.Plan plan = SubstraitConverter.execute(rel);
      System.out.println(plan.toString());
    } else {
      super.execute(rel, context, listener);
    }
  }

  @Override
  public void explain(
      RelNode rel,
      Explain.ExplainFormat format,
      CalcitePlanContext context,
      ResponseListener<ExplainResponse> listener) {
    try {
      if (format == Explain.ExplainFormat.SUBSTRAIT) {
        String logical = RelOptUtil.toString(rel, SqlExplainLevel.NO_ATTRIBUTES);
        String substrait = SubstraitConverter.execute(rel).toString();
        listener.onResponse(
            new ExplainResponse(new ExplainResponseNodeV2(logical, substrait, null)));
      }
    } catch (Exception e) {
      listener.onFailure(e);
    }
  }
}
