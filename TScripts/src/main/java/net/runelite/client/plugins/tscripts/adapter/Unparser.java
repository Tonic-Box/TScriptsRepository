package net.runelite.client.plugins.tscripts.adapter;

import net.runelite.client.plugins.tscripts.adapter.lexer.TScriptBaseVisitor;
import net.runelite.client.plugins.tscripts.adapter.lexer.TScriptParser;
import org.antlr.v4.runtime.tree.ParseTree;

public class Unparser extends TScriptBaseVisitor<String>
{
    private final static Unparser instance = new Unparser();

    public static String revert(ParseTree ctx) {
        return instance.visit(ctx);
    }

    @Override
    public String visitScript(TScriptParser.ScriptContext ctx) {
        StringBuilder builder = new StringBuilder();
        for (TScriptParser.StatementContext statement : ctx.statement()) {
            builder.append(visit(statement));
        }
        return builder.toString();
    }

    @Override
    public String visitStatement(TScriptParser.StatementContext ctx) {
        if (ctx.functionDefinition() != null) {
            return visitFunctionDefinition(ctx.functionDefinition());
        } else if (ctx.subscriberDefinition() != null) {
            return visitSubscriberDefinition(ctx.subscriberDefinition());
        } else if (ctx.ipcPost() != null) {
            return visitIpcPost(ctx.ipcPost());
        } else if (ctx.variableDeclaration() != null) {
            return visitVariableDeclaration(ctx.variableDeclaration()) + ";\n";
        } else if (ctx.arrayDeclaration() != null) {
            return visitArrayDeclaration(ctx.arrayDeclaration()) + ";\n";
        } else if (ctx.functionCall() != null) {
            return visitFunctionCall(ctx.functionCall()) + ";\n";
        } else if (ctx.refferanceFunctionCall() != null) {
            return visitRefferanceFunctionCall(ctx.refferanceFunctionCall()) + ";\n";
        } else if (ctx.scopeStatement() != null) {
            return visitScopeStatement(ctx.scopeStatement());
        } else if (ctx.block() != null) {
            return visitBlock(ctx.block());
        } else {
            return ";\n";
        }
    }

    @Override
    public String visitBlock(TScriptParser.BlockContext ctx) {
        StringBuilder builder = new StringBuilder("{\n");
        for (TScriptParser.StatementContext statement : ctx.statement()) {
            builder.append(visit(statement));
        }
        builder.append("}\n");
        return builder.toString();
    }

    @Override
    public String visitIfStatement(TScriptParser.IfStatementContext ctx) {
        StringBuilder builder = new StringBuilder("if (");
        builder.append(visitCondition(ctx.condition(0))); // Assuming multiple conditions are possible
        for (int i = 1; i < ctx.condition().size(); i++) {
            builder.append(" && ");
            builder.append(visitCondition(ctx.condition(i)));
        }
        builder.append(") ");
        builder.append(visitBlock(ctx.block(0)));
        if (ctx.block().size() > 1) {
            builder.append("else ");
            builder.append(visitBlock(ctx.block(1)));
        }
        return builder.toString();
    }

    @Override
    public String visitVariableDeclaration(TScriptParser.VariableDeclarationContext ctx) {
        return visitVariable(ctx.variable()) + " " + ctx.assignmentOperator().getText() + " " + visit(ctx.expression());
    }

    @Override
    public String visitArrayDeclaration(TScriptParser.ArrayDeclarationContext ctx) {
        return visitArray(ctx.array()) + " " + ctx.assignmentOperator().getText() + " " + visit(ctx.expression());
    }

    @Override
    public String visitFunctionCall(TScriptParser.FunctionCallContext ctx) {
        StringBuilder builder = new StringBuilder(ctx.ID().getText());
        if (ctx.arguments() != null) {
            builder.append("(");
            builder.append(visitArguments(ctx.arguments()));
            builder.append(")");
        }
        return builder.toString();
    }

    @Override
    public String visitRefferanceFunctionCall(TScriptParser.RefferanceFunctionCallContext ctx) {
        return visitVariable(ctx.variable()) + " " + visitArguments(ctx.arguments());
    }

