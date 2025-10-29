package com.example.Primeiro_Projeto.config;

public class LogMessages {

    public static final String RESOURCE_LIST_ALL = "Listando todos os {}";
    public static final String RESOURCE_FIND_BY_FIELD = "Buscando {} por {}";
    public static final String RESOURCE_UPDATE = "Atualizando {} por {}";
    public static final String RESOURCE_CREATE = "Criando {} ";
    public static final String RESOURCE_DELETE = "Deletando {} {}";

    public static final String RESOURCE_NOT_FOUND = "{} não encontrado com {} '{}' não encontrado";

    public static final String FIELD_UPDATE = "{} atualizado para {}";
    public static final String FIELD_CONFLICT = "{} {} já está em uso";

    public static final String CACHE_CLEANING = "Limpando cache para operação: {}";
    public static final String CACHE_SAVED = "Adicionado ao cache";
    public static final String DATABASE_QUERY = "Executando consulta no banco de dados";

    public static final String OPERATION_SUCCESS = "Operação realizada com sucesso: {}";
    public static final String OPERATION_FAILED = "Falha na operaçaõ: {}";

    private LogMessages() {}
}
