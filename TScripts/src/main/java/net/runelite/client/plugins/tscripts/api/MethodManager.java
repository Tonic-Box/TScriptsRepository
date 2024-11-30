package net.runelite.client.plugins.tscripts.api;

import com.google.common.reflect.ClassPath;
import lombok.Getter;
import net.runelite.client.plugins.tscripts.TScriptsPlugin;
import net.runelite.client.plugins.tscripts.api.library.TDelay;
import net.runelite.client.plugins.tscripts.types.*;
import net.runelite.client.plugins.tscripts.adapter.models.method.MethodCall;
import net.runelite.client.plugins.tscripts.util.Logging;

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
    private List<Class<?>> eventClasses = null;
    private Map<String, EventData> eventDataClasses = null;
    @Getter
    private final HashMap<String, MethodDefinition> methods = new HashMap<>();
    private final Set<String> blacklist = Set.of("continue", "break", "die", "subscribe", "breakpoint", "tick", "return");

    /**
     * Constructor
     * @param plugin the plugin
     */
    public MethodManager(TScriptsPlugin plugin)
    {
        this.plugin = plugin;
        fillMethods();
        getEventClasses();
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
        try
        {
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
            else
            {
                Logging.errorLog(new NoSuchMethodException("Method " + methodCall.getName() + " not found"));
            }
        }
        catch(Exception e)
        {
            Logging.errorLog(e);
        }

        //bc menuactions
        TDelay.sleep(20);

        return out == null ? "null" : out;
    }

    /**
     * Fills our method map with all the methods defined in the api definitions classes
     */
    private void fillMethods()
    {
        List<GroupDefinition> groups = getMethodClasses();
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

            if(method == null)
            {
                //Probably a user method
                return CHECK_RESPONSE.OK;
            }

            for(int i = 0; i < method.getParameters().size(); i++)
            {
                Pair<String, Type> parameter = method.getParameters().getOrDefault(i, null);
                if(parameter.getValue().equals(Type.VARARGS))
                    return CHECK_RESPONSE.OK;
            }

            if (method.getParameters().size() != methodCall.getArgs().length)
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
        //Probably a user method
        return CHECK_RESPONSE.OK;
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
            return true;
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
     * Gets all the method classes and converts them to GroupDefinitions
     * @return the classes
     */
    public List<GroupDefinition> getMethodClasses()
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
        methodGroups = classPath.getTopLevelClassesRecursive("net.runelite.client.plugins.tscripts.api.definitions")
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

    /**
     * Gets all the event classes
     * @return the classes
     */
    public List<Class<?>> getEventClasses()
    {
        if(eventClasses != null)
        {
            return eventClasses;
        }
        ClassPath classPath;
        try
        {
            classPath = ClassPath.from(getClass().getClassLoader());
        } catch (IOException e)
        {
            return new ArrayList<>();
        }
        eventClasses = classPath.getTopLevelClassesRecursive("net.runelite.client.events")
                .stream()
                .map(ClassPath.ClassInfo::load)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        eventClasses.addAll(classPath.getTopLevelClassesRecursive("net.runelite.api.events")
                .stream()
                .map(ClassPath.ClassInfo::load)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        eventClasses.addAll(classPath.getTopLevelClassesRecursive("net.unethicalite.api.events")
                .stream()
                .map(ClassPath.ClassInfo::load)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        return eventClasses;
    }

    /**
     * Gets an event class by name
     * @param name the name
     * @return the class
     */
    public Class<?> getEventClass(String name)
    {
        for(Class<?> clazz : getEventClasses())
        {
            if(clazz.getSimpleName().equals(name))
            {
                return clazz;
            }
        }
        return null;
    }

    public Map<String,EventData> getEventDataClasses()
    {
        if(eventDataClasses != null)
        {
            return eventDataClasses;
        }

        ClassPath classPath;
        try
        {
            classPath = ClassPath.from(getClass().getClassLoader());
        } catch (IOException e)
        {
            return new HashMap<>();
        }

        String packageName = "net.runelite.client.plugins.tscripts.api.events";
        try {
            eventDataClasses = new HashMap<>();
            // Load all classes accessible from the context class loader
            for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
                Class<?> clazz = classInfo.load();
                // Check if the class implements EventData
                if (EventData.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                    EventData instance = (EventData) clazz.getDeclaredConstructor().newInstance();
                    eventDataClasses.put(instance.getEventName(), instance);
                }
            }
        } catch (ReflectiveOperationException ex) {
            Logging.errorLog(ex);
        }

        return eventDataClasses;
    }
}