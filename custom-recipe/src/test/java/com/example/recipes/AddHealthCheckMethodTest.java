package com.example.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class AddHealthCheckMethodTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddHealthCheckMethod())
            .parser(JavaParser.fromJavaVersion().classpath("spring-web", "spring-context"))
            .typeValidationOptions(TypeValidation.builder().identifiers(false).build());
    }

    @Test
    void addsHealthCheckToRestController() {
        rewriteRun(
            java(
                """
                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                public class MyController {

                    @GetMapping("/hello")
                    public String hello() {
                        return "hello";
                    }
                }
                """,
                """
                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;

                import java.util.Collections;
                import java.util.Map;

                @RestController
                public class MyController {

                    @GetMapping("/hello")
                    public String hello() {
                        return "hello";
                    }

                    @GetMapping("/health")
                    public Map<String, String> healthCheck() {
                        return Collections.singletonMap("status", "UP");
                    }
                }
                """
            )
        );
    }

    @Test
    void doesNotModifyControllerThatAlreadyHasHealthCheck() {
        rewriteRun(
            java(
                """
                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;

                import java.util.Collections;
                import java.util.Map;

                @RestController
                public class MyController {

                    @GetMapping("/health")
                    public Map<String, String> healthCheck() {
                        return Collections.singletonMap("status", "UP");
                    }
                }
                """
            )
        );
    }

    @Test
    void doesNotModifyNonRestControllerClass() {
        rewriteRun(
            java(
                """
                import org.springframework.stereotype.Service;

                @Service
                public class MyService {

                    public String doWork() {
                        return "done";
                    }
                }
                """
            )
        );
    }
}
