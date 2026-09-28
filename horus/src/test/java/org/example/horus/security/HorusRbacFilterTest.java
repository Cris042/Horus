package org.example.horus.security;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.QuarkusTestProfile;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/** Verifica o RBAC do Horus (T-704) com o filtro ligado via perfil de teste. */
@QuarkusTest
@TestProfile(HorusRbacFilterTest.RbacOn.class)
class HorusRbacFilterTest {

    /** Perfil de teste que liga o RBAC (default é desligado). */
    public static class RbacOn implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("horus.rbac.enabled", "true", "horus.rbac.mode", "header");
        }
    }

    @Test
    void publicEndpoint_isReachableWithoutRole() {
        given().when().get("/horus/info").then().statusCode(200);
    }

    @Test
    void protectedEndpoint_withoutRole_is401() {
        given().when().get("/horus/panel/overview").then().statusCode(401);
    }

    @Test
    void protectedEndpoint_withInvalidRole_is401() {
        given().header(HorusRbacFilter.ROLE_HEADER, "bogus")
                .when().get("/horus/panel/overview").then().statusCode(401);
    }

    @Test
    void viewTelemetry_allowedForAuditor() {
        given().header(HorusRbacFilter.ROLE_HEADER, "AUDITOR")
                .when().get("/horus/panel/overview")
                .then().statusCode(200).body("service", equalTo("horus"));
    }

    @Test
    void cacheConfig_deniedForAuditor() {
        given().header(HorusRbacFilter.ROLE_HEADER, "AUDITOR")
                .when().get("/horus/ai/cache/stats").then().statusCode(403);
    }

    @Test
    void cacheConfig_allowedForAdmin() {
        given().header(HorusRbacFilter.ROLE_HEADER, "platform-admin")
                .when().get("/horus/ai/cache/stats").then().statusCode(200);
    }
}
