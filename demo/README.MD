# Projeto Spring Boot com Lombok e Elasticsearch

Este é um projeto Java utilizando Spring Boot, Lombok e integração com Elasticsearch. O objetivo deste projeto é demonstrar como criar uma API simples que se comunica com o Elasticsearch para indexar e consultar dados e utilizar Ingest Pipelines do Elasticsearch APM para mascarar dados sensiveis de logs.

# Objetivo principal
Demonstrar a utilização de Ingest Pipelines do APM Elasticsearch

- **Referencias**:
  - **Elastic APM Custom Filters** - https://www.elastic.co/guide/en/observability/current/apm-custom-filter.html
  - **Create Ingest Pipeline** - https://www.elastic.co/guide/en/observability/current/apm-custom-filter.html#apm-filters-ingest-pipeline 
  - **Parse data using ingest pipelines** - https://www.elastic.co/guide/en/observability/current/apm-ingest-pipelines.html
  - **Custom Ingest Pipelines** - https://www.elastic.co/guide/en/observability/current/apm-ingest-pipelines.html#custom-ingest-pipelines
  - **Runtime Fields** - https://www.elastic.co/guide/en/elasticsearch/reference/current/runtime-examples.html 


Mecanismo de logica aplicado no script 

```js 
def servicesConfig = [
'poc-demo-logs': ['dev', 'staging'],
'another-service': ['dev', 'qa'],
'third-service': ['dev', 'prod']
];

if (ctx.service != null && 
      ctx.service.name != null && 
      ctx.service.environment != null) {

      def serviceName = ctx.service.name;
      def environment = ctx.service.environment;

      if (servicesConfig.containsKey(serviceName) && 
          servicesConfig[serviceName].contains(environment)) {
      
            ctx.debug_should_mask = 'true';
      } else {
            ctx.debug_should_mask = 'false';
      }
} else {
      
      ctx.debug_should_mask = 'false';
}
```

Ingest Pipeline completa utilizada para mascaramento de informações sensiveis.

```json 
[
  {
    "set": {
      "field": "debug_service_name",
      "value": "{{service.name}}",
      "if": "ctx.service.name == 'poc-demo-logs'",
      "ignore_failure": true
    }
  },
  {
    "script": {
      "source": "def servicesConfig = [\r\n'poc-demo-logs': ['dev', 'staging'],\r\n'another-service': ['dev', 'qa'],\r\n'third-service': ['dev', 'prod']\r\n];\r\n\r\nif (ctx.service != null && \r\n      ctx.service.name != null && \r\n      ctx.service.environment != null) {\r\n\r\n      def serviceName = ctx.service.name;\r\n      def environment = ctx.service.environment;\r\n\r\n      if (servicesConfig.containsKey(serviceName) && \r\n          servicesConfig[serviceName].contains(environment)) {\r\n      \r\n            ctx.debug_should_mask = 'true';\r\n      } else {\r\n            ctx.debug_should_mask = 'false';\r\n      }\r\n} else {\r\n      \r\n      ctx.debug_should_mask = 'false';\r\n}",
      "ignore_failure": true,
      "description": "Set a value for debug_should_mask based on a list of APM application and environments"
    }
  },
  {
    "gsub": {
      "field": "message",
      "pattern": "\\b(\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2})\\b",
      "replacement": "possible.cpf.redacted=XXX.XXX.XXX-XX",
      "ignore_missing": true,
      "if": "ctx.debug_should_mask == 'true'",
      "ignore_failure": true
    }
  },
  {
    "gsub": {
      "field": "message",
      "pattern": "\\b(\\d{2}\\.\\d{3}\\.\\d{3}[-]?[0-9X]?)\\b",
      "replacement": "possible.rg.redacted=XX.XXX.XXX-X",
      "ignore_missing": true,
      "if": "ctx.debug_should_mask == 'true'",
      "ignore_failure": true
    }
  },
  {
    "gsub": {
      "field": "message",
      "pattern": "\\b(?!\\+)(?:\\d{4}[-\\s]?){3}\\d{4}\\b",
      "replacement": "possible.cred.card.redacted=XXXX-XXXX-XXXX-XXXX",
      "ignore_missing": true,
      "if": "ctx.debug_should_mask == 'true'",
      "ignore_failure": true
    }
  },
  {
    "gsub": {
      "field": "message",
      "pattern": "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b",
      "replacement": "possible.email.redacted=XXXXXXXX@XXXX.XXX",
      "ignore_missing": true,
      "if": "ctx.debug_should_mask == 'true'",
      "ignore_failure": true
    }
  },
  {
    "gsub": {
      "field": "message",
      "pattern": "\\b(\\+?55\\s?)?(?:\\(?\\d{2}\\)?[-\\s]?)?\\d{4,5}[-\\s]?\\d{4}\\b",
      "replacement": "possible.tel.redacted=+XX(XX)XXXXX-XXXX",
      "ignore_missing": true,
      "if": "ctx.debug_should_mask == 'true'",
      "ignore_failure": true
    }
  }
]
```
## Ingest Pipeline - Máscara de Dados Sensíveis e Debug

