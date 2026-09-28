package org.example.horus.security;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.jwt.build.Jwt;
import org.example.horus.query.QueryModel.TraceSearch;
import org.example.horus.query.TraceQueryPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RBAC com autenticação real (T-1007): JWT assinado, papéis na claim {@code groups}, escopo por
 * serviço do DEVELOPER. O par RSA é gerado a cada execução — nenhuma chave vai para o repositório.
 */
@QuarkusTest
@TestProfile(HorusJwtRbacTest.JwtOn.class)
class HorusJwtRbacTest {

    static final String ISSUER = "https://idp.horus.test";
    static final Path PRIVATE_KEY;
    static final Path PUBLIC_KEY;

    static {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair pair = gen.generateKeyPair();
            Path dir = Files.createTempDirectory("horus-jwt");
            PRIVATE_KEY = dir.resolve("private.pem");
            PUBLIC_KEY = dir.resolve("public.pem");
            Files.writeString(PRIVATE_KEY, pem("PRIVATE KEY", pair.getPrivate().getEncoded()));
            Files.writeString(PUBLIC_KEY, pem("PUBLIC KEY", pair.getPublic().getEncoded()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String pem(String type, byte[] der) {
        return "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der)
                + "\n-----END " + type + "-----\n";
    }

    public static class JwtOn implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "horus.rbac.enabled", "true",
                    "horus.rbac.mode", "jwt",
                    "mp.jwt.verify.publickey.location", PUBLIC_KEY.toUri().toString(),
                    "mp.jwt.verify.issuer", ISSUER,
                    "smallrye.jwt.sign.key.location", PRIVATE_KEY.toUri().toString());
        }
    }

    @InjectMock
    TraceQueryPort traces;

    private static String token(Set<String> groups, List<String> services) {
        var builder = Jwt.issuer(ISSUER).upn("ana@medrec.test").groups(groups);
        if (services != null) {
            builder.claim(HorusRbacFilter.SERVICES_CLAIM, services);
        }
        return builder.sign();
    }

    @Test
    void noToken_is401_publicStillOpen() {
        given().when().get("/horus/traces").then().statusCode(401);
        given().when().get("/q/health/ready").then().statusCode(200);
    }

    @Test
    void forgedOrForeignToken_is401() {
        given().auth().oauth2("eyJhbGciOiJub25lIn0.eyJncm91cHMiOlsiUExBVEZPUk1fQURNSU4iXX0.")
                .when().get("/horus/traces").then().statusCode(401);
    }

    @Test
    void headerRole_isIgnoredInJwtMode() {
        given().header(HorusRbacFilter.ROLE_HEADER, "PLATFORM_ADMIN")
                .when().get("/horus/traces").then().statusCode(401);
    }

    @Test
    void auditor_canViewTelemetry_butNotConfigure() {
        when(traces.searchTraces(any())).thenReturn(List.of());
        String auditor = token(Set.of("AUDITOR"), null);
        given().auth().oauth2(auditor).when().get("/horus/traces").then().statusCode(200);
        given().auth().oauth2(auditor).when().get("/horus/ai/cache/stats").then().statusCode(403);
    }

    @Test
    void highestPrivilegeGroup_wins() {
        given().auth().oauth2(token(Set.of("AUDITOR", "PLATFORM_ADMIN", "unrelated"), null))
                .when().get("/horus/ai/cache/stats").then().statusCode(200);
    }

    @Test
    void tokenWithoutHorusRole_is401() {
        given().auth().oauth2(token(Set.of("billing"), null))
                .when().get("/horus/traces").then().statusCode(401);
    }

    @Test
    void developer_isScopedToTheirServices() {
        when(traces.searchTraces(any())).thenReturn(List.of());
        when(traces.listServices()).thenReturn(List.of("invoice-service", "payment-service", "prontuario-service"));
        String dev = token(Set.of("DEVELOPER"), List.of("payment-service"));

        given().auth().oauth2(dev).when().get("/horus/traces/services")
                .then().statusCode(200).body("$", contains("payment-service"));
        given().auth().oauth2(dev).when().get("/horus/traces?service=invoice-service").then().statusCode(403);
        given().auth().oauth2(dev).when().get("/horus/traces").then().statusCode(200); // assume o único serviço

        ArgumentCaptor<TraceSearch> captor = ArgumentCaptor.forClass(TraceSearch.class);
        verify(traces).searchTraces(captor.capture());
        assertEquals("payment-service", captor.getValue().service());
    }

    @Test
    void developerWithoutServicesClaim_isNotRestricted() {
        when(traces.searchTraces(any())).thenReturn(List.of());
        given().auth().oauth2(token(Set.of("DEVELOPER"), null))
                .when().get("/horus/traces?service=invoice-service").then().statusCode(200);
    }
}
