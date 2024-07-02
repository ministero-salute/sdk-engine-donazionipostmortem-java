/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.controller;

import it.mds.sdk.flusso.dpm.models.InvioMdsResponse;
import it.mds.sdk.flusso.dpm.models.InvioRequest;
import it.mds.sdk.flusso.dpm.models.DpmRisultatoValidazione;
import it.mds.sdk.flusso.dpm.models.DpmVerificaStatoResponse;
import it.mds.sdk.flusso.dpm.parser.regole.RecordDtoDpm;
import it.mds.sdk.flusso.dpm.parser.regole.conf.ConfigurazioneFlussoDpm;
import it.mds.sdk.flusso.dpm.service.FlussoDpmService;
import it.mds.sdk.gestoreesiti.GestoreRunLog;
import it.mds.sdk.gestoreesiti.Progressivo;
import it.mds.sdk.gestoreesiti.modelli.*;
import it.mds.sdk.gestorefile.GestoreFile;
import it.mds.sdk.gestorefile.factory.GestoreFileFactory;
import it.mds.sdk.libreriaregole.dtos.RecordDtoGenerico;
import it.mds.sdk.libreriaregole.parser.ParserRegole;
import it.mds.sdk.libreriaregole.parser.ParserTracciato;
import it.mds.sdk.libreriaregole.regole.beans.RegoleFlusso;
import it.mds.sdk.rest.api.controller.ValidazioneController;
import it.mds.sdk.rest.persistence.entity.FlussoRequest;
import it.mds.sdk.rest.persistence.entity.RecordRequest;
import it.mds.sdk.rest.persistence.entity.RisultatoInizioValidazione;
import it.mds.sdk.rest.persistence.entity.RisultatoValidazione;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;

@RestController
@Configuration
@EnableAsync
@Slf4j
public class FlussoDpmControllerRest implements ValidazioneController<RecordDtoDpm> {

    private final ParserRegole parserRegole;
    private final ParserTracciato parserTracciato;
    private final FlussoDpmService flussoDpmService;
    private final ConfigurazioneFlussoDpm conf;
    private static final String FILE_OUTPUT_PREFIX = "DPM_OUTPUT_";
    private static final String API_1 = "1";
    private static final String API_2 = "2";
    private static final String API_3 = "3";
    private final MultiValueMap<String, String> headers;

    @Autowired
    public FlussoDpmControllerRest(@Qualifier("parserRegoleDpm") final ParserRegole parserRegole,
                                   @Qualifier("parserTracciato") final ParserTracciato parserTracciato,
                                   @Qualifier("flussoDpmService") final FlussoDpmService flussoDpmService,
                                   @Qualifier("configurazioneFlussoDpm") ConfigurazioneFlussoDpm conf) {
        this.parserRegole = parserRegole;
        this.parserTracciato = parserTracciato;
        this.flussoDpmService = flussoDpmService;
        this.conf = conf;

        headers = new HttpHeaders();
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("X-Frame-Options", "DENY");
        headers.add("X-XSS-Protection", "1; mode=block");
        headers.add("Content-Security-Policy", "default-src 'self'");

    }

    @Override
    public ResponseEntity<RisultatoInizioValidazione> validaTracciato(FlussoRequest flusso, String nomeFlusso) {
        return null;
    }

    @Override
    @PostMapping("v1/flusso/donazionipostmortem/record")
    public ResponseEntity<RisultatoValidazione> validaRecord(@RequestBody RecordRequest<RecordDtoDpm> recordRequest, String nomeFlussoController) {
        File fileRegole = getFileFromPath(conf.getRules().getPercorso());
        RegoleFlusso regoleFlusso = parserRegole.parseRegole(fileRegole);
        GestoreFile gestoreFile = GestoreFileFactory.getGestoreFile("DEFAULT");
        //TODO : Autowire anche di gestoreRunLog, verificarne la fattibilità
        GestoreRunLog gestoreRunLog = createGestoreRunLog(gestoreFile, Progressivo.creaProgressivo(Progressivo.Fonte.FILE));
        String nomeFlusso = "DPM";
        InfoRun infoRun = gestoreRunLog.creaRunLog(recordRequest.getIdClient(), recordRequest.getModalitaOperativa(), 1, nomeFlusso);
        //aggiungo campi per dpm
        infoRun=gestoreRunLog.getRun(infoRun.getIdRun());
        infoRun.setApi(API_1);
        infoRun.setIdentificativoSoggettoAlimentante(getIdSoggettoAlimentante(recordRequest));
        infoRun.setTimestampCreazione(new Timestamp(System.currentTimeMillis()));
        infoRun.setVersion(getClass().getPackage().getImplementationVersion());
        infoRun.setTipoElaborazione(TipoElaborazione.R);
        gestoreRunLog.updateRun(infoRun);
        List<Esito> kos = flussoDpmService.startValidaRecord(recordRequest.getRecordDto(), regoleFlusso, infoRun.getIdRun(), recordRequest.getIdClient(), recordRequest.getModalitaOperativa(), gestoreRunLog);
        EsitiValidazione esitiValidazione = new EsitiValidazione(infoRun.getIdRun(), kos);
        return new ResponseEntity<>(new DpmRisultatoValidazione(kos.isEmpty(), esitiValidazione, null, recordRequest.getIdClient(),kos.isEmpty() ? conf.getXmlOutput().getPercorso() + FILE_OUTPUT_PREFIX+infoRun.getIdRun() + ".xml" : null), headers, HttpStatus.OK);
    }

