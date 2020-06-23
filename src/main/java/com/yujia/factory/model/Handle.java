package com.yujia.factory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Handle {
    private Method method;
    private Object object;
    private Pattern pattern;
    private Set<String> securities;
    private Map<String, Integer> parameterIndexMap;
}
