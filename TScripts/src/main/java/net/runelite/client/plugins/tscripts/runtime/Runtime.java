package net.runelite.client.plugins.tscripts.runtime;

import lombok.Getter;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.tscripts.api.Api;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.eventbus.*;
import net.runelite.client.plugins.tscripts.eventbus.events.*;
import net.runelite.client.plugins.tscripts.lexer.MethodCall;
import net.runelite.client.plugins.tscripts.lexer.Scope.Scope;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.Condition;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.ConditionType;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.Conditions;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.Glue;
import net.runelite.client.plugins.tscripts.lexer.models.Element;
import net.runelite.client.plugins.tscripts.lexer.variable.VariableAssignment;
import net.runelite.client.plugins.tscripts.util.Logging;
import java.util.*;

/**
 * Responsible for executing scripts.
 */
public class Runtime
{
    @Getter
    private final VariableMap variableMap = new VariableMap();
    private final List<EventBus.Subscriber> subscribers = new ArrayList<>();
    private final Map<String, UserDefinedFunction> userDefinedFunctions = new HashMap<>();
    @Getter
    private final MethodManager methodManager;
    private boolean _die = false;
    private boolean _break = false;
    private boolean _continue = false;
    private boolean _done = true;
    @Getter
    private String scriptName = "";
    @Getter
    private Scope rootScope = new Scope(new HashMap<>(), null);
    private boolean breakpointTripped = false;
    private UserDefinedFunction currentFunction = null;

    /**
     * Creates a new instance of the Runtime class.
     */
    public Runtime()
    {
        this.methodManager = MethodManager.getInstance();
        TEventBus.register(this);
    }

    /**
     * Executes the script.
     *
     * @param scope The scope.
     * @param scriptName The script name.
     */
    public void execute(Scope scope, String scriptName)
    {
        this.rootScope = scope;
        this._done = false;
        this._die = false;
        this._break = false;
        this._continue = false;
        this.scriptName = scriptName;
        this.breakpointTripped = false;
        this.userDefinedFunctions.clear();
        this.variableMap.clear();
        new Thread(() -> {
            postFlags();
            try
            {
                TEventBus.post(new ScriptStateChanged(scriptName, true));
                processScope(scope);
                TEventBus.post(new ScriptStateChanged(scriptName, false));
            }
            catch (Exception ex)
            {
                Logging.errorLog(ex);
            }
            Api.unregister(subscribers);
            setDone(true);
            postFlags();
        }).start();
    }

    private Object processUserFunction(UserDefinedFunction function)
    {
        currentFunction = function;
        processScope(function.getScope());
        Object output = function.getReturnValue() == null ? "null" : function.getReturnValue();
        function.setReturnValue(null);
        currentFunction = null;
        return output;
    }

    /**
     * Processes a scope.
     *
     * @param scope The scope.
     */
    private void processScope(Scope scope) {
        if (_die) {
            return;
        }

        if(currentFunction != null && currentFunction.hasReturnValue())
        {
            return;
        }

        scope.setCurrent(true);
        postCurrentInstructionChanged();
        boolean isWhileScope = scope.getConditions() != null && scope.getConditions().getType() != null && scope.getConditions().getType().equals(ConditionType.WHILE);
        boolean isRegisterScope = scope.getConditions() != null && scope.getConditions().getType() != null  && scope.getConditions().getType().equals(ConditionType.REGISTER);
        boolean isUserDefinedFunction = scope.getConditions() != null && scope.getConditions().getType() != null  && scope.getConditions().getType().equals(ConditionType.USER_DEFINED_FUNCTION);
        boolean shouldProcess = (scope.getConditions() == null || scope.getConditions().getType() == null) || processConditions(scope.getConditions());
        scope.setCurrent(false);

        if(isRegisterScope)
        {
            Class<?> event = methodManager.getEventClass(scope.getConditions().getConditions().get(0).getLeft().toString());
            if(event != null)
            {
                EventBus.Subscriber subscriber = Api.register(event, object -> {
                    try
                    {
                        Runtime runtime = new Runtime();
                        Scope eventScope = scope.clone();
                        eventScope.setConditions(null);
                        new Thread(() -> runtime.execute(eventScope, "TS_EVENT")).start();
                    }
                    catch (Exception ex)
                    {
                        Logging.errorLog(ex);
                    }
                });
                subscribers.add(subscriber);
            }
            return;
        }
        else if(isUserDefinedFunction)
        {
            String name = scope.getConditions().getConditions().get(0).getLeft().toString().replace("\"", "");
            Scope userDefinedFunction = scope.clone();
            userDefinedFunction.setConditions(null);
            userDefinedFunctions.put(name, new UserDefinedFunction(name, userDefinedFunction));
            return;
        }

        while (shouldProcess) {
            if (isScriptInterrupted()) return;

            for (Element element : scope.getElements().values()) {
                if (isScriptInterrupted()) return;
                processElement(element, scope.clone());
                postFlags();
                if (_die || _break || _continue) break;
                if(currentFunction != null && currentFunction.hasReturnValue())
                {
                    return;
                }
            }

            if (handleControlFlow(scope)) return;

            // Re-evaluate condition for while loop
            shouldProcess = isWhileScope && processConditions(scope.getConditions());
        }
    }

