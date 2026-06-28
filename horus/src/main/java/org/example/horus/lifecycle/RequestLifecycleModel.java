package org.example.horus.lifecycle;

import java.util.List;

/** Modelos da API de ciclo de vida de request (T-503, RF-H-001/RF-H-011). */
public final class RequestLifecycleModel {

    private RequestLifecycleModel() {
    }

    public record RequestLifecycle(
            String traceId,
            int spanCount,
            long durationMicros,
            boolean messagingInvolved,
            boolean workerInvolved,
            List<String> services,
            List<WaterfallSpan> spans) {
    }

    public record WaterfallSpan(
            String spanId,
            String parentSpanId,
            String operation,
            String serviceName,
            String kind,
            String category,
            long startTimeMicros,
            long offsetMicros,
            long durationMicros,
            int depth) {
    }
}
