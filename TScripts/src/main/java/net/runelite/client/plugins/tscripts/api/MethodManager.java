package net.runelite.client.plugins.tscripts.api;

import com.google.common.reflect.ClassPath;
import lombok.Getter;
import net.runelite.client.plugins.tscripts.TScriptsPlugin;
import net.runelite.client.plugins.tscripts.types.GroupDefinition;
import net.runelite.client.plugins.tscripts.types.MethodDefinition;
import net.runelite.client.plugins.tscripts.types.Pair;
import net.runelite.client.plugins.tscripts.types.Type;
import net.runelite.client.plugins.tscripts.lexer.MethodCall;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages all the methods that can be called from the script
 */
@Getter
public class MethodManager
{
    @Getter
    private static MethodManager instance;
    private final TScriptsPlugin plugin;
    private List<GroupDefinition> methodGroups = null;
    @Getter
    private final HashMap<String, MethodDefinition> methods = new HashMap<>();
    private final Set<String> blacklist = Set.of("continue", "break", "die", "debug");

    /**
     * Constructor
     * @param plugin the plugin
     */
    public MethodManager(TScriptsPlugin plugin)
    {
        this.plugin = plugin;
        fillMethods();
        instance = this;
    }

    /**
     * Calls a method
     * @param methodCall the method call
     * @return the result of the method
     */
    public Object call(MethodCall methodCall)
    {
        Object out = null;
        if (methods.containsKey(methodCall.getName().toLowerCase()))
        {
            MethodDefinition method = methods.getOrDefault(methodCall.getName().toLowerCase(), null);
            if (method == null)
                return "null";
            out = method.getFunction().apply(methodCall);
            if(methodCall.isNegate() && out instanceof Boolean)
            {
                out = !(boolean) out;
            }
        }

        //bc menuactions
        try {
            Thread.sleep(20);
        } catch (InterruptedException ignored) {
        }

        return out == null ? "null" : out;
    }

    /**
     * Fills our method map with all the methods defined in the api definitions classes
     */
    private void fillMethods()
    {
        List<GroupDefinition> groups = getClasses("net.runelite.client.plugins.tscripts.api.definitions");
        for (GroupDefinition groupDefinition : groups)
        {
            List<MethodDefinition> methods = groupDefinition.methods(this);
            for (MethodDefinition method : methods)
            {
                this.methods.put(method.getName().toLowerCase(), method);
            }
        }
    }

    /**
     * Checks if a method call and its arguments are valid
     * @param methodCall the method call
     * @return the response
     */
    public CHECK_RESPONSE check(MethodCall methodCall)
    {
        if (blacklist.contains(methodCall.getName().toLowerCase()))
            return CHECK_RESPONSE.OK;
        else if (methods.containsKey(methodCall.getName().toLowerCase()))
        {
            MethodDefinition method = methods.getOrDefault(methodCall.getName().toLowerCase(), null);
            if (method == null || method.getParameters().size() != methodCall.getArgs().length)
            {
                return CHECK_RESPONSE.INCORRECT_PARAMETERS_SIZE;
            }

            for(int i = 0; i < method.getParameters().size(); i++)
            {
                Pair<String, Type> parameter = method.getParameters().getOrDefault(i, null);
                if(!checkParam(methodCall.getArgs()[i], parameter))
                    return CHECK_RESPONSE.INCORRECT_PARAMETER_TYPE;
            }
            return CHECK_RESPONSE.OK;
        }
        return CHECK_RESPONSE.NOT_FOUND;
    }

    /**
     * Checks if a method call parameter is valid
     * @param methodCallParam the method call parameter
     * @param parameter the parameter
     * @return if the parameter is valid
     */
    private boolean checkParam(Object methodCallParam, Pair<String, Type> parameter)
    {
        if (parameter == null)
            return false;

        if(methodCallParam instanceof MethodCall)
        {
            MethodCall innerMethodCall = (MethodCall) methodCallParam;
            MethodDefinition innerMethod = methods.getOrDefault(innerMethodCall.getName().toLowerCase(), null);
            return innerMethod != null && innerMethod.getReturnType().equals(parameter.getValue());
        }

        if(methodCallParam instanceof String)
        {
            String string = (String) methodCallParam;
            if(string.startsWith("$"))
                return true;
        }

        switch (parameter.getValue())
        {
            case INT:
                if (!(methodCallParam instanceof Integer))
                    return false;
                break;
            case STRING:
                if (!(methodCallParam instanceof String))
                    return false;
                break;
            case BOOL:
                if (!(methodCallParam instanceof Boolean))
                    return false;
                break;
            case ANY:
                break;
        }

        return true;
    }

    /**
     * The response of the check method
     */
    public enum CHECK_RESPONSE
    {
        OK,
        NOT_FOUND,
        INCORRECT_PARAMETERS_SIZE,
        INCORRECT_PARAMETER_TYPE
    }

    /**
     * Gets all the classes in a package
     * @param packageName the package name
     * @return the classes
     */
    public List<GroupDefinition> getClasses(String packageName)
    {
        if(methodGroups != null)
        {
            return methodGroups;
        }
        ClassPath classPath;
        try
        {
            classPath = ClassPath.from(getClass().getClassLoader());
        } catch (IOException e)
        {
            return new ArrayList<>();
        }
        methodGroups = classPath.getTopLevelClassesRecursive(packageName)
                .stream()
                .map(ClassPath.ClassInfo::load)
                .filter(GroupDefinition.class::isAssignableFrom)
                .map(clazz -> {
                    try {
                        return (GroupDefinition) clazz.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return methodGroups;
    }
}