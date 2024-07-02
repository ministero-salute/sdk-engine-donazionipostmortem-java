/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.inforun;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DpmInfoRun {
    private String idClient;
    private String idRun;
    private String api;
    private String codiceStatoRun;
    private String descrizioneCodiceStatoRun;
    private String timestamp;
    private String version;
    private String utenza;
    private String identificativoSoggettoAlimentante;
    private String tipoAtto;
    private String numeroAtto;
    private String tipoEsitoMds;
    private String dataRicevutaMds;
    private String nomeFile;

    @Builder(setterPrefix = "with")
    public DpmInfoRun(@JsonProperty("idClient") String idClient,
                      @JsonProperty("idClient") String idRun,
                      @JsonProperty("api") String api,
                      @JsonProperty("codiceStatoRun") String codiceStatoRun,
                      @JsonProperty("descrizioneCodiceStatoRun") String descrizioneCodiceStatoRun,
                      @JsonProperty("timestamp") String timestamp,
                      @JsonProperty("version") String version,
                      @JsonProperty("utenza") String utenza,
                      @JsonProperty("identificativoSoggettoAlimentante") String identificativoSoggettoAlimentante,
                      @JsonProperty("tipoAtto") String tipoAtto,
                      @JsonProperty("numeroAtto") String numeroAtto,
                      @JsonProperty("tipoEsitoMds") String tipoEsitoMds,
                      @JsonProperty("dataRicevutaMds")  String dataRicevutaMds,
                      @JsonProperty("nomeFile") String nomeFile) {
        this.idClient = idClient;
        this.idRun = idRun;
        this.api = api;
        this.codiceStatoRun = codiceStatoRun;
        this.descrizioneCodiceStatoRun = descrizioneCodiceStatoRun;
        this.timestamp = timestamp;
        this.version = version;
        this.utenza = utenza;
        this.identificativoSoggettoAlimentante = identificativoSoggettoAlimentante;
        this.tipoAtto = tipoAtto;
        this.numeroAtto = numeroAtto;
        this.tipoEsitoMds = tipoEsitoMds;
        this.dataRicevutaMds = dataRicevutaMds;
        this.nomeFile = nomeFile;
    }
}
