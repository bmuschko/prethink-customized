# Architecture

## FINOS CALM architecture diagram

FINOS CALM (Common Architecture Language Model) architecture diagram showing services, databases, external integrations, and messaging connections. Use this to understand the high-level system architecture and component relationships.

## Data Tables

### Service endpoints

**File:** [`service-endpoints.csv`](service-endpoints.csv)

REST/HTTP endpoints exposed by the application.

| Column | Description |
|--------|-------------|
| Entity ID | Unique identifier for this endpoint entity (format: endpoint:{className}#{methodSignature}). |
| Source path | The path to the source file containing the endpoint. |
| Service class | The fully qualified name of the controller or resource class. |
| Method name | The name of the endpoint method. |
| HTTP method | The HTTP method (GET, POST, PUT, DELETE, PATCH, etc.). |
| Path | The URL path pattern for the endpoint. |
| Produces | Content types the endpoint produces (e.g., application/json). |
| Consumes | Content types the endpoint consumes (e.g., application/json). |
| Framework | The web framework used (Spring, JAX-RS, Micronaut, Quarkus). |
| Method signature | The full method signature for linking to method descriptions. |

### Data assets

**File:** [`data-assets.csv`](data-assets.csv)

Data entities, DTOs, and records that represent the application's data model.

| Column | Description |
|--------|-------------|
| Source path | The path to the source file containing the data asset. |
| Class name | The fully qualified name of the data asset class. |
| Simple name | The simple class name for display. |
| Asset type | The type of data asset (Entity, Record, DTO, Document, etc.). |
| Description | A description of the data asset based on its fields. |
| Fields | Comma-separated list of field names. |

### Server configuration

**File:** [`server-configuration.csv`](server-configuration.csv)

Server configuration properties extracted from application.properties/yml.

| Column | Description |
|--------|-------------|
| Source path | The path to the configuration file. |
| Server port | The server port (default: 8080). |
| SSL enabled | Whether SSL/TLS is enabled. |
| Context path | The servlet context path. |
| Protocol | The protocol (HTTP or HTTPS) based on SSL configuration. |

