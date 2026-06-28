package org.example.horus.security;

/**
 * Capacidades protegidas do Horus (T-704, RNF-H-010) — as "linhas" da matriz de
 * permissões de {@code docs/ROLES.md} §4, agrupadas no nível em que os endpoints
 * são de fato segmentados nesta fatia.
 *
 * <p>O escopo por serviço do papel {@code DEVELOPER} (🟡 "somente seus serviços")
 * fica para uma fatia posterior; aqui a granularidade é por grupo de endpoint.
 */
public enum Capability {

    /** Ver ciclo de vida de request/query, logs de erro e o painel (RF-H-001/002/003/012). */
    VIEW_TELEMETRY,

    /** Resumo, explicação, RCA e consulta em linguagem natural por IA (RF-H-005/006/007/010). */
    AI_INSIGHTS,

    /** Configurar ingestão/backends/retenção, alertas e observabilidade do cache de IA. */
    CONFIGURE_PLATFORM,

    /** Gerenciar usuários, RBAC e chaves de IA (exclusivo do Platform Admin). */
    MANAGE_USERS
}
