/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.models;

import it.mds.sdk.gestoreesiti.modelli.EsitiValidazione;
import it.mds.sdk.rest.persistence.entity.RisultatoValidazione;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DpmRisultatoValidazione extends RisultatoValidazione {

    private String xmlOutputPath;

    public DpmRisultatoValidazione(Boolean isValidato, EsitiValidazione esitiValidazione, String idRun, String idClient, String xmlOutputPath) {
        super(isValidato, esitiValidazione, idRun, idClient);
        this.xmlOutputPath = xmlOutputPath;
    }

    public DpmRisultatoValidazione(Boolean isValidato, EsitiValidazione esitiValidazione, String idRun, String idClient) {
        super(isValidato, esitiValidazione, idRun, idClient);
    }

    public void setXmlOutputPath(String xmlOutputPath) {
        this.xmlOutputPath = xmlOutputPath;
    }

    public String getXmlOutputPath() {
        return xmlOutputPath;
    }
}
