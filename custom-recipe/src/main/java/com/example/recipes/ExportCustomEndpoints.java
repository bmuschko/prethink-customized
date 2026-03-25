package com.example.recipes;

import org.openrewrite.*;
import org.openrewrite.marker.Markers;
import org.openrewrite.text.PlainText;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ExportCustomEndpoints extends ScanningRecipe<ExportCustomEndpoints.Accumulator> {

    @Override
    public String getDisplayName() {
        return "Export custom endpoints to Prethink context";
    }

    @Override
    public String getDescription() {
        return "Reads endpoint definitions from custom.properties and generates " +
               "a Prethink-compatible context CSV and Markdown file in .moderne/context/.";
    }

    @Override
    public Accumulator getInitialValue(ExecutionContext ctx) {
        return new Accumulator();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Accumulator acc) {
        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public Tree visit(Tree tree, ExecutionContext ctx) {
                if (tree instanceof SourceFile) {
                    SourceFile sf = (SourceFile) tree;
                    String path = sf.getSourcePath().toString();
                    if (path.equals(".moderne/context/custom-endpoints.csv")) {
                        acc.alreadyGenerated = true;
                    } else if (path.endsWith("custom.properties")) {
                        acc.endpoints.addAll(parseEndpoints(sf.printAll()));
                    }
                }
                return tree;
            }
        };
    }

    @Override
    public Collection<? extends SourceFile> generate(Accumulator acc, ExecutionContext ctx) {
        if (acc.endpoints.isEmpty() || acc.alreadyGenerated) {
            return Collections.emptyList();
        }

        List<SourceFile> result = new ArrayList<>();

        result.add(new PlainText(
                Tree.randomId(),
                Paths.get(".moderne/context/custom-endpoints.csv"),
                Markers.EMPTY,
                null, false, null, null,
                buildCsv(acc.endpoints),
                null));

        result.add(new PlainText(
                Tree.randomId(),
                Paths.get(".moderne/context/custom-endpoints.md"),
                Markers.EMPTY,
                null, false, null, null,
                buildMarkdown(),
                null));

        return result;
    }

    static List<EndpointDef> parseEndpoints(String content) {
        Properties props = new Properties();
        try {
            props.load(new StringReader(content));
        } catch (IOException e) {
            return Collections.emptyList();
        }

        Set<String> indices = props.stringPropertyNames().stream()
                .filter(k -> k.startsWith("endpoints."))
                .map(k -> k.split("\\.")[1])
                .collect(Collectors.toCollection(TreeSet::new));

        List<EndpointDef> endpoints = new ArrayList<>();
        for (String idx : indices) {
            String method = props.getProperty("endpoints." + idx + ".method", "");
            String path = props.getProperty("endpoints." + idx + ".path", "");
            String service = props.getProperty("endpoints." + idx + ".service", "");
            String description = props.getProperty("endpoints." + idx + ".description", "");
            if (!method.isEmpty() && !path.isEmpty()) {
                endpoints.add(new EndpointDef(method, path, service, description));
            }
        }
        return endpoints;
    }

    static String buildCsv(List<EndpointDef> endpoints) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP method,Path,Service,Description\n");
        for (EndpointDef ep : endpoints) {
            sb.append(csvEscape(ep.method)).append(',')
                    .append(csvEscape(ep.path)).append(',')
                    .append(csvEscape(ep.service)).append(',')
                    .append(csvEscape(ep.description)).append('\n');
        }
        return sb.toString();
    }

    static String csvEscape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    static String buildMarkdown() {
        return "# Custom Endpoints\n\n" +
                "## External endpoint definitions from custom.properties\n\n" +
                "Endpoint definitions declared in `custom.properties` that are not derived from source code annotations. " +
                "Use this to understand planned or external API endpoints that supplement the auto-detected service endpoints.\n\n" +
                "## Data Tables\n\n" +
                "### Custom endpoints\n\n" +
                "**File:** [`custom-endpoints.csv`](custom-endpoints.csv)\n\n" +
                "Endpoint definitions loaded from custom.properties.\n\n" +
                "| Column | Description |\n" +
                "|--------|-------------|\n" +
                "| HTTP method | The HTTP method (GET, POST, PUT, DELETE, etc.). |\n" +
                "| Path | The URL path pattern for the endpoint. |\n" +
                "| Service | The logical service name that owns this endpoint. |\n" +
                "| Description | A human-readable description of the endpoint. |\n";
    }

    static class Accumulator {
        final List<EndpointDef> endpoints = new ArrayList<>();
        boolean alreadyGenerated = false;
    }

    static class EndpointDef {
        final String method;
        final String path;
        final String service;
        final String description;

        EndpointDef(String method, String path, String service, String description) {
            this.method = method;
            this.path = path;
            this.service = service;
            this.description = description;
        }
    }
}
