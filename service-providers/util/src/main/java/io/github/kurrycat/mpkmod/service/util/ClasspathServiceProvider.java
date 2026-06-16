package io.github.kurrycat.mpkmod.service.util;

import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class ClasspathServiceProvider<T> extends StandardServiceProvider<T> {
    protected ClasspathServiceProvider(Supplier<T> provider, Class<T> type) {
        super(provider, type);
    }

    protected record RequiredMethod(
            String className,
            String methodName,
            String returnTypeName,
            String... argTypeNames
    ) {
        public RequiredMethod {}
    }

    protected abstract List<RequiredMethod> requiredMethods();

    @Override
    public final Optional<String> invalidReason() {
        for (RequiredMethod requiredMethod : requiredMethods()) {
            Optional<String> invalidReason = checkMethod(requiredMethod);
            if (invalidReason.isPresent()) return invalidReason;
        }
        return Optional.empty();
    }

    private Optional<String> checkMethod(RequiredMethod requiredMethod) {
        Class<?> clazz = getClazz(requiredMethod.className);
        if (clazz == null) return missingClass(requiredMethod.className);

        Class<?> returnType = getType(requiredMethod.returnTypeName);
        if (returnType == null) return missingClass(requiredMethod.returnTypeName);

        Class<?>[] argTypes = new Class<?>[requiredMethod.argTypeNames.length];
        for (int i = 0; i < requiredMethod.argTypeNames.length; i++) {
            String argTypeName = requiredMethod.argTypeNames[i];
            Class<?> argType = getType(argTypeName);
            if (argType == null) return missingClass(requiredMethod.returnTypeName);
            argTypes[i] = argType;
        }

        Executable method = getMethod(clazz, requiredMethod.methodName, returnType, argTypes);
        if (method == null) return missingMethod(requiredMethod);

        return Optional.empty();
    }

    private static Optional<String> missingClass(String className) {
        return Optional.of("Missing class: " + className);
    }

    private static Optional<String> missingMethod(RequiredMethod requiredMethod) {
        return Optional.of(
                "Missing method: " +
                requiredMethod.className + "#" +
                requiredMethod.methodName + "(" +
                String.join(",", requiredMethod.argTypeNames) + ")" +
                requiredMethod.returnTypeName
        );
    }

    private Class<?> getType(String typeName) {
        return switch (typeName) {
            case "void" -> void.class;
            case "boolean" -> boolean.class;
            case "byte" -> byte.class;
            case "short" -> short.class;
            case "char" -> char.class;
            case "int" -> int.class;
            case "long" -> long.class;
            case "float" -> float.class;
            case "double" -> double.class;
            default -> getClazz(typeName);
        };
    }

    private Executable getMethod(
            Class<?> ownerClass,
            String methodName,
            Class<?> returnType,
            Class<?>... argTypes
    ) {
        try {
            if (methodName.equals("<init>")) {
                Constructor<?> constructor = ownerClass.getConstructor(argTypes);
                if (!returnType.equals(void.class)) return null;
                return constructor;
            }
            Method method = ownerClass.getMethod(methodName, argTypes);
            if (!method.getReturnType().equals(returnType)) return null;
            return method;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private Class<?> getClazz(String binaryClassName) {
        try {
            return Class.forName(binaryClassName, false, getClass().getClassLoader());
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
