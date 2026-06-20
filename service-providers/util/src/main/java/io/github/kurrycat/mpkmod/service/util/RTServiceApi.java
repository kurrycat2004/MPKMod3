package io.github.kurrycat.mpkmod.service.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;

public abstract class RTServiceApi {
    public RTServiceApi() throws ApiCreationException {}

    protected Class<?> clazz(String clazzName) throws ApiCreationException {
        try {
            return Class.forName(clazzName, false, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ApiCreationException("Class not found: " + clazzName);
        }
    }

    protected MethodHandle instanceGetter(Class<?> clazz, String... methodNames) throws ApiCreationException {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();

        MethodType rtype = MethodType.methodType(clazz);
        for (String name : methodNames) {
            try {
                return lookup.findStatic(clazz, name, rtype);
            } catch (NoSuchMethodException | IllegalAccessException ignored) {
            }
        }

        throw new ApiCreationException(
                "Method not found: " + clazz.getName() + "." +
                Arrays.toString(methodNames) + rtype.descriptorString()
        );
    }

    protected MethodHandle constructor(Class<?> clazz, MethodType methodType) throws ApiCreationException {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();

        try {
            return lookup.findConstructor(clazz, methodType);
        } catch (NoSuchMethodException e) {
            throw new ApiCreationException(
                    "Constructor not found: " + clazz.getName() +
                    "<init>" + methodTypeDescriptor(methodType)
            );
        } catch (IllegalAccessException e) {
            throw new ApiCreationException(
                    "Constructor not accessible: " + clazz.getName() +
                    "<init>" + methodTypeDescriptor(methodType)
            );
        }
    }

    protected MethodHandle method(Class<?> clazz, MethodType methodType, String... methodNames) throws ApiCreationException {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();

        for (String name : methodNames) {
            try {
                return lookup.findVirtual(clazz, name, methodType);
            } catch (NoSuchMethodException | IllegalAccessException ignored) {
            }
        }

        throw new ApiCreationException(
                "Method not found: " + clazz.getName() + "." +
                Arrays.toString(methodNames) + methodTypeDescriptor(methodType)
        );
    }

    protected MethodHandle getter(Class<?> clazz, Class<?> fieldType, String... fieldNames) throws ApiCreationException {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();

        for (String name : fieldNames) {
            try {
                return lookup.findGetter(clazz, name, fieldType);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }

        throw new ApiCreationException(
                "Field not found: " + clazz.getName() + "." + Arrays.toString(fieldNames)
        );
    }

    //TODO: remove when https://github.com/unimined/JvmDowngrader/issues/44 is fixed
    private String methodTypeDescriptor(MethodType methodType) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Class<?> pt : methodType.parameterArray())
            sb.append(pt.descriptorString());
        sb.append(')');
        sb.append(methodType.returnType().descriptorString());
        return sb.toString();
    }

    public MethodHandle feed(MethodHandle method, MethodHandle... args) {
        MethodHandle methodHandle = method;
        for (MethodHandle arg : args) {
            methodHandle = MethodHandles.collectArguments(methodHandle, 0, arg);
        }
        return methodHandle;
    }
}
