# Samples Apache Atlas

## Apache Atlas Java Client Automation Example

This repository contains a complete, production-ready Java automation example using the **Apache Atlas Client V2 API** (compatible with Apache Atlas v2.5.0+). 

The goal of this project is to demonstrate how data professionals can programmatically interact with a Data Governance and Metadata Catalog tool, automating entity creation, metadata lineage, and classification tagging.

This lab is an integral part of the official training program provided by **Ambiente Livre**:
👉 [Apache Atlas Training - Ambiente Livre](https://www.ambientelivre.com.br/treinamento/data-science/apache-atlas.html)

## Features Demonstrated

- **Programmatic Initialization:** Safe bootstrapping of `AtlasClientV2` using Apache Commons Configuration to avoid standard classpath/environment file conflicts.
- **Entity Creation (`hive_table`):** Dynamic mapping and registration of a Hive table schema (`titanic_java_api`) within the metadata graph.
- **Graph Referencing:** Programmatic link establishment between the newly created table and an existing parent database (`hive_db`) using unique qualifying attributes.
- **Automated Data Governance:** Automated attachment of security classifications (`DADO_SENSIVEL` / Sensitive Data tag) directly to the generated entity GUID.

## Project Structure

```text
atlas-java-automation/
├── .gitignore
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── ambientelivre/
        │           └── atlas/
        │               └── AutomacaoAtlas.java
        └── resources/
            └── log4j.properties
