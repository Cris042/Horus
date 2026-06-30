package org.example.horus.api;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.alert.AlertModel.AlertRequest;
import org.example.horus.alert.AlertModel.AlertResult;
import org.example.horus.alert.AlertModel.ChannelResult;
import org.example.horus.alert.AlertService;

import java.util.List;

/** API de alertas do Horus (T-703, RF-H-013). */
@Path("/horus/alerts")
@Produces(MediaType.APPLICATION_JSON)
public class HorusAlertResource {

    private final AlertService alerts;

    public HorusAlertResource(AlertService alerts) {
        this.alerts = alerts;
    }

    /** Levanta um alerta: resume via IA e dispara aos canais habilitados. */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public AlertResult raise(AlertRequest request) {
        if (request == null
                || ((request.title() == null || request.title().isBlank())
                && (request.details() == null || request.details().isBlank()))) {
            throw new BadRequestException("informe ao menos title ou details");
        }
        return alerts.raise(request);
    }

    /** Lista os canais de alerta e seu estado de habilitação. */
    @GET
    @Path("/channels")
    public List<ChannelResult> channels() {
        return alerts.channelStates();
    }
}
