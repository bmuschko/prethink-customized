package com.example.recipes;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

public class AddHealthCheckMethod extends Recipe {

    @Override
    public String getDisplayName() {
        return "Add health check endpoint to REST controllers";
    }

    @Override
    public String getDescription() {
        return "Adds a GET /health endpoint method to Spring @RestController classes " +
               "that don't already have one.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl,
                                                            ExecutionContext ctx) {
                classDecl = super.visitClassDeclaration(classDecl, ctx);

                // Only target @RestController classes
                boolean hasRestController = classDecl.getLeadingAnnotations().stream()
                        .anyMatch(a -> TypeUtils.isOfClassType(
                                a.getType(),
                                "org.springframework.web.bind.annotation.RestController"));
                if (!hasRestController) {
                    return classDecl;
                }

                // Skip if a healthCheck method already exists
                boolean hasHealthCheck = classDecl.getBody().getStatements().stream()
                        .filter(J.MethodDeclaration.class::isInstance)
                        .map(J.MethodDeclaration.class::cast)
                        .anyMatch(m -> m.getSimpleName().equals("healthCheck"));
                if (hasHealthCheck) {
                    return classDecl;
                }

                // Add imports
                maybeAddImport("org.springframework.web.bind.annotation.GetMapping");
                maybeAddImport("java.util.Collections");
                maybeAddImport("java.util.Map");

                // Build and apply the template
                return JavaTemplate.builder(
                                "@GetMapping(\"/health\")\n" +
                                "public Map<String, String> healthCheck() {\n" +
                                "    return Collections.singletonMap(\"status\", \"UP\");\n" +
                                "}")
                        .imports(
                                "org.springframework.web.bind.annotation.GetMapping",
                                "java.util.Collections",
                                "java.util.Map")
                        .build()
                        .apply(updateCursor(classDecl),
                                classDecl.getBody().getCoordinates().lastStatement());
            }
        };
    }
}
