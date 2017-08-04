package com.atlassian.capture.findbugs.BytecodeScanningDetector;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.StringAnnotation;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;

public class UnsafeSingletonFieldDetector extends BytecodeScanningDetector {

    private static final String[] CLASS_SINGLETON_ANNOTATIONS = new String[]{
            "org/springframework/stereotype/Service",
            "org/springframework/stereotype/Component",
    };

    private static final List<String> SAFE_FIELD_SIGNATURES = Arrays.asList(
            "Lorg/slf4j/Logger;",
            "Lorg/apache/log4j/Logger;",
            "Ljava/util/logging/Logger;",
            "Lcom/atlassian/jira/log/clean/LaasLogger;"
    );

    private static final List<String> SAFE_CONSTANT_FIELD_SIGNATURES = Arrays.asList(
            "Ljava/lang/Integer;",
            "Ljava/lang/Long;",
            "Ljava/lang/Double;",
            "Ljava/lang/Boolean;",
            "Ljava/util/regex/Pattern;",
            "Ljava/lang/Object;"
    );

    private static final String TENANT_AWARE = "TenantAware";
    private static final String AUTOWIRED = "Autowired";
    private static final String JIRA_RESOURCE = "JIRAResource";
    private static final String CAPTURE_RESOURCE = "Resource";


    private final BugReporter bugReporter;
    private boolean isSingleton;
    private List<Field> tenantSuspectFields;
    private JavaClass javaClass;


    public UnsafeSingletonFieldDetector(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
        this.tenantSuspectFields = new ArrayList<>();
    }

    @Override
    public void visit(JavaClass javaClass) {
        this.javaClass = javaClass;
        tenantSuspectFields.clear();
        isSingleton = isService(javaClass);
        if (isSingleton) {
            try {
                for (JavaClass classes : javaClass.getSuperClasses()) {
                    for (Field field : classes.getFields()) {
                        visit(field);
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot find superclass", e);
            }
        }
    }


    @Override
    public void visit(Method method) {
        if (!tenantSuspectFields.isEmpty()) {
            // Remove constructor autowired fields from suspect field list
            if ("<init>".equals(method.getName()) && matchingAnnotationEntry(method.getAnnotationEntries(), "Autowired").isPresent()) {
                List<Type> autowiredConstructorTypes = Arrays.asList(method.getArgumentTypes());
                tenantSuspectFields.removeIf(field -> autowiredConstructorTypes.contains(field.getType()));
            }
        }
    }

    @Override
    public void visit(Field field) {
        if (isSingleton &&
                !isSimpleConstant(field) &&
                !hasSafeSignature(field) &&
                !isAutowired(field) &&
                !isTenantAware(field)
                ) {
            tenantSuspectFields.add(field);
        }
    }

    private boolean isService(JavaClass javaClass) {
        AnnotationEntry[] annotationEntries = javaClass.getAnnotationEntries();
        for (AnnotationEntry annotationEntry : annotationEntries) {
            String annotationType = annotationEntry.getAnnotationType();

            for (String annotationName : CLASS_SINGLETON_ANNOTATIONS) {
                if (annotationType.contains(annotationName)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isTenantAware(Field field) {
        Optional<Boolean> hasResolvedAnnotation = matchingAnnotationEntry(field.getAnnotationEntries(), TENANT_AWARE)
                .map(annotation -> {
                    boolean hasUnresolved = Arrays.stream(annotation.getElementValuePairs())
                            .anyMatch(elementValuePair -> elementValuePair.getValue().toString().equals("UNRESOLVED"));
                    return !hasUnresolved;
                });

        return hasResolvedAnnotation.orElseGet(() -> false);
    }

    private boolean isAutowired(Field field) {
        return matchingAnnotationEntry(field.getAnnotationEntries(), AUTOWIRED, JIRA_RESOURCE, CAPTURE_RESOURCE).isPresent();
    }

    private boolean hasSafeSignature(Field field) {
        return SAFE_FIELD_SIGNATURES.contains(field.getSignature());
    }

    private boolean isSimpleConstant(Field field) {
        Type type = field.getType();
        return field.isFinal() && (type instanceof BasicType || type.equals(Type.STRING) || type.equals(Type.CLASS) ||
                SAFE_CONSTANT_FIELD_SIGNATURES.contains(type.getSignature()));
    }

    private Optional<AnnotationEntry> matchingAnnotationEntry(AnnotationEntry[] annotationEntries, String... annotationTypeFragments) {
        for (String annotationTypeFragment : annotationTypeFragments) {
            for (AnnotationEntry annotationEntry : annotationEntries) {
                if (annotationEntry.getAnnotationType().contains(annotationTypeFragment)) {
                    return Optional.of(annotationEntry);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void visitAfter(JavaClass obj) {
        if (!tenantSuspectFields.isEmpty()) {
            for (Field suspectField : tenantSuspectFields) {
                BugInstance bugInstance = new BugInstance(this, "UNSAFE_SINGLETON_FIELD", HIGH_PRIORITY).addClass(javaClass).addField(
                        new FieldAnnotation(javaClass.getClassName(), suspectField.getName(), suspectField.getSignature(), suspectField.getAccessFlags()));

                Optional<AnnotationEntry> annotationEntry = matchingAnnotationEntry(suspectField.getAnnotationEntries(), TENANT_AWARE);
                Optional<String> unresolvedComment = annotationEntry.map(tenantAwareAnnotation -> Arrays.stream(tenantAwareAnnotation.getElementValuePairs())
                        .filter(elementValuePair -> elementValuePair.getNameString().equals("comment"))
                        .findFirst()
                        .map(annotationComment -> annotationComment.getValue().stringifyValue())
                        .orElse("NO UNRESOLVED COMMENT")
                );

                bugInstance.add(new StringAnnotation("[" + unresolvedComment.orElse("NOT ANNOTATED") + "]"));

                bugReporter.reportBug(bugInstance);

            }
        }
    }

}
