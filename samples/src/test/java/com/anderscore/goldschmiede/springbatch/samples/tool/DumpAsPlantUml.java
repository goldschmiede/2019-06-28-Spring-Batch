package com.anderscore.goldschmiede.springbatch.samples.tool;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import javax.batch.api.listener.JobListener;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.FlowStepBuilder;
import org.springframework.batch.core.step.builder.JobStepBuilder;
import org.springframework.batch.core.step.builder.PartitionStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.builder.TaskletStepBuilder;
import org.springframework.batch.item.ItemStream;
import org.springframework.retry.RetryListener;

public class DumpAsPlantUml {
    private final PrintStream out = System.out;

    @Test
    void dumpListener2() {
        dumpTree(ItemStream.class);
        dumpTree(JobListener.class);
    }

    void dumpListener() {
        dumpTree(StepListenerSupport.class);
        dumpTree(RetryListener.class);
    }

    void dumpBuilder() {
        dumpTree(FaultTolerantStepBuilder.class);
        dumpTree(TaskletStepBuilder.class);
        dumpTree(FlowStepBuilder.class);
        dumpTree(JobStepBuilder.class);
        dumpTree(PartitionStepBuilder.class);
        dumpTree(StepBuilder.class);
    }

    private void dumpTree(Class<?> clazz) {
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && !Object.class.equals(superclass)) {
            dumpTree(superclass);
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            dumpTree(ifc);
        }
        dump(clazz);
        out.println();
        if (superclass != null && !Object.class.equals(superclass)) {
            out.print(superclass.getSimpleName());
            out.print(" <|-- ");
            out.println(clazz.getSimpleName());
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            out.println();
            out.print(ifc.getSimpleName());
            if (clazz.isInterface()) {
                out.print(" <|-- ");
            } else {
                out.print(" <|.. ");
            }
            out.println(clazz.getSimpleName());
        }
        out.println();
    }

    private void dump(Class<?> clazz) {
        if (clazz.isInterface()) {
            out.print("interface");
        } else {
            out.print("class");
        }
        out.print(" ");
        out.print(clazz.getSimpleName());
        out.println(" {");
        dumpPublicMethods(clazz);
        out.println("}");
    }

    private void dumpPublicMethods(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                dumpMethod(method);
            }
        }
    }

    private void dumpMethod(Method method) {
        out.print("    ");
        out.print(method.getName());
        out.print("(");
        boolean first = true;
        for (Parameter parameter : method.getParameters()) {
            if (first) {
                first = false;
            } else {
                out.print(", ");
            }

            out.print(parameter.getName());
            out.print(": ");
            out.print(parameter.getType().getSimpleName());
        }
        out.print(")");
        Class<?> returnType = method.getReturnType();
        if (returnType != Void.TYPE) {
            out.print(": ");
            out.print(returnType.getSimpleName());
        }
        out.println();
    }
}