    @Override
    public String visitVariable(TScriptParser.VariableContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitArray(TScriptParser.ArrayContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitArguments(TScriptParser.ArgumentsContext ctx) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ctx.expression().size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append(visitExpression(ctx.expression(i)));
        }
        return builder.toString();
    }

    @Override
    public String visitExpression(TScriptParser.ExpressionContext ctx) {
        if (ctx.operationExpression() != null) {
            return visitOperationExpression(ctx.operationExpression());
        } else if (ctx.NUMBER() != null) {
            return ctx.NUMBER().getText();
        } else if (ctx.STRING() != null) {
            return ctx.STRING().getText();
        } else {
            return ""; // Handle other cases similarly
        }
    }

    @Override
    public String visitOperationExpression(TScriptParser.OperationExpressionContext ctx) {
        return "(" + visitExpression(ctx.expression(0)) + " " + ctx.opperationOperator().getText() + " " + visitExpression(ctx.expression(1)) + ")";
    }

    @Override
    public String visitScopeStatement(TScriptParser.ScopeStatementContext ctx) {
        if (ctx.ifStatement() != null) {
            return visitIfStatement(ctx.ifStatement());
        } else if (ctx.whileStatement() != null) {
            return visitWhileStatement(ctx.whileStatement());
        } else if (ctx.forStatement() != null) {
            return visitForStatement(ctx.forStatement());
        }
        return "";
    }

    @Override
    public String visitWhileStatement(TScriptParser.WhileStatementContext ctx) {
        return "while (" + visitCondition(ctx.condition(0)) + ") " + visitBlock(ctx.block());
    }

    @Override
    public String visitForStatement(TScriptParser.ForStatementContext ctx) {
        return "for (" + visitVariableDeclaration(ctx.variableDeclaration(0)) + "; " +
                visitCondition(ctx.condition()) + "; " +
                visitVariableDeclaration(ctx.variableDeclaration(1)) + ") " +
                visitBlock(ctx.block());
    }

    @Override
    public String visitIpcPost(TScriptParser.IpcPostContext ctx) {
        String expr = ctx.expression() != null ? visit(ctx.expression()) : "";
        return "post(" + expr + ") " + visitBlock(ctx.block());
    }

    @Override
    public String visitFunctionDefinition(TScriptParser.FunctionDefinitionContext ctx) {
        StringBuilder builder = new StringBuilder("function ");
        builder.append(ctx.ID().getText());
        builder.append("(");
        if (ctx.params() != null) {
            builder.append(visitParams(ctx.params()));
        }
        builder.append(") ");
        builder.append(visitBlock(ctx.block()));
        return builder.toString();
    }

    @Override
    public String visitSubscriberDefinition(TScriptParser.SubscriberDefinitionContext ctx) {
        StringBuilder builder = new StringBuilder("subscribe ");
        builder.append(ctx.ID().getText());
        builder.append("(");
        if (ctx.array() != null) {
            builder.append(visitArray(ctx.array()));
        } else if (ctx.variable() != null) {
            builder.append(visitVariable(ctx.variable()));
        }
        builder.append(") ");
        builder.append(visitBlock(ctx.block()));
        return builder.toString();
    }

    @Override
    public String visitCondition(TScriptParser.ConditionContext ctx) {
        if (ctx.expression().size() > 1) {
            return visitExpression(ctx.expression(0)) + " " + ctx.comparator().getText() + " " + visitExpression(ctx.expression(1));
        } else {
            return visitExpression(ctx.expression(0));
        }
    }

    @Override
    public String visitParams(TScriptParser.ParamsContext ctx) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (i > 0) builder.append(", ");
            if (ctx.variable(i) != null) {
                builder.append(visitVariable(ctx.variable(i)));
            } else if (ctx.array(i) != null) {
                builder.append(visitArray(ctx.array(i)));
            }
        }
        return builder.toString();
    }

    @Override
    public String visitUnaryOperator(TScriptParser.UnaryOperatorContext ctx) {
        return ctx.getText();
    }
}