Este pipeline de ingestão do Elasticsearch realiza várias operações de manipulação e mascaramento de dados em logs, baseado em condições específicas. Ele tem como objetivo garantir que dados sensíveis, como CPF, RG, telefone e email, sejam mascarados durante a ingestão, dependendo do ambiente de execução e do serviço que gerou o log.

## Etapas do Pipeline

1. **Definir `debug_service_name` com base no nome do serviço**:
   - A primeira etapa define o campo `debug_service_name` como o nome do serviço (`{{service.name}}`), mas somente se o campo `service.name` for igual a `'poc-demo-logs'`.
   - Isso é feito usando o processador `set`.
   - **Campo de verificação**: `service.name`
   - **Condição**: `ctx.service.name == 'poc-demo-logs'`
   - **Comportamento**: Se a condição for atendida, `debug_service_name` será definido com o valor do campo `service.name`. Caso contrário, a etapa será ignorada.
   - **Ignorar falhas**: Ativado.

   ```json
   {
     "set": {
       "field": "debug_service_name",
       "value": "{{service.name}}",
       "if": "ctx.service.name == 'poc-demo-logs'",
       "ignore_failure": true
     }
   }

2. **Script para definir se os dados sensíveis devem ser mascarados**:

    - Esta etapa utiliza um script para determinar se os dados sensíveis (como CPF, RG, telefone e email) devem ser mascarados.
    - Condições:
        - O campo ``service.environment`` deve ser ``'dev'``.
        - O campo ``service.name`` deve ser ``'poc-demo-logs``'.
    - Se as condições forem atendidas, o campo debug_should_mask será definido como 'true'. Caso contrário, será 'false'.

```json
{
  "script": {
    "source": "if (ctx.service != null && \r\n      ctx.service.environment != null && ctx.service.environment == 'dev' && \r\n      ctx.service.name != null && ctx.service.name == 'poc-demo-logs') {\r\n            \r\n            ctx.debug_should_mask = 'true';\r\n} else {\r\n            ctx.debug_should_mask = 'false';\r\n}",
    "ignore_failure": true
  }
}
```

## GSubs Adicionais que podem melhorar ainda mais a pipeline

- Numero de cartão de crédito 

```json
{
  "gsub": {
    "field": "message",
    "pattern": "\\b(?:\\d{4}[-\\s]?){3}\\d{4}\\b",
    "replacement": "XXXX-XXXX-XXXX-XXXX",
    "if": "ctx.debug_should_mask == 'true'",
    "ignore_missing": true,
    "ignore_failure": true
  }
}
```

- Data de nascimento
``` json
{
  "gsub": {
    "field": "message",
    "pattern": "\\b\\d{2}/\\d{2}/\\d{4}\\b",
    "replacement": "XX/XX/XXXX",
    "if": "ctx.debug_should_mask == 'true'",
    "ignore_missing": true,
    "ignore_failure": true
  }
}
```

- Passaporte
```json
{
  "gsub": {
    "field": "message",
    "pattern": "\\b[A-Z]{2}\\d{6}\\b",
    "replacement": "PASSPORTE_MASCARADO",
    "if": "ctx.debug_should_mask == 'true'",
    "ignore_missing": true,
    "ignore_failure": true
  }
}
``` 

- Conta bancaria
```json
{
  "gsub": {
    "field": "message",
    "pattern": "\\b\\d{4,6}-\\d{1,2}\\b",
    "replacement": "CONTA_MASCARADA",
    "if": "ctx.debug_should_mask == 'true'",
    "ignore_missing": true,
    "ignore_failure": true
  }
}
```

- Chave Pix
```json
{
  "gsub": {
    "field": "message",
    "pattern": "\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b",
    "replacement": "CHAVE_PIX_MASCARADA",
    "if": "ctx.debug_should_mask == 'true'",
    "ignore_missing": true,
    "ignore_failure": true
  }
}
```

Token de Autenticação
```json
{
  "gsub": {
    "field": "message",
    "pattern": "\\bBearer\\s+[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*\\b",
    "replacement": "TOKEN_MASCARADO",
    "if": "ctx.debug_should_mask == 'true'",
    "ignore_missing": true,
    "ignore_failure": true
  }
}
```

## Requisitos

- Desenvolvido em **Java 22**
- Dependencias gerenciadas por **Maven** 3.9+ 
- **APM Agent em modo attached definido no pom.xml** 1.52.0 ver classe ``DemoApplication.java`` linha ``ElasticApmAttacher.attach();``
- **Elasticsearch** (local ou instância externa)
- **Kibana** (local ou instância externa)
- **Elastic APM Server** (local ou instância externa)
- **Docker** (opcional, para rodar o Elasticsearch em um contêiner)

## Configuração do Ambiente

### Elasticsearch Local com Docker (opcional)

Se você deseja rodar o Elasticsearch localmente usando Docker, execute o seguinte comando:

```bash
docker run -d -p 9200:9200 -e "discovery.type=single-node" elasticsearch:8.9.2

