// Generated from TScript.g4 by ANTLR 4.13.1

  package net.runelite.client.plugins.tscripts.adapter.lexer;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TScriptParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TScriptVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TScriptParser#script}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScript(TScriptParser.ScriptContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(TScriptParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#scopeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScopeStatement(TScriptParser.ScopeStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(TScriptParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#ipcPost}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIpcPost(TScriptParser.IpcPostContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(TScriptParser.WhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#forStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatement(TScriptParser.ForStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition(TScriptParser.ConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(TScriptParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#textBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTextBlock(TScriptParser.TextBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#functionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDefinition(TScriptParser.FunctionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#subscriberDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubscriberDefinition(TScriptParser.SubscriberDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#variableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaration(TScriptParser.VariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#arrayDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayDeclaration(TScriptParser.ArrayDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(TScriptParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#array}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray(TScriptParser.ArrayContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#comparator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparator(TScriptParser.ComparatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#glue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGlue(TScriptParser.GlueContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#unaryOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryOperator(TScriptParser.UnaryOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#assignmentOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentOperator(TScriptParser.AssignmentOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#opperationOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpperationOperator(TScriptParser.OpperationOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(TScriptParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#operationExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperationExpression(TScriptParser.OperationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#shorthandExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShorthandExpression(TScriptParser.ShorthandExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#ternaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTernaryExpression(TScriptParser.TernaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#nullCoalescingExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullCoalescingExpression(TScriptParser.NullCoalescingExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#nullCheck}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullCheck(TScriptParser.NullCheckContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(TScriptParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#refferanceFunctionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefferanceFunctionCall(TScriptParser.RefferanceFunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#params}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParams(TScriptParser.ParamsContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#arguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArguments(TScriptParser.ArgumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link TScriptParser#lambda}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambda(TScriptParser.LambdaContext ctx);
}