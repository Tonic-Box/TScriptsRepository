package net.runelite.client.plugins.tscripts.adapter;

import net.runelite.client.plugins.tscripts.adapter.models.Scope.Scope;
import net.runelite.client.plugins.tscripts.adapter.models.condition.Comparator;
import net.runelite.client.plugins.tscripts.adapter.models.condition.Condition;
import net.runelite.client.plugins.tscripts.adapter.models.condition.ConditionType;
import net.runelite.client.plugins.tscripts.adapter.models.condition.Conditions;
import net.runelite.client.plugins.tscripts.adapter.models.condition.ForCondition;
import net.runelite.client.plugins.tscripts.adapter.models.condition.Glue;
import net.runelite.client.plugins.tscripts.adapter.lexer.TScriptLexer;
import net.runelite.client.plugins.tscripts.adapter.lexer.TScriptParser;
import net.runelite.client.plugins.tscripts.adapter.models.method.MethodCall;
import net.runelite.client.plugins.tscripts.adapter.models.Element;
import net.runelite.client.plugins.tscripts.adapter.models.ternary.TernaryExpression;
import net.runelite.client.plugins.tscripts.adapter.models.variable.ArrayAccess;
import net.runelite.client.plugins.tscripts.adapter.models.variable.AssignmentType;
import net.runelite.client.plugins.tscripts.adapter.models.variable.VariableAssignment;
import net.runelite.client.plugins.tscripts.util.Logging;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Adapter
{
    public static Scope parse(String script)
    {
        CharStream input = new ANTLRInputStream(script);
        TScriptLexer lexer = new TScriptLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TScriptParser parser = new TScriptParser(tokens);
        TScriptParser.ScriptContext tree = parser.script();
        return new Scope(flushBlock(tree.children), null);
    }

    private static Map<Integer, net.runelite.client.plugins.tscripts.adapter.models.Element> flushBlock(List<ParseTree> ctx)
    {
        Map<Integer, net.runelite.client.plugins.tscripts.adapter.models.Element> elements = new HashMap<>();
        int elementIndex = 0;

        for(ParseTree child : ctx)
        {
            if(!(child instanceof TScriptParser.StatementContext))
            {
                continue;
            }

            ParseTree tree = child.getChild(0);
            net.runelite.client.plugins.tscripts.adapter.models.Element element = null;

            if(tree instanceof TScriptParser.ScopeStatementContext)
            {
                element = flushScope((TScriptParser.ScopeStatementContext) tree);
            }
            else if(tree instanceof TScriptParser.VariableDeclarationContext)
            {
                element = flushVariableDeclaration((TScriptParser.VariableDeclarationContext) tree);
            }
            else if(tree instanceof TScriptParser.FunctionDefinitionContext)
            {
                element = flushFunctionDefinition((TScriptParser.FunctionDefinitionContext) tree);
            }
            else if(tree instanceof TScriptParser.SubscriberDefinitionContext)
            {
                element = flushSubscriberDefinition((TScriptParser.SubscriberDefinitionContext) tree);
            }
            else if(tree instanceof TScriptParser.ArrayDeclarationContext)
            {
                element = flushArrayDeclaration((TScriptParser.ArrayDeclarationContext) tree);
            }
            else if(tree instanceof TScriptParser.FunctionCallContext)
            {
                element = flushFunctionCall((TScriptParser.FunctionCallContext) tree, false);
            }
            else if(tree instanceof TScriptParser.BlockContext)
            {
                TScriptParser.BlockContext block = (TScriptParser.BlockContext) tree;
                element = new Scope(flushBlock(block.children), null);
            }

            if(element != null)
            {
                elements.put(elementIndex++, element);
                continue;
            }

            Logging.errorLog(new RuntimeException("Unknown tree type: " + tree.getClass().getSimpleName()));
        }
        return elements;
    }

    private static VariableAssignment flushArrayDeclaration(TScriptParser.ArrayDeclarationContext ctx)
    {
        String name = "$" + ctx.array().ID().getText();
        Object idx = null;
        AssignmentType type = AssignmentType.of(ctx.assignmentOperator().getText());
        if(ctx.array().expression() != null)
        {
            idx = flushExpression(ctx.array().expression());
        }
        ArrayAccess arrayAccess = new ArrayAccess(name, idx, false);

        List<Object> values = new ArrayList<>();
        values.add(flushExpression(ctx.expression()));

        return new VariableAssignment(arrayAccess, values, type);
    }

    private static net.runelite.client.plugins.tscripts.adapter.models.Element flushSubscriberDefinition(TScriptParser.SubscriberDefinitionContext ctx)
    {
        Conditions conditions = new Conditions();
        conditions.setType(ConditionType.SUBSCRIBE);
        conditions.setUserFunctionName(ctx.ID().getText());

        Condition condition = new Condition(ctx.array().ID().getText(), null, null);
        conditions.getConditions().put(conditions.getConditions().size(), condition);

        Map<Integer, net.runelite.client.plugins.tscripts.adapter.models.Element> elements = flushBlock(ctx.block().children);
        return new Scope(elements, conditions);
    }

    private static Scope flushFunctionDefinition(TScriptParser.FunctionDefinitionContext ctx)
    {
        Conditions conditions = new Conditions();
        conditions.setType(ConditionType.USER_DEFINED_FUNCTION);
        conditions.setUserFunctionName(ctx.ID().getText());
        if(ctx.params() != null)
        {
            for(var arg : ctx.params().variable())
            {
                Condition condition = new Condition("$" + arg.ID().getText(), null, null);
                conditions.getConditions().put(conditions.getConditions().size(), condition);
            }
        }
        Map<Integer, net.runelite.client.plugins.tscripts.adapter.models.Element> elements = flushBlock(ctx.block().children);
        return new Scope(elements, conditions);
    }

    private static VariableAssignment flushVariableDeclaration(TScriptParser.VariableDeclarationContext ctx)
    {
        String name = "$" + ctx.variable().ID().getText();
        AssignmentType type = AssignmentType.of(ctx.assignmentOperator().getText());
        List<Object> values = new ArrayList<>();
        if(ctx.expression() != null)
        {
            values.add(flushExpression(ctx.expression()));
        }

        return new VariableAssignment(name, values, type);
    }

    private static Scope flushScope(TScriptParser.ScopeStatementContext ctx)
    {
        ParseTree context = ctx.getChild(0);
        if(context instanceof TScriptParser.IfStatementContext)
        {
            return flushIfStatement((TScriptParser.IfStatementContext) context);
        }
        else if(context instanceof TScriptParser.WhileStatementContext)
        {
            return flushWhileStatement((TScriptParser.WhileStatementContext) context);
        }
        else if(context instanceof TScriptParser.ForStatementContext)
        {
            return flushForStatement((TScriptParser.ForStatementContext) context);
        }

        Logging.errorLog(new RuntimeException("Unknown scope type: " + context.getClass().getSimpleName()));
        return null;
    }

    private static Scope flushForStatement(TScriptParser.ForStatementContext ctx)
    {
        Conditions conditions = new Conditions();
        conditions.setType(ConditionType.FOR);
        conditions.getConditions().put(0, flushCondition(ctx.condition()));

        ForCondition forCondition = new ForCondition();
        forCondition.setVariableAssignment(flushVariableDeclaration(ctx.variableDeclaration().get(0)));
        forCondition.setOperation(flushVariableDeclaration(ctx.variableDeclaration().get(1)));
        conditions.setForCondition(forCondition);

        Map<Integer, net.runelite.client.plugins.tscripts.adapter.models.Element> elements = flushBlock(ctx.block().children);
        return new Scope(elements, conditions);
    }

    private static Scope flushWhileStatement(TScriptParser.WhileStatementContext ctx)
    {
        Conditions conditions = flushConditions(ctx.condition(), ctx.glue(), ConditionType.WHILE);
        Map<Integer, net.runelite.client.plugins.tscripts.adapter.models.Element> elements = flushBlock(ctx.block().children);
        return new Scope(elements, conditions);
    }

    private static Scope flushIfStatement(TScriptParser.IfStatementContext ctx)
    {
        Conditions conditions = flushConditions(ctx.condition(), ctx.glue(), ConditionType.IF);
        Map<Integer, Element> elements = flushBlock(ctx.block().get(0).children);
        Scope scope = new Scope(elements, conditions);
        if(ctx.block().size() > 1)
        {
            scope.setElseElements(flushBlock(ctx.block().get(1).children));
        }
        return scope;
    }

    private static Conditions flushConditions(List<TScriptParser.ConditionContext> conditionsCtx, List<TScriptParser.GlueContext> gluesCtx, ConditionType type)
    {
        Conditions conditions = new Conditions();
        conditions.setType(type);
        for(TScriptParser.ConditionContext condition : conditionsCtx)
        {
            conditions.getConditions().put(conditions.getConditions().size(), flushCondition(condition));
        }
        for(TScriptParser.GlueContext glue : gluesCtx)
        {
            conditions.getGlues().put(conditions.getGlues().size(), Glue.of(glue.getText()));
        }
        return conditions;
    }

    private static Condition flushCondition(TScriptParser.ConditionContext ctx)
    {
        Comparator comparator = null;
        if(ctx.comparator() != null)
        {
            comparator = Comparator.of(ctx.comparator().getText());
        }

        Object left = null;
        Object right = null;
        for(TScriptParser.ExpressionContext child : ctx.expression())
        {
            if(left == null)
            {
                left = flushExpression(child);
            }
            else if(right == null)
            {
                right = flushExpression(child);
            }
        }
        return new Condition(left, right, comparator);
    }

    private static Object flushExpression(TScriptParser.ExpressionContext ctx)
    {
        boolean negated = false;
        boolean negative = false;
        String pre = "";
        for(ParseTree child : ctx.children)
        {
            if(child instanceof TScriptParser.UnaryOperatorContext)
            {
                if(child.getText().equals("!"))
                {
                    negated = true;
                    pre = "!";
                }
                else if(child.getText().equals("-"))
                {
                    negative = true;
                    pre = "-";
                }
            }
            else if(child.getText().equals("null"))
            {
                return "null";
            }
            else if(child instanceof TScriptParser.VariableContext)
            {
                return pre + "$" + ((TScriptParser.VariableContext) child).ID().getText();
            }
            else if(child instanceof TScriptParser.ArrayContext)
            {
                return flushArrayAccess((TScriptParser.ArrayContext) child, negated);
            }
            else if(child instanceof TScriptParser.FunctionCallContext)
            {
                return flushFunctionCall((TScriptParser.FunctionCallContext) child, negated);
            }
            else if(child instanceof TScriptParser.TernaryExpressionContext)
            {
                return flushTernaryExpression((TScriptParser.TernaryExpressionContext) child);
            }
        }

        if(ctx.NUMBER() != null)
        {
            return negative ? -Integer.parseInt(ctx.NUMBER().getText()) : Integer.parseInt(ctx.NUMBER().getText());
        }
        else if(ctx.STRING() != null)
        {
            return ctx.STRING().getText().substring(0, ctx.STRING().getText().length() - 1);
        }
        else if(ctx.BOOLEAN() != null)
        {
            return negated != Boolean.parseBoolean(ctx.BOOLEAN().getText());
        }
        else if(ctx.CONSTANT() != null)
        {
            return ctx.CONSTANT().getText();
        }
        return null;
    }

    private static MethodCall flushFunctionCall(TScriptParser.FunctionCallContext ctx, boolean negated)
    {
        String name = ctx.ID().getText();
        List<Object> objects = new ArrayList<>();
        if(ctx.arguments() != null)
        {
            for(var arg : ctx.arguments().expression())
            {
                objects.add(flushExpression(arg));
            }
        }
        return new MethodCall(name, objects.toArray(), negated);
    }

    private static ArrayAccess flushArrayAccess(TScriptParser.ArrayContext ctx, boolean negated)
    {
        String name = "$" + ctx.ID().getText();

        for(ParseTree child : ctx.children)
        {
            if(child instanceof TScriptParser.ExpressionContext)
            {
                return new ArrayAccess(name, flushExpression((TScriptParser.ExpressionContext) child), negated);
            }
        }

        return new ArrayAccess(name, null, negated);
    }

    private static TernaryExpression flushTernaryExpression(TScriptParser.TernaryExpressionContext ctx)
    {
        Conditions condition = flushConditions(ctx.condition(), ctx.glue(), ConditionType.TERNARY);
        Object ifTrue = flushExpression(ctx.expression().get(0));
        Object ifFalse = flushExpression(ctx.expression().get(1));
        return new TernaryExpression(condition, ifTrue, ifFalse);
    }
}
