package com.ambientelivre.atlas;

import org.apache.atlas.AtlasClientV2;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntity.AtlasEntityWithExtInfo;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.instance.EntityMutationResponse;
import org.apache.atlas.model.instance.EntityMutations.EntityOperation;
import org.apache.atlas.model.instance.AtlasClassification;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutomacaoAtlas {

    public static void main(String[] args) {
        String urlServidor = "http://localhost:21000/";
        String[] basicAuth = new String[]{"admin", "admin"};
        
        System.out.println("====== INICIANDO AUTOMAÇÃO APACHE ATLAS (v2.5.0) ======");
        System.out.println("Conectando ao servidor: " + urlServidor);
        
        // 1. Blindando a inicialização: Criando uma configuração programática 
        // para evitar que o SDK busque um arquivo atlas-application.properties inexistente
        Configuration config = new PropertiesConfiguration();
        config.addProperty("atlas.client.readTimeoutMSecs", "60000");
        config.addProperty("atlas.client.connectTimeoutMSecs", "60000");

        try {
            // Inicialização usando a assinatura estável com o objeto Configuration
            AtlasClientV2 atlasClient = new AtlasClientV2(config, new String[]{urlServidor}, basicAuth);

            // 2. Configurando os atributos obrigatórios da tabela
            AtlasEntity tabela = new AtlasEntity("hive_table");
            tabela.setAttribute("name", "titanic_java_api");
            tabela.setAttribute("qualifiedName", "marcio_vieira.titanic_java_api@cm"); 
            tabela.setAttribute("owner", "miguel");
            tabela.setAttribute("tableType", "EXTERNAL_TABLE");
            tabela.setAttribute("temporary", false);
            tabela.setAttribute("retention", 0);

            // 3. Criando o relacionamento com o banco existente no grafo (marcio_vieira@cm)
            Map<String, Object> dbUniqueAttrs = new HashMap<>();
            dbUniqueAttrs.put("qualifiedName", "marcio_vieira@cm");
            
            AtlasObjectId dbObjectId = new AtlasObjectId("hive_db", dbUniqueAttrs);
            tabela.setAttribute("db", dbObjectId);

            // 4. Preparando o wrapper que casa perfeitamente com a v2.5.0
            AtlasEntityWithExtInfo entityWrapper = new AtlasEntityWithExtInfo(tabela);

            // 5. Enviando requisição para o Atlas
            System.out.println("Cadastrando a tabela 'titanic_java_api'...");
            EntityMutationResponse response = atlasClient.createEntity(entityWrapper);
            
            // 6. Extraindo o GUID com o padrão Map da versão 2.5.0
            String guidGerado = null;
            
            List<AtlasEntityHeader> criadas = response.getCreatedEntities();
            List<AtlasEntityHeader> atualizadas = response.getUpdatedEntities();
            Map<EntityOperation, List<AtlasEntityHeader>> modificadasMap = response.getMutatedEntities();

            if (criadas != null && !criadas.isEmpty()) {
                guidGerado = criadas.get(0).getGuid();
            } else if (atualizadas != null && !atualizadas.isEmpty()) {
                guidGerado = atualizadas.get(0).getGuid();
            } else if (modificadasMap != null && !modificadasMap.isEmpty()) {
                for (List<AtlasEntityHeader> listaHeaders : modificadasMap.values()) {
                    if (listaHeaders != null && !listaHeaders.isEmpty()) {
                        guidGerado = listaHeaders.get(0).getGuid();
                        break;
                    }
                }
            }

            if (guidGerado == null) {
                throw new RuntimeException("Não foi possível rastrear o GUID da entidade criada/atualizada.");
            }

            System.out.println("[SUCESSO] Tabela registrada com GUID: " + guidGerado);

            // 7. Aplicando a Tag de Governança de forma automática
            System.out.println("Aplicando classificação 'DADO_SENSIVEL' via código...");
            AtlasClassification classification = new AtlasClassification("DADO_SENSIVEL");
            
            atlasClient.addClassifications(guidGerado, Collections.singletonList(classification));
            System.out.println("[SUCESSO] Tag vinculada com sucesso!");
            System.out.println("====== PROCESSO CONCLUÍDO ======");

        } catch (Exception e) {
            System.err.println("[ERRO] Falha ao executar automação: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
