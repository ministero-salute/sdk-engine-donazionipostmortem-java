/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.models;

import it.mds.sdk.gestoreesiti.modelli.EsitiValidazione;
import it.mds.sdk.rest.persistence.entity.RisultatoValidazione;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RisultatoValidazioneDpm extends RisultatoValidazione {

    private Boolean controlloFirma;
    private String idUpload;

    public RisultatoValidazioneDpm(Boolean isValidato, EsitiValidazione esitiValidazione, String idRun, String idClient, Boolean controlloFirma, String idUpload) {
        super(isValidato, esitiValidazione, idRun, idClient);
        this.controlloFirma = controlloFirma;
        this.idUpload = idUpload;
    }

    public RisultatoValidazioneDpm(Boolean isValidato, EsitiValidazione esitiValidazione, String idRun, String idClient) {
        super(isValidato, esitiValidazione, idRun, idClient);
    }
}
