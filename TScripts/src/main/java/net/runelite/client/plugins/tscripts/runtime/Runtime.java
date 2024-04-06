package net.runelite.client.plugins.tscripts.runtime;

import lombok.Getter;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TDelay;
import net.runelite.client.plugins.tscripts.api.library.TGame;
import net.runelite.client.plugins.tscripts.lexer.MethodCall;
import net.runelite.client.plugins.tscripts.lexer.Scope.Scope;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.Condition;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.ConditionType;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.Conditions;
import net.runelite.client.plugins.tscripts.lexer.Scope.condition.Glue;
import net.runelite.client.plugins.tscripts.lexer.models.Element;
import net.runelite.client.plugins.tscripts.lexer.variable.VariableAssignment;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.runelite.client.plugins.tscripts.util.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.util.eventbus._Subscribe;
import net.runelite.client.plugins.tscripts.util.eventbus.events.*;

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
    private UserDefinedFunction currentFunction = null;
    @Getter
    private Scope rootScope = new Scope(new HashMap<>(), null);
    @Getter
    private String scriptName = "", profile = "";
    private boolean _die = false, _break = false, _continue = false, _done = true, _return = false, breakpointTripped = false;

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
    public void execute(Scope scope, String scriptName, String profile)
    {
        this.rootScope = scope;
        this._done = false;
        this._die = false;
        this._break = false;
        this._continue = false;
        this._return = false;
        this.scriptName = scriptName;
        this.profile = profile;
        this.breakpointTripped = false;
        this.userDefinedFunctions.clear();
        this.variableMap.clear();
        new Thread(() -> {
            postFlags();
            try
            {
                TEventBus.post(new ScriptStateChanged(scriptName, profile, true));
                processScope(scope);
            }
            catch (Exception ex)
            {
                Logging.errorLog(ex);
            }
            TGame.unregister(subscribers);
            _done = true;
            TEventBus.post(new ScriptStateChanged(scriptName, profile, false));
            postFlags();
        }).start();
    }

    /**
     * Processes a scope.
     *
     * @param scope The scope.
     */
    private void processScope(Scope scope) {
        if(_die || _return) return;

        scope.setCurrent(true);
        postCurrentInstructionChanged();

        boolean isRegisterScope = scope.getConditions() != null && scope.getConditions().getType() != null  && scope.getConditions().getType().equals(ConditionType.SUBSCRIBE);
        if(isRegisterScope)
        {
            addAnonymousEventSubscriber(scope);
            scope.setCurrent(false);
            return;
        }

        boolean isUserDefinedFunction = scope.getConditions() != null && scope.getConditions().getType() != null  && scope.getConditions().getType().equals(ConditionType.USER_DEFINED_FUNCTION);
        if(isUserDefinedFunction)
        {
            addUserDefinedFunction(scope);
            scope.setCurrent(false);
            return;
        }

        boolean isWhileScope = scope.getConditions() != null && scope.getConditions().getType() != null && scope.getConditions().getType().equals(ConditionType.WHILE);
        boolean shouldProcess = (scope.getConditions() == null || scope.getConditions().getType() == null) || processConditions(scope.getConditions());
        scope.setCurrent(false);

        while (shouldProcess)
        {
            for (Element element : scope.getElements().values()) {
                processElement(element);
                postFlags();
                if (_die || _break || _continue || _return) break;
            }

            if (handleControlFlow(isWhileScope)) break;

            shouldProcess = isWhileScope && processConditions(scope.getConditions());
        }
    }

    /**
     * Processes an element.
     *
     * @param element The element.
     */
    private void processElement(Element element) {
        switch (element.getType()) {
            case SCOPE:
                processScope((Scope) element);
                break;
            case FUNCTION_CALL:
                MethodCall methodCall = (MethodCall) element;
                methodCall.setCurrent(true);
                postCurrentInstructionChanged();
                processFunctionCall(methodCall);
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
     */
    private void processFunctionCall(MethodCall call) {
        switch (call.getName()) {
            case "die":
                _die = true;
                break;
            case "break":
                _break = true;
                break;
            case "continue":
                _continue = true;
                break;
            case "breakpoint":
                TEventBus.post(BreakpointTripped.get());
                breakpointTripped = true;
                postFlags();
                while (breakpointTripped)
                {
                    if(_done || _die) break;
                    TDelay.sleep(100);
                }
                break;
            case "return":
                if(currentFunction != null)
                {
                    if(call.getArgs().length > 0)
                        currentFunction.setReturnValue(getValue(call.getArgs()[0]));
                    else
                        currentFunction.setReturnValue("null");
                    _return = true;
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

    /**
     * Processes conditions.
     * @param conditions The conditions.
     * @return Whether the conditions are true.
     */
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

        if(right == null)
        {
            if(left instanceof Boolean)
            {
                return (Boolean) left;
            }
            return !left.toString().equals("null");
        }

        return condition.getComparator() != null && condition.getComparator().process(left, right);
    }

    /**
     * Processes a user-defined function.
     * @param function The function.
     * @return The output of the function.
     */
    private Object processUserFunction(UserDefinedFunction function)
    {
        currentFunction = function;
        processScope(function.getScope());
        Object output = function.getReturnValue() == null ? "null" : function.getReturnValue();
        function.setReturnValue(null);
        currentFunction = null;
        _return = false;
        return output;
    }

    /**
     * Handles control flow.
     *
     * @return Whether the control flow was handled.
     */
    private boolean handleControlFlow(boolean isWhileScope) {
        if (_die || _return) return true;

        if (isWhileScope) {
            if (_break) {
                _break = false;
                return true;
            }
            else if (_continue) {
                _continue = false;
                return false;
            }
        }

        return _break || _continue;
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
                if(!variableMap.containsKey(string))
                {
                    return "null";
                }
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
     * Adds a user-defined function.
     *
     * @param scope The scope.
     */
    private void addUserDefinedFunction(Scope scope)
    {
        String name = scope.getConditions().getConditions().get(0).getLeft().toString().replace("\"", "");
        Scope userDefinedFunction = scope.clone();
        userDefinedFunction.setConditions(null);
        userDefinedFunctions.put(name, new UserDefinedFunction(name, userDefinedFunction));
    }

    /**
     * Adds an anonymous event subscriber.
     *
     * @param scope The scope.
     */
    private void addAnonymousEventSubscriber(Scope scope)
    {
        Class<?> event = methodManager.getEventClass(scope.getConditions().getConditions().get(0).getLeft().toString());
        if(event != null)
        {
            EventBus.Subscriber subscriber = TGame.register(event, object -> {
                try
                {
                    Runtime runtime = new Runtime();
                    Scope eventScope = scope.clone();
                    eventScope.setConditions(null);
                    new Thread(() -> runtime.execute(eventScope, "TS_EVENT", "TS_EVENT")).start();
                }
                catch (Exception ex)
                {
                    Logging.errorLog(ex);
                }
            });
            subscribers.add(subscriber);
        }
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

    //********** EVENT STUFF **********//

    /**
     * Posts the flags.
     */
    private void postFlags()
    {
        Map<String,Object> flags = new HashMap<>();
        flags.put("scriptName", scriptName);
        flags.put("profile", profile);
        flags.put("running", !_done);
        flags.put("subscribers", subscribers.size());
        flags.put("variables", variableMap.getVariableMap().size());
        flags.put("done", _done);
        flags.put("die", _die);
        flags.put("break", _break);
        flags.put("continue", _continue);
        flags.put("return", _return);
        flags.put("breakpointTripped", breakpointTripped);
        flags.put("userDefinedFunctions", userDefinedFunctions.size());
        TEventBus.post(new FlagChanged(flags));
    }

    /**
     * Posts the current instruction changed event.
     */
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