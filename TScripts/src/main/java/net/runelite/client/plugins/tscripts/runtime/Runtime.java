package net.runelite.client.plugins.tscripts.runtime;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.tscripts.adapter.Adapter;
import net.runelite.client.plugins.tscripts.adapter.models.Expression;
import net.runelite.client.plugins.tscripts.adapter.models.OperatorType;
import net.runelite.client.plugins.tscripts.adapter.models.shorthand.NullCheckExpression;
import net.runelite.client.plugins.tscripts.adapter.models.shorthand.NullCoalescingExpression;
import net.runelite.client.plugins.tscripts.adapter.models.shorthand.TernaryExpression;
import net.runelite.client.plugins.tscripts.api.MethodManager;
import net.runelite.client.plugins.tscripts.api.library.TDelay;
import net.runelite.client.plugins.tscripts.adapter.models.method.MethodCall;
import net.runelite.client.plugins.tscripts.adapter.models.Scope.Scope;
import net.runelite.client.plugins.tscripts.adapter.models.condition.Condition;
import net.runelite.client.plugins.tscripts.adapter.models.condition.ConditionType;
import net.runelite.client.plugins.tscripts.adapter.models.condition.Conditions;
import net.runelite.client.plugins.tscripts.adapter.models.condition.Glue;
import net.runelite.client.plugins.tscripts.adapter.models.Element;
import net.runelite.client.plugins.tscripts.adapter.models.variable.ArrayAccess;
import net.runelite.client.plugins.tscripts.adapter.models.variable.VariableAssignment;
import net.runelite.client.plugins.tscripts.runtime.variables.Frame;
import net.runelite.client.plugins.tscripts.runtime.variables.VariableMap;
import net.runelite.client.plugins.tscripts.sevices.eventbus.events.*;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.util.Logging;
import net.runelite.client.plugins.tscripts.util.ThreadPool;
import net.runelite.client.plugins.tscripts.sevices.eventbus.TEventBus;
import net.runelite.client.plugins.tscripts.sevices.eventbus._Subscribe;
import net.runelite.client.plugins.tscripts.util.Values;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Responsible for executing scripts.
 */
public class Runtime
{
    private final static CopyOnWriteArrayList<String> interrupted = new CopyOnWriteArrayList<>();

    public static boolean isInterrupted()
    {
        if(interrupted.contains(Thread.currentThread().getName()))
        {
            interrupted.remove(Thread.currentThread().getName());
            return true;
        }
        return false;
    }

    @Getter
    private final VariableMap variableMap;
    private final Map<String, UserDefinedFunction> userDefinedFunctions = new HashMap<>();
    private Pair<String, Map<String,Object>> globalArrays;
    @Getter
    private final MethodManager methodManager;
    private UserDefinedFunction currentFunction = null;
    @Getter
    private Scope rootScope = new Scope(new HashMap<>(), null);
    @Getter
    private String scriptName = "", profile = "";
    private boolean _die = false, _break = false, _continue = false, _done = true, _return = false, breakpointTripped = false;
    @Setter
    private boolean child = false;
    @Getter
    @Setter
    private boolean anonymous = false;
    private ScriptThread scriptThread;

    /**
     * Creates a new instance of the Runtime class.
     */
    public Runtime()
    {
        this.methodManager = MethodManager.getInstance();
        this.variableMap = new VariableMap();
        TEventBus.register(this);
    }

    public Runtime(VariableMap variableMap)
    {
        this.methodManager = MethodManager.getInstance();
        this.variableMap = variableMap;
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
        scriptThread = new ScriptThread(() ->
        {
            postFlags();
            try
            {
                postScriptStateChanged(true);
                processScope(scope);
            }
            catch (Exception ex)
            {
                Logging.errorLog(ex);
            }
            _done = true;
            postScriptStateChanged(false);
            postFlags();
        });
        ThreadPool.submit(scriptThread);
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
        variableMap.pushScope();

        boolean isLoopScope = false;
        boolean isIf = false;
        ConditionType type = scope.getConditions() != null ? scope.getConditions().getType() : ConditionType.NONE;
        switch (type)
        {
            case USER_DEFINED_FUNCTION:
            case LAMBDA:
                addUserDefinedFunction(scope);
                scope.setCurrent(false);
                variableMap.popScope();
                return;
            case WHILE:
                isLoopScope = true;
                break;
            case FOR:
                isLoopScope = true;
                processVariableAssignment(scope.getConditions().getForCondition().getVariableAssignment());
                break;
            case IF:
                isIf = true;
                break;
        }

        boolean shouldProcess = (scope.getConditions() == null || scope.getConditions().getType() == null) || processConditions(scope.getConditions());
        boolean originalShouldProcess = shouldProcess;
        scope.setCurrent(false);

        while (shouldProcess)
        {
            processElements(scope.getElements());
            if (handleControlFlow(isLoopScope)) break;
            if(type == ConditionType.FOR) processVariableAssignment(scope.getConditions().getForCondition().getOperation());
            shouldProcess = isLoopScope && processConditions(scope.getConditions());
        }

        if(isIf && !originalShouldProcess && scope.getElseElements() != null)
        {
            processElements(scope.getElseElements());
        }

        variableMap.popScope();
    }

