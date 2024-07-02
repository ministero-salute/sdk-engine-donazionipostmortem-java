/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.service;

import it.mds.sdk.connettoremds.ConnettoreMds;
import it.mds.sdk.connettoremds.dpm.webservice.bean.Dpm;
import it.mds.sdk.connettoremds.dpm.webservice.bean.SoggettoAlimentanteType;
import it.mds.sdk.connettoremds.dpm.webservice.bean.verifica.stato.DpmMdsResponse;
import it.mds.sdk.connettoremds.exception.ConnettoreMdsException;
import it.mds.sdk.connettoremds.parser.xml.ParserFirma;
import it.mds.sdk.flusso.dpm.models.DpmVerificaStatoResponse;
import it.mds.sdk.flusso.dpm.models.EsitoServizioEnum;
import it.mds.sdk.flusso.dpm.models.InvioMdsResponse;
import it.mds.sdk.flusso.dpm.parser.regole.RecordDtoDpm;
import it.mds.sdk.flusso.dpm.parser.xml.ParserXml;
import it.mds.sdk.gestoreesiti.GestoreRunLog;
import it.mds.sdk.gestoreesiti.modelli.Esito;
import it.mds.sdk.gestoreesiti.modelli.InfoRun;
import it.mds.sdk.gestoreesiti.modelli.ModalitaOperativa;
import it.mds.sdk.gestoreesiti.modelli.StatoRun;
import it.mds.sdk.gestorefile.exception.XSDNonSupportedException;
import it.mds.sdk.libreriaregole.dtos.RecordDtoGenerico;
import it.mds.sdk.libreriaregole.exception.ValidazioneImpossibileException;
import it.mds.sdk.libreriaregole.gestorevalidazione.GestoreValidazione;
import it.mds.sdk.libreriaregole.parser.ParserRegole;
import it.mds.sdk.libreriaregole.parser.ParserTracciato;
import it.mds.sdk.libreriaregole.regole.beans.RegoleFlusso;
import it.mds.sdk.libreriaregole.tracciato.TracciatoSplitter;
import it.mds.sdk.libreriaregole.validator.ValidationEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("flussoDpmService")
@Slf4j
public class FlussoDpmService {

    private ParserRegole parserRegole;
    private ParserTracciato parserTracciato;
    private ValidationEngine validationEngine;
    private TracciatoSplitter<RecordDtoDpm> tracciatoSplitter;
    private ParserXml parserXml;
    private ConnettoreMds connettoreMds;
    private ParserFirma parserFirma;

    @Autowired
    public FlussoDpmService(@Qualifier("parserRegoleDpm") final ParserRegole parserRegole,
                            @Qualifier("parserTracciato") final ParserTracciato parserTracciato,
                            @Qualifier("validationEngine") final ValidationEngine validationEngine,
                            @Qualifier("tracciatoSplitter") final TracciatoSplitter<RecordDtoDpm> tracciatoSplitter,
                            @Qualifier("parserXmlDpm") final ParserXml parserXml,
                            @Qualifier("connettoreMds") final ConnettoreMds connettoreMds,
                            @Qualifier("parserFirma") final ParserFirma parserFirma) {
        this.parserRegole = parserRegole;
        this.parserTracciato = parserTracciato;
        this.validationEngine = validationEngine;
        this.tracciatoSplitter = tracciatoSplitter;
        this.parserXml = parserXml;
        this.connettoreMds = connettoreMds;
        this.parserFirma = parserFirma;
    }

