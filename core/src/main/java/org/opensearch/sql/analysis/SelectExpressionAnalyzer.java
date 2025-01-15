/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.analysis;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.opensearch.sql.analysis.symbol.Namespace;
import org.opensearch.sql.analysis.symbol.Symbol;
import org.opensearch.sql.ast.AbstractNodeVisitor;
import org.opensearch.sql.ast.expression.Alias;
import org.opensearch.sql.ast.expression.AllFields;
import org.opensearch.sql.ast.expression.Field;
import org.opensearch.sql.ast.expression.Function;
import org.opensearch.sql.ast.expression.NestedAllTupleFields;
import org.opensearch.sql.ast.expression.QualifiedName;
import org.opensearch.sql.ast.expression.UnresolvedExpression;
import org.opensearch.sql.data.type.ExprType;
import org.opensearch.sql.expression.DSL;
import org.opensearch.sql.expression.Expression;
import org.opensearch.sql.expression.NamedExpression;
import org.opensearch.sql.expression.ReferenceExpression;

/**
 * Analyze the select list in the {@link AnalysisContext} to construct the list of {@link
 * NamedExpression}.
 */
@RequiredArgsConstructor
public class SelectExpressionAnalyzer
    extends AbstractNodeVisitor<List<NamedExpression>, AnalysisContext> {
  private final ExpressionAnalyzer expressionAnalyzer;

  private ExpressionReferenceOptimizer optimizer;

  /** Analyze Select fields. */
  public List<NamedExpression> analyze(
      List<UnresolvedExpression> selectList,
      AnalysisContext analysisContext,
      ExpressionReferenceOptimizer optimizer) {
    this.optimizer = optimizer;
    ImmutableList.Builder<NamedExpression> builder = new ImmutableList.Builder<>();
    for (UnresolvedExpression unresolvedExpression : selectList) {
      builder.addAll(unresolvedExpression.accept(this, analysisContext));
    }
    return builder.build();
  }

  @Override
  public List<NamedExpression> visitField(Field node, AnalysisContext context) {
    return Collections.singletonList(DSL.named(node.accept(expressionAnalyzer, context)));
  }

  @Override
  public List<NamedExpression> visitAlias(Alias node, AnalysisContext context) {
    // Expand all nested fields if used in SELECT clause
    if (node.getDelegated() instanceof NestedAllTupleFields) {
      return node.getDelegated().accept(this, context);
    }

    Expression expr = referenceIfSymbolDefined(node, context);
    return Collections.singletonList(DSL.named(node.getName(), expr));
  }

  /**
   * The Alias could be
   *
   * <ol>
   *   <li>SELECT name, AVG(age) FROM s BY name -> Project(Alias("name", expr), Alias("AVG(age)",
   *       aggExpr)) Agg(Alias("AVG(age)", aggExpr))
   *   <li>SELECT length(name), AVG(age) FROM s BY length(name) Project(Alias("name", expr),
   *       Alias("AVG(age)", aggExpr)) Agg(Alias("AVG(age)", aggExpr))
   *   <li>SELECT length(name) as l, AVG(age) FROM s BY l Project(Alias("l", expr),
   *       Alias("AVG(age)", aggExpr)) Agg(Alias("AVG(age)", aggExpr), Alias("length(name)",
   *       groupExpr))
   * </ol>
   */
  private Expression referenceIfSymbolDefined(Alias expr, AnalysisContext context) {
    UnresolvedExpression delegatedExpr = expr.getDelegated();

    // Pass named expression because expression like window function loses full name
    // (OVER clause) and thus depends on name in alias to be replaced correctly
    return optimizer.optimize(
        DSL.named(
            delegatedExpr.toString(),
            delegatedExpr.accept(expressionAnalyzer, context),
            expr.getName()),
        context);
  }

  @Override
  public List<NamedExpression> visitAllFields(AllFields node, AnalysisContext context) {
    TypeEnvironment environment = context.peek();
    Map<String, ExprType> lookupAllFields = environment.lookupAllFields(Namespace.FIELD_NAME);
    return lookupAllFields.entrySet().stream()
        .map(
            entry ->
                DSL.named(
                    entry.getKey(), new ReferenceExpression(entry.getKey(), entry.getValue())))
        .collect(Collectors.toList());
  }

  @Override
  public List<NamedExpression> visitNestedAllTupleFields(
      NestedAllTupleFields node, AnalysisContext context) {
    TypeEnvironment environment = context.peek();
    Map<String, ExprType> lookupAllTupleFields =
        environment.lookupAllTupleFields(Namespace.FIELD_NAME);
    environment.resolve(new Symbol(Namespace.FIELD_NAME, node.getPath()));

    // Match all fields with same path as used in nested function.
    Pattern p = Pattern.compile(node.getPath() + "\\.[^\\.]+$");
    return lookupAllTupleFields.entrySet().stream()
        .filter(field -> p.matcher(field.getKey()).find())
        .map(
            entry -> {
              Expression nestedFunc =
                  new Function(
                          "nested",
                          List.of(new QualifiedName(List.of(entry.getKey().split("\\.")))))
                      .accept(expressionAnalyzer, context);
              return DSL.named("nested(" + entry.getKey() + ")", nestedFunc);
            })
        .collect(Collectors.toList());
  }
}