    private void processElements(Map<Integer, Element> elements)
    {
        for (Element element : elements.values()) {
            processElement(element);
            postFlags();
            if (_die || _break || _continue || _return) break;
        }
    }

    /**
     * Processes an element.
     *
     * @param element The element.
     */
    private void processElement(Element element)
    {
        switch (element.getType())
        {
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
                postBreakpointTripped();
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
            case "eval":
                if(call.getArgs().length == 0) {
                    break;
                }
                Object code = getValue(call.getArgs()[0]);
                if(!(code instanceof String))
                {
                    return;
                }
                Scope scope = Adapter.parse((String)code);
                processScope(scope);
                break;
            default:
                if(userDefinedFunctions.containsKey(call.getName()))
                {
                    processUserFunction(userDefinedFunctions.get(call.getName()), call);
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
        Object var = variableAssignment.getVar();
        String name;

        if(var instanceof ArrayAccess)
        {
            ArrayAccess arrayAccess = (ArrayAccess) var;
            name = arrayAccess.getVariable();
            if(globalArrays != null && globalArrays.getKey().equals(name))
            {
                return;
            }
            Object index = getValue(arrayAccess.getIndex());
            processArrayIndexAssignment(name, index, variableAssignment);
            return;
        }

        name = (String) var;

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
        if(value instanceof Scope)
        {
            Scope scope = (Scope) value;
            if(scope.getConditions() == null || scope.getConditions().getType() != ConditionType.LAMBDA)
            {
                return;
            }
            scope.getConditions().setUserFunctionName(name);
            addUserDefinedFunction(scope);
        }
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

    private void processArrayIndexAssignment(String name, Object index, VariableAssignment variableAssignment)
    {
        if(index == null)
        {
            Object value = getValue(variableAssignment.getValues().get(0));
            if(value == null)
                return;
            if (value.getClass().isArray())
            {
                Object[] integers = (Object[]) value;
                for (int i = 0; i < integers.length; i++)
                {
                    variableMap.put(name, i, integers[i]);
                }
            }
            else if (value instanceof List)
            {
                List<?> integers = (List<?>) value;
                for (int i = 0; i < integers.size(); i++)
                {
                    variableMap.put(name, i, integers.get(i));
                }
            }
            else if (value instanceof String)
            {
                char[] chars = ((String) value).toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    variableMap.put(name, i, chars[i] + "");
                }
            }
            return;
        }

        switch (variableAssignment.getAssignmentType())
        {
            case ADD_ONE:
                incrementVariable(name, index, 1);
                return;
            case REMOVE_ONE:
                decrementVariable(name, index, 1);
                return;
        }

        Object value = getValue(variableAssignment.getValues().get(0));
        switch (variableAssignment.getAssignmentType())
        {
            case ASSIGNMENT:
                variableMap.put(name, index, value);
                return;
            case INCREMENT:
                incrementVariable(name, index, value);
                return;
            case DECREMENT:
                decrementVariable(name, index, value);
                break;
        }
    }

    /**
     * Processes the arguments in a method call.
     *
     * @param methodCall The method call.
     * @return The processed method call.
     */
    private MethodCall processMethodCallArguments(MethodCall methodCall)
    {
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
    private Object processUserFunction(UserDefinedFunction function, MethodCall call)
    {
        Scope scope = function.getScope().clone();
        scope.setConditions(null);
        currentFunction = function;
        Frame frame = variableMap.pushScope();
        for (int i = 0; i < call.getArgs().length; i++)
        {
            frame.put(function.getArguments().get(i), getValue(call.getArgs()[i]));
        }

        processScope(scope);
        Object output = function.getReturnValue() == null ? "null" : function.getReturnValue();
        function.setReturnValue(null);
        currentFunction = null;
        _return = false;
        if(call.isNegate() && output instanceof Boolean)
        {
            return !((Boolean) output);
        }
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
            int integer = Values.getAsInt(value);
            int prev = Values.getAsInt(variableMap.get(name));
            variableMap.getFrames().put(name, prev + integer);
        } else if (value instanceof String) {
            String string = (String) value;
            String prev = variableMap.getFrames().containsKey(name) ? (String) variableMap.get(name) : "";
            variableMap.getFrames().put(name, prev + string);
        }
    }

    private void incrementVariable(String name, Object index, Object value) {
        if (value instanceof Integer) {
            int integer = Values.getAsInt(value);
            int prev = Values.getAsInt(variableMap.get(name));
            variableMap.getFrames().put(name, index, prev + integer);
        } else if (value instanceof String) {
            String string = (String) value;
            String prev = variableMap.getFrames().containsKey(name) ? (String) variableMap.get(name, index) : "";
            variableMap.getFrames().put(name, index, prev + string);
        }
    }

    /**
     * Decrements a variable.
     *
     * @param name The variable name.
     * @param value The value.
     */
    private void decrementVariable(String name, Object value) {
        int integer = Values.getAsInt(value);
        int prev = Values.getAsInt(variableMap.get(name));
        variableMap.put(name, prev - integer);
    }

    private void decrementVariable(String name, Object index, Object value) {
        int integer = Values.getAsInt(value);
        int prev= Values.getAsInt(variableMap.get(name, index));
        variableMap.put(name, index, prev - integer);
    }

    /**
     * Gets the value of an argument.
     *
     * @param object The object.
     * @return The value.
     */
    private Object getValue(Object object)
    {
        if(object instanceof Scope)
        {
            return object;
        }
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
                return string.startsWith("\"") ? string.substring(1) : string;
            }
        }
        if(object instanceof ArrayAccess)
        {
            ArrayAccess arrayAccess = (ArrayAccess) object;
            Object index = getValue(arrayAccess.getIndex());
            if(index == null)
            {
                return null;
            }
            String name = arrayAccess.getVariable();

            Object value;
            if(globalArrays != null && globalArrays.getKey().equals(name))
            {
                value = globalArrays.getValue().get(index + "");
            }
            else
            {
                value = variableMap.get(name, index);
            }
            if(arrayAccess.isNegated())
                return (value instanceof Boolean) ? !((Boolean) value) : value;
            return value;
        }
        else if (object instanceof MethodCall)
        {
            MethodCall methodCall = (MethodCall) object;
            if(userDefinedFunctions.containsKey(methodCall.getName()))
            {
                return processUserFunction(userDefinedFunctions.get(methodCall.getName()), methodCall);
            }
            return methodManager.call(processMethodCallArguments((MethodCall) object));
        }
        else if(object instanceof TernaryExpression)
        {
            return processTernary((TernaryExpression) object);
        }
        else if(object instanceof NullCoalescingExpression)
        {
            return processNullCoalescing((NullCoalescingExpression) object);
        }
        else if(object instanceof NullCheckExpression)
        {
            return processNullCheck((NullCheckExpression) object);
        }
        else if(object instanceof Expression)
        {
            return flushOperationExpression((Expression) object);
        }
        else if (typeOfAny(object, Integer.class, Boolean.class))
        {
            return object;
        }

        return null;
    }

