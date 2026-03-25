package com.example.recipes;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.test.SourceSpecs.text;

class ExportCustomEndpointsTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ExportCustomEndpoints());
    }

    @Test
    void generatesContextFilesFromCustomProperties() {
        rewriteRun(
            text(
                """
                endpoints.1.method=POST
                endpoints.1.path=/api/v2/orders
                endpoints.1.service=OrderService
                endpoints.1.description=Submit a new order

                endpoints.2.method=GET
                endpoints.2.path=/api/v2/orders/{orderId}
                endpoints.2.service=OrderService
                endpoints.2.description=Retrieve order details by ID
                """,
                spec -> spec.path("app/src/main/resources/custom.properties")
            ),
            text(
                null,
                """
                HTTP method,Path,Service,Description
                POST,/api/v2/orders,OrderService,Submit a new order
                GET,/api/v2/orders/{orderId},OrderService,Retrieve order details by ID
                """,
                spec -> spec.path(".moderne/context/custom-endpoints.csv")
            ),
            text(
                null,
                """
                # Custom Endpoints

                ## External endpoint definitions from custom.properties

                Endpoint definitions declared in `custom.properties` that are not derived from source code annotations. Use this to understand planned or external API endpoints that supplement the auto-detected service endpoints.

                ## Data Tables

                ### Custom endpoints

                **File:** [`custom-endpoints.csv`](custom-endpoints.csv)

                Endpoint definitions loaded from custom.properties.

                | Column | Description |
                |--------|-------------|
                | HTTP method | The HTTP method (GET, POST, PUT, DELETE, etc.). |
                | Path | The URL path pattern for the endpoint. |
                | Service | The logical service name that owns this endpoint. |
                | Description | A human-readable description of the endpoint. |
                """,
                spec -> spec.path(".moderne/context/custom-endpoints.md")
            )
        );
    }

    @Test
    void doesNotGenerateWhenNoCustomProperties() {
        rewriteRun(
            text(
                "some.other.key=value\n",
                spec -> spec.path("app/src/main/resources/application.properties")
            )
        );
    }

    @Test
    void doesNotGenerateWhenPropertiesHasNoEndpoints() {
        rewriteRun(
            text(
                "some.other.key=value\n",
                spec -> spec.path("app/src/main/resources/custom.properties")
            )
        );
    }

    @Test
    void doesNotDeleteExistingFilesInModerneContext() {
        rewriteRun(
            text(
                """
                endpoints.1.method=GET
                endpoints.1.path=/api/health
                endpoints.1.service=HealthService
                endpoints.1.description=Health check
                """,
                spec -> spec.path("app/src/main/resources/custom.properties")
            ),
            // Simulate a pre-existing Prethink file — should NOT be deleted
            text(
                "{\"nodes\":[]}",
                spec -> spec.path(".moderne/context/calm-architecture.json")
            ),
            text(
                "# Architecture\nSome content",
                spec -> spec.path(".moderne/context/architecture.md")
            ),
            text(
                null,
                """
                HTTP method,Path,Service,Description
                GET,/api/health,HealthService,Health check
                """,
                spec -> spec.path(".moderne/context/custom-endpoints.csv")
            ),
            text(
                null,
                """
                # Custom Endpoints

                ## External endpoint definitions from custom.properties

                Endpoint definitions declared in `custom.properties` that are not derived from source code annotations. Use this to understand planned or external API endpoints that supplement the auto-detected service endpoints.

                ## Data Tables

                ### Custom endpoints

                **File:** [`custom-endpoints.csv`](custom-endpoints.csv)

                Endpoint definitions loaded from custom.properties.

                | Column | Description |
                |--------|-------------|
                | HTTP method | The HTTP method (GET, POST, PUT, DELETE, etc.). |
                | Path | The URL path pattern for the endpoint. |
                | Service | The logical service name that owns this endpoint. |
                | Description | A human-readable description of the endpoint. |
                """,
                spec -> spec.path(".moderne/context/custom-endpoints.md")
            )
        );
    }

    @Test
    void handlesEndpointWithCommaInDescription() {
        rewriteRun(
            text(
                """
                endpoints.1.method=POST
                endpoints.1.path=/api/orders
                endpoints.1.service=OrderService
                endpoints.1.description=Create order, validate payment
                """,
                spec -> spec.path("custom.properties")
            ),
            text(
                null,
                """
                HTTP method,Path,Service,Description
                POST,/api/orders,OrderService,"Create order, validate payment"
                """,
                spec -> spec.path(".moderne/context/custom-endpoints.csv")
            ),
            text(
                null,
                ExportCustomEndpoints.buildMarkdown(),
                spec -> spec.path(".moderne/context/custom-endpoints.md")
            )
        );
    }
}
