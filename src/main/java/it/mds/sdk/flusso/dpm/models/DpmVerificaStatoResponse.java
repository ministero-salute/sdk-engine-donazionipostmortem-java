/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.models;

import it.mds.sdk.connettoremds.dpm.webservice.bean.DettaglioType;
import it.mds.sdk.connettoremds.dpm.webservice.bean.EsitoType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.sql.Timestamp;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DpmVerificaStatoResponse {

    protected EsitoServizioEnum esitoServizio;
    protected String descrizioneEsitoServizio;
    protected String numeroAtto;
    protected String identificativoSoggettoAlimentante;
    protected EsitoType tipoEsito;
    protected DettaglioType dettagli;
    protected String idRun;
    protected Timestamp dataEmissioneRicevuta;

    @Builder(setterPrefix = "with")
    public DpmVerificaStatoResponse(EsitoServizioEnum esitoServizio, String descrizioneEsitoServizio, String numeroAtto, String identificativoSoggettoAlimentante, EsitoType tipoEsito, DettaglioType dettagli, String idRun,Timestamp dataEmissioneRicevuta) {
        this.esitoServizio = esitoServizio;
        this.descrizioneEsitoServizio = descrizioneEsitoServizio;
        this.numeroAtto = numeroAtto;
        this.identificativoSoggettoAlimentante = identificativoSoggettoAlimentante;
        this.tipoEsito = tipoEsito;
        this.dettagli = dettagli;
        this.idRun = idRun;
        this.dataEmissioneRicevuta = dataEmissioneRicevuta;
    }
}
