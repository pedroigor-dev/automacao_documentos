# Automação de Documentos

Este projeto é uma aplicação backend desenvolvida em Java utilizando o framework Spring Boot, com foco em automação de processamento e classificação de documentos enviados pelo usuário. O projeto foi criado para facilitar o recebimento, extração de texto, classificação automática e armazenamento organizado de diversos tipos de documentos.

## Principais Funcionalidades
- **Upload de Arquivos**: Endpoint para envio de arquivos via API REST.
- **Extração de Texto**: Utiliza Apache Tika para extrair o conteúdo textual dos documentos enviados.
- **Classificação Automática**: Identifica o tipo de documento (currículo, contrato, nota fiscal, parecer técnico, relatório, entre outros) com base no texto extraído.
- **Organização e Armazenamento**: Salva os arquivos classificados em pastas específicas conforme o tipo identificado.
- **Resumo Inteligente**: Gera um resumo do texto extraído para facilitar consultas rápidas.

## Tecnologias e Bibliotecas Utilizadas
- **Java 17**
- **Spring Boot 4**
- **Spring Web MVC**
- **Apache Tika** (via Spring AI Tika Document Reader)
- **Lombok**

## Estrutura de Pastas
- `src/main/java/com/automacao/automacaopt1/` - Código fonte principal
- `cloud/` - Pastas de armazenamento dos documentos classificados
- `src/main/resources/` - Configurações e recursos estáticos

## Endpoints
- `POST /api/arquivos/upload`  
  Recebe um arquivo via multipart/form-data, processa, extrai o texto, classifica e armazena o documento.

### Exemplo de Requisição
```http
POST /api/arquivos/upload
Content-Type: multipart/form-data
Body: file=<seu_arquivo>
```

### Resposta
```json
{
  "nomeOriginal": "documento.pdf",
  "nomeFinal": "CONTRATO_20260228_153000.pdf",
  "status": "Processado e salvo",
  "tipoDocumento": "CONTRATO",
  "textoExtraido": "Resumo do texto extraído..."
}
```

## Como Executar
1. Certifique-se de ter o Java 17 instalado.
2. Execute o comando abaixo na raiz do projeto:
   ```bash
   ./mvnw spring-boot:run
   ```
3. A API estará disponível em `http://localhost:8080`.

## Configurações Adicionais
- O CORS está habilitado para permitir requisições de aplicações frontend rodando em `localhost:3000`.
- Os arquivos enviados são organizados automaticamente nas subpastas de `cloud/` conforme o tipo identificado.

## Sobre o Projeto
Este projeto foi desenvolvido para automatizar o fluxo de recebimento e classificação de documentos em ambientes corporativos, reduzindo o trabalho manual e aumentando a eficiência no processamento de arquivos.

---

**Licença:** MIT
