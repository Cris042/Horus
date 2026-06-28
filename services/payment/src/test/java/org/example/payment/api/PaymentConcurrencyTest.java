package org.example.payment.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Concorrência no saldo da carteira (T-108): valida que débitos simultâneos sobre a mesma
 * carteira são serializados pelo lock pessimista — sem *lost update* nem saldo negativo.
 *
 * <p>Sem o lock, estes testes falhariam de forma intermitente (saldo final &gt; 0 por updates
 * perdidos, ou múltiplos débitos passando além do saldo).
 */
@QuarkusTest
class PaymentConcurrencyTest {

    private int abrirCarteira(double saldoInicial) {
        return given().contentType("application/json")
                .body("{\"titularId\":\"conc\",\"saldoInicial\":" + saldoInicial + "}")
                .when().post("/carteiras")
                .then().statusCode(201)
                .extract().path("id");
    }

    private int debitar(int carteiraId, double valor) {
        return given().contentType("application/json")
                .body("{\"tipo\":\"SAIDA\",\"valor\":" + valor + ",\"descricao\":\"d\"}")
                .when().post("/carteiras/{id}/movimentacoes", carteiraId)
                .then().extract().statusCode();
    }

    @Test
    void debitosConcorrentesNaoPerdemAtualizacao() throws Exception {
        int n = 20;
        double valor = 5.00;
        int carteiraId = abrirCarteira(n * valor); // saldo exato para os n débitos

        runConcurrently(n, () -> debitar(carteiraId, valor));

        // Todos os n débitos devem ter sido aplicados → saldo final exatamente 0.
        given().when().get("/carteiras/{id}", carteiraId)
                .then().statusCode(200).body("saldo", is(0.00f));
    }

    @Test
    void naoPermiteSaldoNegativoSobConcorrencia() throws Exception {
        int n = 20;
        double valor = 5.00;
        int carteiraId = abrirCarteira(valor); // saldo só para UM débito

        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger conflitos = new AtomicInteger();
        runConcurrently(n, () -> {
            int code = debitar(carteiraId, valor);
            if (code == 201) {
                sucessos.incrementAndGet();
            } else if (code == 409) {
                conflitos.incrementAndGet();
            }
            return code;
        });

        assertEquals(1, sucessos.get(), "apenas um débito pode ter sucesso");
        assertEquals(n - 1, conflitos.get(), "os demais devem ser rejeitados por saldo insuficiente");
        given().when().get("/carteiras/{id}", carteiraId)
                .then().statusCode(200).body("saldo", is(0.00f));
    }

    private interface Task {
        int run();
    }

    /** Dispara {@code n} tarefas em paralelo, soltas ao mesmo tempo, e espera todas terminarem. */
    private void runConcurrently(int n, Task task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(n);
        try {
            CountDownLatch start = new CountDownLatch(1);
            Future<?>[] futures = new Future<?>[n];
            for (int i = 0; i < n; i++) {
                futures[i] = pool.submit(() -> {
                    start.await();
                    return task.run();
                });
            }
            start.countDown(); // libera todas de uma vez (máxima contenção)
            for (Future<?> f : futures) {
                f.get();
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
