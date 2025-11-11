/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.substrait;

import io.substrait.extension.DefaultExtensionCatalog;
import io.substrait.isthmus.SubstraitRelVisitor;
import io.substrait.plan.ImmutablePlan;
import io.substrait.plan.Plan;
import io.substrait.plan.PlanProtoConverter;
import lombok.experimental.UtilityClass;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.sql.SqlKind;

@UtilityClass
public class SubstraitConverter {

  public static Plan convert(RelNode relNode) {
    ImmutablePlan.Builder builder = io.substrait.plan.Plan.builder();
    builder.version(
        Plan.Version.builder().from(Plan.Version.DEFAULT_VERSION).producer("isthmus").build());

    RelRoot root = RelRoot.of(relNode, SqlKind.SELECT);
    Plan.Root planRoot =
        SubstraitRelVisitor.convert(root, DefaultExtensionCatalog.DEFAULT_COLLECTION);
    builder.addRoots(planRoot);
    return builder.build();
  }

  public static io.substrait.proto.Plan execute(RelNode relNode) {
    PlanProtoConverter planToProto = new PlanProtoConverter();
    return planToProto.toProto(convert(relNode));
  }
}
