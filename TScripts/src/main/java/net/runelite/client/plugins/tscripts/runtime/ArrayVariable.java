package net.runelite.client.plugins.tscripts.runtime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ArrayVariable
{
    private final String name;
    private final String scopeHash;
    private final Map<Object,Object> values = new HashMap<>();
}
