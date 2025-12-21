/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.opensearch.planner.rules;

import java.util.Objects;
import java.util.function.Predicate;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rel.logical.LogicalSort;
import org.immutables.value.Value;
import org.opensearch.sql.calcite.plan.OpenSearchRuleConfig;
import org.opensearch.sql.calcite.utils.PlanUtils;
import org.opensearch.sql.opensearch.storage.scan.AbstractCalciteIndexScan;
import org.opensearch.sql.opensearch.storage.scan.CalciteLogicalIndexScan;

@Value.Enclosing
public class SortIndexScanRule extends InterruptibleRelRule<SortIndexScanRule.Config> {

  protected SortIndexScanRule(Config config) {
    super(config);
  }

  @Override
  protected void onMatchImpl(RelOptRuleCall call) {
    final Sort sort = call.rel(0);
    final AbstractCalciteIndexScan scan = call.rel(1);
    if (sort.getConvention() != scan.getConvention()) {
      return;
    }

    var collations = sort.collation.getFieldCollations();
    AbstractCalciteIndexScan newScan = scan.pushDownSort(collations);
    if (newScan != null) {
      if (newScan instanceof CalciteLogicalIndexScan) {
        Integer fetchValue = LimitIndexScanRule.extractLimitValue(sort.fetch);
        Integer offsetValue = LimitIndexScanRule.extractOffsetValue(sort.offset);
        AbstractCalciteIndexScan limitPushed =
            (AbstractCalciteIndexScan)
                ((CalciteLogicalIndexScan) newScan)
                    .pushDownLimit((LogicalSort) sort, fetchValue, offsetValue);
        if (limitPushed != null) {
          call.transformTo(limitPushed);
        } else if (fetchValue != null) {
          call.transformTo(
              call.builder()
                  .push(newScan)
                  .limit(fetchValue, Objects.requireNonNullElse(offsetValue, 0))
                  .build());
        } else {
          call.transformTo(newScan);
        }
      } else {
        call.transformTo(newScan);
      }
    }
  }

  /** Rule configuration. */
  @Value.Immutable
  public interface Config extends OpenSearchRuleConfig {
    SortIndexScanRule.Config DEFAULT =
        ImmutableSortIndexScanRule.Config.builder()
            .build()
            .withOperandSupplier(
                b0 ->
                    b0.operand(Sort.class)
                        .predicate(Predicate.not(PlanUtils::isLimitOnly))
                        .oneInput(
                            b1 ->
                                b1.operand(AbstractCalciteIndexScan.class)
                                    // Skip the rule if Top-K(i.e. sort + limit) has already been
                                    // pushed down. Otherwise,
                                    // Continue to push down sort although limit has already been
                                    // pushed down since we don't promise collation with only limit.
                                    .predicate(
                                        Predicate.not(AbstractCalciteIndexScan::isTopKPushed)
                                            .and(
                                                Predicate.not(
                                                    AbstractCalciteIndexScan
                                                        ::isMetricsOrderPushed)))
                                    .noInputs()));

    @Override
    default SortIndexScanRule toRule() {
      return new SortIndexScanRule(this);
    }
  }
}
