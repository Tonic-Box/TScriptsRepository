package net.runelite.client.plugins.tscripts.runtime;

import lombok.Getter;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.tscripts.api.Api;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.eventbus._Subscribe;
import net.runelite.client.plugins.tscripts.eventbus.events.*;
import net.runelite.client.plugins.tscripts.lexer.MethodCall;
import net.runelite.client.plugins.tscripts.lexer.Scope.Scope;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.Condition;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.ConditionType;
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
        variableMap.clear();
        new Thread(() -> {
            variableMap.clear();
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

    /**
     * Processes a scope.
     *
     * @param scope The scope.
     */
    private void processScope(Scope scope) {
        if (_die) {
            return;
        }

        scope.setCurrent(true);
        postCurrentInstructionChanged();
        boolean isWhileScope = scope.getCondition() != null && scope.getCondition().getType().equals(ConditionType.WHILE);
        boolean isRegisterScope = scope.getCondition() != null && scope.getCondition().getType().equals(ConditionType.REGISTER);
        boolean shouldProcess = scope.getCondition() == null || processCondition(scope.getCondition());
        scope.setCurrent(false);

        if(isRegisterScope)
        {
            Class event = methodManager.getEventClass(scope.getCondition().getLeft().toString());
            if(event != null)
            {
                EventBus.Subscriber subscriber = Api.register(event, (Object object) -> {
                    try
                    {
                        Runtime runtime = new Runtime();
                        Scope eventScope = scope.clone();
                        eventScope.setCondition(null);
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

        while (shouldProcess) {
            if (isScriptInterrupted()) return;

            for (Element element : scope.getElements().values()) {
                if (isScriptInterrupted()) return;
                processElement(element, scope);
                postFlags();
                if (_die || _break || _continue) break;
            }

            if (handleControlFlow(scope)) return;

            // Re-evaluate condition for while loop
            shouldProcess = isWhileScope && processCondition(scope.getCondition());
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
            default:
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
        Object value = getValue(variableAssignment.getValues().get(0));
        String name = variableAssignment.getVar();

        switch (variableAssignment.getAssignmentType())
        {
            case ASSIGNMENT:
                variableMap.put(name, value);
                break;
            case INCREMENT:
                incrementVariable(name, value);
                break;
            case DECREMENT :
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
        return scope.getCondition() == null || !scope.getCondition().getType().equals(ConditionType.WHILE);
    }

    /**
     * Checks if the script should continue.
     *
     * @param scope The scope.
     * @return Whether the script should continue.
     */
    private boolean shouldContinue(Scope scope) {
        return scope.getCondition() != null && scope.getCondition().getType().equals(ConditionType.WHILE);
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
            return methodManager.call(processMethodCallArguments((MethodCall) object));
        }
        else if (typeOfAny(object, Integer.class, Boolean.class))
        {
            return object;
        }

        return null;
    }

    /**
     * Checks if the object is an instance of any of the classes.
     *
     * @param test The object to test.
     * @param against The classes to test against.
     * @return Whether the object is an instance of any of the classes.
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