    private Object flushOperationExpression(Expression expression)
    {
        Object element = getValue(expression.getData().getLeft());
        OperatorType operator = expression.getData().getCenter();
        if(operator == null)
        {
            return getValue(element);
        }
        Object element2 = getValue(expression.getData().getRight());

        int left = 0;
        if(element instanceof Integer)
        {
            left = (int) element;
        }
        else if(element instanceof Boolean)
        {
            left = (boolean) element ? 1 : 0;
        }

        int right = 0;
        if(element2 instanceof Integer)
        {
            right = (int) element2;
        }
        else if(element2 instanceof Boolean)
        {
            right = (boolean) element2 ? 1 : 0;
        }

        return OperatorType.compute(left, operator, right);
    }

    /**
     * Adds a user-defined function.
     *
     * @param scope The scope.
     */
    private void addUserDefinedFunction(Scope scope)
    {
        String name = scope.getConditions().getUserFunctionName();
        userDefinedFunctions.put(name, new UserDefinedFunction(name, scope));
    }

    private Object processTernary(TernaryExpression expression)
    {
        Object left = getValue(expression.getTrueValue());
        Object right = getValue(expression.getFalseValue());
        Object ret = processConditions(expression.getConditions()) ? getValue(left) : getValue(right);
        if(ret instanceof Boolean)
        {
            return expression.isNegated() != (Boolean) ret;
        }
        return ret;
    }

    private Object processNullCoalescing(NullCoalescingExpression expression)
    {
        Object left = getValue(expression.getLeft());
        Object right = getValue(expression.getRight());
        Object ret = left != null && !left.equals("null") ? left : right;
        if(ret instanceof Boolean)
        {
            return expression.isNegated() != (Boolean) ret;
        }
        return ret;
    }

    private boolean processNullCheck(NullCheckExpression expression)
    {
        Object value = getValue(expression.getValue());
        return expression.isNegated() != (value == null || value.equals("null"));
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
        interrupted.add(scriptThread.getName());
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
        if(child || anonymous) return;
        Map<String,Object> flags = new HashMap<>();
        flags.put("scriptName", scriptName);
        flags.put("profile", profile);
        flags.put("running", !_done);
        flags.put("frames", variableMap.getFrameCount());
        flags.put("variables", variableMap.getVariableCount());
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
        if(anonymous) return;
        TEventBus.post(CurrentInstructionChanged.get());
    }

    private void postScriptStateChanged(boolean state)
    {
        if(child || anonymous) return;
        TEventBus.post(new ScriptStateChanged(scriptName, profile, state));
    }

    private void postBreakpointTripped()
    {
        if(anonymous) return;
        TEventBus.post(BreakpointTripped.get());
    }

    @_Subscribe
    public void onBreakpointUnTripped(BreakpointUnTripped event)
    {
        if(anonymous) return;
        breakpointTripped = false;
    }
}