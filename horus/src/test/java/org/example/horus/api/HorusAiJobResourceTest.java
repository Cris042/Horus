package org.example.horus.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.example.horus.ai.context.ContextAssembler;
import org.example.horus.ai.context.PromptContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/** RCA assíncrona (T-1006): 202 + Location, depois o resultado pelo jobId. */
@QuarkusTest
class HorusAiJobResourceTest {

    @InjectMock
    ContextAssembler assembler;

    @Test
    void rcaJob_acceptedThenSucceeds() throws InterruptedException {
        when(assembler.assembleForIncident(eq("abc123"), any(), anyInt()))
                .thenReturn(new PromptContext("# Trace abc123\n", 4, false, List.of("trace")));

        String id = given().contentType("application/json").body("{\"traceId\":\"abc123\"}")
                .when().post("/horus/ai/jobs/rca")
                .then().statusCode(202)
                .header("Location", org.hamcrest.Matchers.containsString("/horus/ai/jobs/"))
                .extract().path("id");
        String status = "PENDING";
        for (int i = 0; i < 100 && !status.equals("SUCCEEDED") && !status.equals("FAILED"); i++) {
            Thread.sleep(50);
            status = given().when().get("/horus/ai/jobs/" + id).then().statusCode(200).extract().path("status");
        }
        assertEquals("SUCCEEDED", status);
        given().when().get("/horus/ai/jobs/" + id).then()
                .body("kind", equalTo("rca"))
                .body("result.traceId", equalTo("abc123"))
                .body("result.modelId", equalTo("claude-opus-5"));
    }

    @Test
    void rcaJob_withoutTraceId_returns400() {
        given().contentType("application/json").body("{}").when().post("/horus/ai/jobs/rca").then().statusCode(400);
    }

    @Test
    void unknownJob_returns404() {
        given().when().get("/horus/ai/jobs/nao-existe").then().statusCode(404);
    }
}