    public List<Esito> startValidaRecord(RecordDtoGenerico dtoGenerico, RegoleFlusso regoleFlusso, String idRun, String idClient, ModalitaOperativa modalitaOperativa, GestoreRunLog gestoreRunLog) {

        log.debug("{}.startValidaRecord - dtoGenerico[{}] - regoleFlusso[{}] - idRun[{}]  - idClient[{}]  - modalitaOperativa[{}] - BEGIN",
                this.getClass().getName(), dtoGenerico.toString(), regoleFlusso.toString(), idRun, idClient, modalitaOperativa.toString());

        InfoRun infoRun;
        GestoreValidazione gestoreValidazione;
        List<Esito> esiti = List.of();
        try {
            gestoreValidazione = createGestoreValidazione(validationEngine, parserRegole, parserTracciato);
            log.debug("startValidaRecord :start Validazione");
            infoRun = gestoreRunLog.getRun(idRun);
            esiti = gestoreValidazione.gestioneValidazioneRecord(dtoGenerico, regoleFlusso, idRun);
        } catch (ValidazioneImpossibileException vie) {
            infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.IN_ELABORAZIONE);
            infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.KO_GESTIONE_VALIDAZIONE);
            log.error("Errore validazione idrun {}", idRun, vie);
            infoRun.setDescrizioneStatoEsecuzione(vie.getMessage());
            gestoreRunLog.updateRun(infoRun);
        } catch (Throwable t) {
            infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.IN_ELABORAZIONE);
            infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.KO_GENERICO);
            log.error("Errore generico idRun {}", idRun, t);
            infoRun.setDescrizioneStatoEsecuzione(t.getMessage());
            gestoreRunLog.updateRun(infoRun);
            return null;
        }
        List<Esito> esitiKo = esiti.stream().filter(e -> !e.isValoreEsito()).collect(Collectors.toList());
        boolean isOK = esitiKo.isEmpty();

        if (isOK) {
            log.debug("record ok genero XML output");
            RecordDtoDpm recordDtoDpm = getRecordDpmFromGenerico(dtoGenerico);
            List<RecordDtoDpm> recordList = new ArrayList<>();
            recordList.add(recordDtoDpm);
            try {
                tracciatoSplitter.dividiTracciato(recordList, idRun);
            } catch (XSDNonSupportedException e) {
                log.error("XSD non validato. ", e);
                infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.IN_ELABORAZIONE);
                infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.KO_VALIDAZIONE_SDK);
                infoRun.setDescrizioneStatoEsecuzione(e.getMessage());
                gestoreRunLog.updateRun(infoRun);
            } catch (Throwable t) {
                infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.IN_ELABORAZIONE);
                infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.KO_GENERICO);
                log.error("Errore generico idRun {}", idRun, t);
                infoRun.setDescrizioneStatoEsecuzione(t.getMessage());
                gestoreRunLog.updateRun(infoRun);
            }
        } else {
            infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.IN_ELABORAZIONE);
            infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.KO_GESTIONE_VALIDAZIONE);
            log.error("Errore validazione idrun {}", idRun);
            infoRun.setDescrizioneStatoEsecuzione("KO Gestione validazione perchè le BR non sono rispettate.");
            gestoreRunLog.updateRun(infoRun);
        }
        return esitiKo;
    }

    public RecordDtoDpm getRecordDpmFromGenerico(RecordDtoGenerico dtoGenerico) {
        return (RecordDtoDpm) dtoGenerico;
    }

    public GestoreValidazione createGestoreValidazione(ValidationEngine validationEngine, ParserRegole parserRegole, ParserTracciato parserTracciato) {
        return new GestoreValidazione(validationEngine, parserRegole, parserTracciato);
    }

    public InvioMdsResponse startInvioMdsXmlRecord(File xmlToMds, RegoleFlusso regoleFlusso, String idRun, String idClient, ModalitaOperativa modalitaOperativa, GestoreRunLog gestoreRunLog) {
        log.debug("{}.startInvioMdsXmlRecord - xmlToMds[{}] - regoleFlusso[{}] - idRun[{}]  - idClient[{}]  - modalitaOperativa[{}] - BEGIN",
                this.getClass().getName(), xmlToMds.getAbsolutePath(), regoleFlusso.toString(), idRun, idClient, modalitaOperativa.toString());
        log.debug("check firma");
        InfoRun infoRun;

        String xmlToMdsOk;
        try {
            xmlToMdsOk = parserFirma.checkFirmaXmlP7MConn(xmlToMds);
        } catch (it.mds.sdk.connettoremds.parser.xml.exception.ValidazioneFirmaException e) {
            log.error("{}.startInvioMdsXmlRecord - xmlToMds[{}] - regoleFlusso[{}] - idRun[{}]  - idClient[{}]  - modalitaOperativa[{}] - BEGIN",
                    this.getClass().getName(), xmlToMds.getAbsolutePath(), regoleFlusso, idRun, idClient, modalitaOperativa, e);
            infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.IN_ELABORAZIONE);
            infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.KO_INVIO_MINISTERO);
            infoRun.setDescrizioneStatoEsecuzione(e.getMessage());
            gestoreRunLog.updateRun(infoRun);
            return InvioMdsResponse.builder().withEsitoServizio(EsitoServizioEnum.KO).withDescrizioneEsitoServizio("Errore validazione firma").build();
        }

        log.debug("Parse Xml Input");

        Dpm dpm = parserXml.parseXmlDpm(xmlToMdsOk);
        log.debug("Convert XML to DTO");
        RecordDtoDpm recordDtoDpm = parserXml.convertXMLToDto(dpm);

        infoRun = gestoreRunLog.getRun(idRun);
        infoRun.setIdentificativoSoggettoAlimentante(recordDtoDpm.getIdSoggettoAlimentante());
        gestoreRunLog.updateRun(infoRun);

        GestoreValidazione gestoreValidazione;
        List<Esito> esiti = List.of();
        try {
            gestoreValidazione = createGestoreValidazione(validationEngine, parserRegole, parserTracciato);
            log.debug("Start Validazione xml");
            esiti = gestoreValidazione.gestioneValidazioneRecord(recordDtoDpm, regoleFlusso, idRun);
        } catch (ValidazioneImpossibileException vie) {
            infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.IN_ELABORAZIONE);
            infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.KO_GESTIONE_VALIDAZIONE);
            log.error("Errore validazione idrun {}", idRun, vie);
            infoRun.setDescrizioneStatoEsecuzione(vie.getMessage());
            gestoreRunLog.updateRun(infoRun);
        } catch (Throwable t) {
            infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.IN_ELABORAZIONE);
            infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.KO_GENERICO);
            log.error("Errore generico idRun {}", idRun, t);
            infoRun.setDescrizioneStatoEsecuzione(t.getMessage());
            gestoreRunLog.updateRun(infoRun);
            return null;
        }
        List<Esito> kos = esiti.stream().filter(e -> !e.isValoreEsito()).collect(Collectors.toList());

        if (kos.isEmpty()) {
            log.debug("Invio tracciato al ministero");
            try {
                DpmMdsResponse dpmMdsResponse = connettoreMds.invioTracciatoDonazionePostMortem(xmlToMds, dpm.getIdentificativoSoggettoAlimentante(), dpm.getSoggettoAlimentante(), dpm.getTipoAtto(), idRun);
                log.debug("Fine chiamata invio DPM Mds");
                return InvioMdsResponse.builder()
                        .withEsitoServizio(EsitoServizioEnum.OK)
                        .withDettagli(dpmMdsResponse.getDettagli())
                        .withIdentificativoSoggettoAlimentante(dpmMdsResponse.getIdentificativoSoggettoAlimentante())
                        .withDataEmissioneRicevuta(dpmMdsResponse.getDataEmissioneRicevuta())
                        .withTipoEsito(dpmMdsResponse.getTipoEsito())
                        .build();

            } catch (ConnettoreMdsException e) {

                log.error("{}.startInvioMdsXmlRecord - xmlToMds[{}] - regoleFlusso[{}] - idRun[{}]  - idClient[{}]  - modalitaOperativa[{}] - BEGIN",
                        this.getClass().getName(), xmlToMds.getAbsolutePath(), regoleFlusso, idRun, idClient, modalitaOperativa, e);
                infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.IN_ELABORAZIONE);
                infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.KO_INVIO_MINISTERO);
                infoRun.setDescrizioneStatoEsecuzione(e.getMessage());
                gestoreRunLog.updateRun(infoRun);
                return InvioMdsResponse.builder().withEsitoServizio(EsitoServizioEnum.KO).withDescrizioneEsitoServizio(e.getMessage()).build();
            } catch (Throwable t) {
                infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.IN_ELABORAZIONE);
                infoRun = gestoreRunLog.cambiaStatoRun(idRun, StatoRun.KO_GENERICO);
                log.error("Errore generico idRun {}", idRun, t);
                infoRun.setDescrizioneStatoEsecuzione(t.getMessage());
                gestoreRunLog.updateRun(infoRun);
                return null;
            }

        } else {
            return InvioMdsResponse.builder()
                    .withEsitoServizio(EsitoServizioEnum.KO)
                    .withDescrizioneEsitoServizio("Validazione XML fallita")
                    .withEsitiValidazione(kos)
                    .build();
        }

    }

    public DpmVerificaStatoResponse dpmVerificaStato(String identificativoSoggettoAlimentante, String regioneSoggettoAlimentante, String cap, String codiceSoggettoAlimentante, String indirizzo, String idRun) {
        log.debug("{}.dpmVerificaStato - identificativoSoggettoAlimentante[{}] - regioneSoggettoAlimentante[{}] - cap[{}] - codiceSoggettoAlimentante[{}] - indirizzo[{}] - BEGIN",
                this.getClass().getName(), identificativoSoggettoAlimentante, regioneSoggettoAlimentante, cap, codiceSoggettoAlimentante, indirizzo);
        SoggettoAlimentanteType soggettoAlimentanteType = createSoggettoAlimentanteType();
        soggettoAlimentanteType.setCodiceSoggettoAlimentante(codiceSoggettoAlimentante);
        soggettoAlimentanteType.setRegioneSoggettoAlimentante(regioneSoggettoAlimentante);
        soggettoAlimentanteType.setCap(cap);
        soggettoAlimentanteType.setIndirizzo(indirizzo);
        soggettoAlimentanteType.setRegioneSoggettoAlimentante(regioneSoggettoAlimentante);
        log.debug("Chiamata Mds verificaElaborazioneDonazionePostMortem");
        try {
            DpmMdsResponse dpmMdsResponse = connettoreMds.verificaElaborazioneDonazionePostMortem(identificativoSoggettoAlimentante, soggettoAlimentanteType, idRun);
            return DpmVerificaStatoResponse.builder()
                    .withEsitoServizio(EsitoServizioEnum.OK)
                    .withNumeroAtto(dpmMdsResponse.getNumeroAtto())
                    .withDettagli(dpmMdsResponse.getDettagli())
                    .withIdentificativoSoggettoAlimentante(dpmMdsResponse.getIdentificativoSoggettoAlimentante())
                    .withTipoEsito(dpmMdsResponse.getTipoEsito())
                    .withDataEmissioneRicevuta(dpmMdsResponse.getDataEmissioneRicevuta())
                    .withIdRun(idRun)
                    .build();
        } catch (ConnettoreMdsException e) {
            log.debug("{}.dpmVerificaStato - identificativoSoggettoAlimentante[{}] - regioneSoggettoAlimentante[{}] - cap[{}] - codiceSoggettoAlimentante[{}] - indirizzo[{}] ",
                    this.getClass().getName(), identificativoSoggettoAlimentante, regioneSoggettoAlimentante, cap, codiceSoggettoAlimentante, indirizzo, e);
            return DpmVerificaStatoResponse.builder().withEsitoServizio(EsitoServizioEnum.KO)
                    .withDescrizioneEsitoServizio(e.getMessage()).build();
        }
    }

    public SoggettoAlimentanteType createSoggettoAlimentanteType() {
        return new SoggettoAlimentanteType();
    }
}
