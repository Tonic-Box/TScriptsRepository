package net.runelite.client.plugins.tscripts.runtime.variables;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ArrayVariable
{
    private final String name;
    private final Map<Object,Object> values = new HashMap<>();
}