    /**
     * Processes an element.
     *
     * @param element The element.
     * @param scope The scope.
     */
    private void processElement(Element element, Scope scope) {
        switch (element.getType()) {
            case SCOPE:
                processScope((Scope) element);
                break;
            case FUNCTION_CALL:
                MethodCall methodCall = (MethodCall) element;
                methodCall.setCurrent(true);
                postCurrentInstructionChanged();
                processFunctionCall(methodCall, scope);
                methodCall.setCurrent(false);
                break;
            case VARIABLE_ASSIGNMENT:
                VariableAssignment assignment = (VariableAssignment) element;
                assignment.setCurrent(true);
                postCurrentInstructionChanged();
                processVariableAssignment(assignment);
                assignment.setCurrent(false);
                break;
        }
    }

    /**
     * Processes a function call.
     *
     * @param call The method call.
     * @param scope The scope.
     */
    private void processFunctionCall(MethodCall call, Scope scope) {
        switch (call.getName()) {
            case "die":
                _die = true;
                break;
            case "break":
                _break = shouldBreak(scope);
                break;
            case "continue":
                _continue = shouldContinue(scope);
                break;
            case "breakpoint":
                TEventBus.post(BreakpointTripped.get());
                breakpointTripped = true;
                postFlags();
                while (breakpointTripped)
                {
                    if(isScriptInterrupted())
                    {
                        break;
                    }
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException ex)
                    {
                        Logging.errorLog(ex);
                    }
                }
                break;
            case "return":
                if(currentFunction != null)
                {
                    if(call.getArgs().length > 0)
                        currentFunction.setReturnValue(getValue(call.getArgs()[0]));
                    else
                        currentFunction.setReturnValue("null");
                }
                break;
            default:
                if(userDefinedFunctions.containsKey(call.getName()))
                {
                    processUserFunction(userDefinedFunctions.get(call.getName()));
                    break;
                }
                methodManager.call(processMethodCallArguments(call));
                break;
        }
    }

    /**
     * Processes a variable assignment.
     *
     * @param variableAssignment The variable assignment.
     */
    private void processVariableAssignment(VariableAssignment variableAssignment) {
        String name = variableAssignment.getVar();
        switch (variableAssignment.getAssignmentType())
        {
            case ADD_ONE:
                incrementVariable(name, 1);
                return;
            case REMOVE_ONE:
                decrementVariable(name, 1);
                return;
        }

        Object value = getValue(variableAssignment.getValues().get(0));

        switch (variableAssignment.getAssignmentType())
        {
            case ASSIGNMENT:
                variableMap.put(name, value);
                return;
            case INCREMENT:
                incrementVariable(name, value);
                return;
            case DECREMENT:
                decrementVariable(name, value);
                break;
        }
    }

    /**
     * Processes the arguments in a method call.
     *
     * @param methodCall The method call.
     * @return The processed method call.
     */
    private MethodCall processMethodCallArguments(MethodCall methodCall) {
        Object[] objects = Arrays.stream(methodCall.getArgs())
                .map(this::getValue)
                .toArray();
        return new MethodCall(methodCall.getName(), objects, methodCall.isNegate());
    }

    private boolean processConditions(Conditions conditions) {
        boolean result = true;
        for (Map.Entry<Integer, Condition> entry : conditions.getConditions().entrySet())
        {
            Condition condition = entry.getValue();
            boolean conditionResult = processCondition(condition);
            int key = entry.getKey();
            if(key == 0)
            {
                result = conditionResult;
                continue;
            }

            if(!conditions.getGlues().containsKey(key - 1))
                continue;

            Glue glue = conditions.getGlues().get(key - 1);
            switch (glue)
            {
                case AND:
                    result = result && conditionResult;
                    break;
                case OR:
                    result = result || conditionResult;
                    break;
            }
        }
        return result;
    }

    /**
     * Processes a condition.
     *
     * @param condition The condition.
     * @return Whether the condition is true.
     */
    private boolean processCondition(Condition condition) {
        if(condition == null)
        {
            return true;
        }

        Object left = getValue(condition.getLeft());
        if (left == null)
        {
            return true;
        }

        Object right = getValue(condition.getRight());

        return right == null ?
                left instanceof Boolean && (Boolean) left
                :
                condition.getComparator() != null && condition.getComparator().process(left, right);
    }

    /**
     * Checks if the script is interrupted.
     *
     * @return Whether the script is interrupted.
     */
    private boolean isScriptInterrupted() {
        return _done || _die;
    }

    /**
     * Checks if the script should break.
     *
     * @param scope The scope.
     * @return Whether the script should break.
     */
    private boolean shouldBreak(Scope scope) {
        return scope.getConditions() == null || !scope.getConditions().getType().equals(ConditionType.WHILE);
    }

    /**
     * Checks if the script should continue.
     *
     * @param scope The scope.
     * @return Whether the script should continue.
     */
    private boolean shouldContinue(Scope scope) {
        return scope.getConditions() != null && scope.getConditions().getType().equals(ConditionType.WHILE);
    }

    /**
     * Handles control flow.
     *
     * @param scope The scope.
     * @return Whether the control flow was handled.
     */
    private boolean handleControlFlow(Scope scope) {
        if (_die) return true;
        if (_break) {
            _break = shouldBreak(scope);
            return true;
        }
        if (_continue) {
            _continue = shouldContinue(scope);
            return !_continue;
        }
        if(currentFunction != null && currentFunction.hasReturnValue())
        {
            return true;
        }
        return false;
    }

    /**
     * Increments a variable.
     *
     * @param name The variable name.
     * @param value The value.
     */
    private void incrementVariable(String name, Object value) {
        if (value instanceof Integer) {
            int integer = (int) value;
            int prev = variableMap.containsKey(name) ? (int) variableMap.get(name) : 0;
            variableMap.put(name, prev + integer);
        } else if (value instanceof String) {
            String string = (String) value;
            String prev = variableMap.containsKey(name) ? (String) variableMap.get(name) : "";
            variableMap.put(name, prev + string);
        }
    }

    /**
     * Decrements a variable.
     *
     * @param name The variable name.
     * @param value The value.
     */
    private void decrementVariable(String name, Object value) {
        if (value instanceof Integer) {
            int integer = (int) value;
            int prev = variableMap.containsKey(name) ? (int) variableMap.get(name) : 0;
            variableMap.put(name, prev - integer);
        }
    }

    /**
     * Gets the value of an argument.
     *
     * @param object The object.
     * @return The value.
     */
    private Object getValue(Object object)
    {
        if (object instanceof String)
        {
            String string = (String) object;
            if (string.startsWith("$"))
            {
                return variableMap.get(string);
            }
            else if (string.startsWith("!$"))
            {
                String varName = string.substring(1);
                Object value = variableMap.get(varName);
                return (value instanceof Boolean) ? !((Boolean) value) : value;
            }
            else
            {
                return string.startsWith("\"") ? string.substring(1, string.length() - 1) : string;
            }
        }
        else if (object instanceof MethodCall)
        {
            MethodCall methodCall = (MethodCall) object;
            if(userDefinedFunctions.containsKey(methodCall.getName()))
            {
                return processUserFunction(userDefinedFunctions.get(methodCall.getName()));
            }
            return methodManager.call(processMethodCallArguments((MethodCall) object));
        }
        else if (typeOfAny(object, Integer.class, Boolean.class))
        {
            return object;
        }

        return null;
    }

    /**
     * Checks if the object is an instance of the classes.
     *
     * @param test The object to test.
     * @param against The classes to test against.
     * @return Whether the object is an instance of the classes.
     */
    private boolean typeOfAny(Object test, Class<?>... against)
    {
        for (Class<?> clazz : against) {
            if (clazz.isInstance(test)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kills the script.
     */
    public void killScript()
    {
        _die = true;
        _done = true;
    }

    /**
     * Checks if the script is done.
     *
     * @return Whether the script is done.
     */
    public boolean isDone()
    {
        return _done;
    }

    /**
     * Sets whether the script is done.
     *
     * @param done Whether the script is done.
     */
    public void setDone(boolean done)
    {
        _done = done;
    }

    //********** EVENT STUFF **********//

    private void postFlags()
    {
        Map<String,Object> flags = new HashMap<>();
        flags.put("scriptName", scriptName);
        flags.put("running", !_done);
        flags.put("subscribers", subscribers.size());
        flags.put("variables", variableMap.getVariableMap().size());
        flags.put("done", _done);
        flags.put("die", _die);
        flags.put("break", _break);
        flags.put("continue", _continue);
        flags.put("breakpointTripped", breakpointTripped);
        flags.put("userDefinedFunctions", userDefinedFunctions.size());
        TEventBus.post(new FlagChanged(flags));
    }

    private void postCurrentInstructionChanged()
    {
        TEventBus.post(CurrentInstructionChanged.get());
    }

    @_Subscribe
    public void onBreakpointUnTripped(BreakpointUnTripped event)
    {
        breakpointTripped = false;
    }
}