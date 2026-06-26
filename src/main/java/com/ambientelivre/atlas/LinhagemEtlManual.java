package com.ambientelivre.atlas;

import org.apache.atlas.AtlasClientV2;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntity.AtlasEntityWithExtInfo;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.instance.EntityMutationResponse;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.Collections;

public class LinhagemEtlManual {

    public static void main(String[] args) {
        String urlServidor = "http://localhost:21000/";
        String[] basicAuth = new String[]{"admin", "admin"};
        
        System.out.println("====== INICIANDO REGISTRO DE LINHAGEM CORE (v3 - v2.5.0) ======");
        
        Configuration config = new PropertiesConfiguration();
        config.addProperty("atlas.client.readTimeoutMSecs", "60000");
        config.addProperty("atlas.client.connectTimeoutMSecs", "60000");

        try {
            AtlasClientV2 atlasClient = new AtlasClientV2(config, new String[]{urlServidor}, basicAuth);

            // ==========================================
            // PASSO 1: CRIAR ENTIDADE DE ORIGEM (DataSet Genérico)
            // ==========================================
            System.out.println("1. Mapeando origem de dados (DataSet de Origem)...");
            AtlasEntity origemDataSet = new AtlasEntity("DataSet");
            origemDataSet.setAttribute("name", "clientes_raw_v3.csv");
            origemDataSet.setAttribute("qualifiedName", "/opt/dados/origem/clientes_raw_v3.csv@cm");
            origemDataSet.setAttribute("uri", "/opt/dados/origem/clientes_raw_v3.csv");
            origemDataSet.setAttribute("description", "DataSet bruto de origem carregado pelo ETL customizado.");

            System.out.println("Enviando entidade de origem para o Atlas...");
            atlasClient.createEntity(new AtlasEntityWithExtInfo(origemDataSet));

            // ==========================================
            // PASSO 2: CRIAR ENTIDADE DE DESTINO (DataSet Genérico)
            // ==========================================
            System.out.println("2. Mapeando destino de dados (DataSet de Destino)...");
            AtlasEntity destinoDataSet = new AtlasEntity("DataSet");
            destinoDataSet.setAttribute("name", "clientes_processados_v3");
            destinoDataSet.setAttribute("qualifiedName", "hdfs://namenode:8020/data/analytics/clientes_v3@cm");
            destinoDataSet.setAttribute("uri", "hdfs://namenode:8020/data/analytics/clientes_v3");
            destinoDataSet.setAttribute("description", "DataSet final processado armazenado no Data Lake.");

            System.out.println("Enviando entidade de destino para o Atlas...");
            atlasClient.createEntity(new AtlasEntityWithExtInfo(destinoDataSet));

            // ==========================================
            // PASSO 3: CRIAR O PROCESSO DE LINHAGEM NATIVO (Process)
            // ==========================================
            System.out.println("3. Criando processo de linhagem nativo (Process)...");
            AtlasEntity processoETL = new AtlasEntity("Process");
            processoETL.setAttribute("name", "ETL_Clientes_Java_Manual_v3");
            processoETL.setAttribute("qualifiedName", "com.ambientelivre.etl.ClientesJobManualV3@cm");

            // Criando os IDs de referência utilizando os tipos base do grafo do Core (DataSet)
            AtlasObjectId idOrigem = new AtlasObjectId("DataSet", "qualifiedName", "/opt/dados/origem/clientes_raw_v3.csv@cm");
            AtlasObjectId idDestino = new AtlasObjectId("DataSet", "qualifiedName", "hdfs://namenode:8020/data/analytics/clientes_v3@cm");

            // Associando as entradas e saídas ao processo de linhagem
            processoETL.setAttribute("inputs", Collections.singletonList(idOrigem));
            processoETL.setAttribute("outputs", Collections.singletonList(idDestino));

            System.out.println("Enviando o processo de linhagem para o Atlas...");
            atlasClient.createEntity(new AtlasEntityWithExtInfo(processoETL));
            
            System.out.println("[SUCESSO] Linhagem do ETL Core v3 registrada com sucesso!");
            System.out.println("====== PROCESSO CONCLUÍDO ======");

        } catch (Exception e) {
            System.err.println("[ERRO] Falha ao registrar linhagem: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