    @PostMapping("v1/flusso/donazionipostmortem/record/invio")
    public ResponseEntity<InvioMdsResponse> invioFileMds(@RequestBody InvioRequest invioRequest, String nomeFlussoController){
        String filename =  FilenameUtils.normalize(invioRequest.getNomeFile());
        File xmlToMds = new File(conf.getXmlInput().getPercorso() + filename);
        if (!xmlToMds.exists()) {
            log.error("File da inviare al ministero non trovato : " + filename);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File da inviare non trovato");
        }
        GestoreFile gestoreFile = GestoreFileFactory.getGestoreFile("DEFAULT");
        GestoreRunLog gestoreRunLog = new GestoreRunLog(gestoreFile, Progressivo.creaProgressivo(Progressivo.Fonte.FILE));
        String nomeFlusso = "DPM";
        InfoRun infoRun = gestoreRunLog.creaRunLog(invioRequest.getIdClient(), ModalitaOperativa.P, 1, nomeFlusso);
        //aggiungo campi per dpm
        infoRun=gestoreRunLog.getRun(infoRun.getIdRun());
        infoRun.setApi(API_2);
        infoRun.setTimestampCreazione(new Timestamp(System.currentTimeMillis()));
        infoRun.setVersion(getClass().getPackage().getImplementationVersion());
        infoRun.setFileAssociatiRun(filename);
        infoRun.setTipoElaborazione(TipoElaborazione.R);
        gestoreRunLog.updateRun(infoRun);
        File fileRegole = new File(conf.getRules().getPercorso());
        RegoleFlusso regoleFlusso = parserRegole.parseRegole(fileRegole);
        InvioMdsResponse invioMdsResponse = flussoDpmService.startInvioMdsXmlRecord(xmlToMds, regoleFlusso, infoRun.getIdRun(), invioRequest.getIdClient(), ModalitaOperativa.P,gestoreRunLog);
        return new ResponseEntity<>(invioMdsResponse,headers, HttpStatus.OK);
    }

    @GetMapping("v1/flusso/donazionipostmortem/stato/{identificativoSoggettoAlimentante}")
    public ResponseEntity<DpmVerificaStatoResponse> dpmVerificaStato(String nomeFlussoController, @PathVariable String identificativoSoggettoAlimentante, @RequestParam String regioneSoggettoAlimentante, @RequestParam String cap, @RequestParam String codiceSoggettoAlimentante, @RequestParam String indirizzo)
    {
        GestoreFile gestoreFile = getGestoreFileFromString("DEFAULT");
        GestoreRunLog gestoreRunLog = createGestoreRunLog(gestoreFile, Progressivo.creaProgressivo(Progressivo.Fonte.FILE));
        String nomeFlusso = "DPM";
        InfoRun infoRun = gestoreRunLog.creaRunLog("", ModalitaOperativa.P, 1, nomeFlusso);
        //aggiungo campi per dpm
        infoRun=gestoreRunLog.getRun(infoRun.getIdRun());
        infoRun.setApi(API_3);
        infoRun.setTimestampCreazione(new Timestamp(System.currentTimeMillis()));
        infoRun.setVersion(getClass().getPackage().getImplementationVersion());
        infoRun.setIdentificativoSoggettoAlimentante(identificativoSoggettoAlimentante);
        gestoreRunLog.updateRun(infoRun);
        DpmVerificaStatoResponse response = getDpmVerificaStatoResponseFromService(identificativoSoggettoAlimentante,regioneSoggettoAlimentante,cap,codiceSoggettoAlimentante,indirizzo,infoRun.getIdRun());
        infoRun.setNumeroAtto(response.getNumeroAtto());
        infoRun.setTipoEsitoMds(response.getTipoEsito().value());
        infoRun.setDataRicevutaMds(response.getDataEmissioneRicevuta());
        gestoreRunLog.updateRun(infoRun);
        return new ResponseEntity<>(response,headers, HttpStatus.OK);
    }

    @Override
    @GetMapping("v1/flusso/donazionipostmortem/info")
    public ResponseEntity<InfoRun> informazioniRun(@RequestParam(required = false) String idRun, @RequestParam(required = false) String idClient) {
        GestoreFile gestoreFile = getGestoreFileFromString("CSV");
        GestoreRunLog gestoreRunLog = createGestoreRunLog(gestoreFile, Progressivo.creaProgressivo(Progressivo.Fonte.FILE));
        InfoRun infoRun;
        if (idRun != null) {
            infoRun = gestoreRunLog.getRun(idRun);
        } else if (idClient != null) {
            infoRun = gestoreRunLog.getRunFromClient(idClient);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Run non trovata");
        }
        if (infoRun == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Run non trovata");
        }
        return new ResponseEntity<>(infoRun, headers, HttpStatus.OK);
    }

    public DpmVerificaStatoResponse getDpmVerificaStatoResponseFromService(String identificativoSoggettoAlimentante, String regioneSoggettoAlimentante, String cap, String codiceSoggettoAlimentante, String indirizzo, String idRun) {
        return flussoDpmService.dpmVerificaStato(identificativoSoggettoAlimentante,regioneSoggettoAlimentante,cap,codiceSoggettoAlimentante,indirizzo,idRun);
    }
    public String getIdSoggettoAlimentante(RecordRequest<RecordDtoDpm> recordRequest) {
        return recordRequest.getRecordDto().getIdSoggettoAlimentante();
    }
    public GestoreFile getGestoreFileFromString(String csv) {
        return GestoreFileFactory.getGestoreFile(csv);
    }

    public GestoreRunLog createGestoreRunLog(GestoreFile gestoreFile, Progressivo creaProgressivo) {
        return new GestoreRunLog(gestoreFile, creaProgressivo);
    }

    public File getFileFromPath(String percorso) {
        return new File(percorso);
    }
}
