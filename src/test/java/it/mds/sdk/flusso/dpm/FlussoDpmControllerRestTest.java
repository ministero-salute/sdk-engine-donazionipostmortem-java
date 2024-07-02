/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm;

import it.mds.sdk.connettoremds.dpm.webservice.bean.DettaglioType;
import it.mds.sdk.connettoremds.dpm.webservice.bean.Dpm;
import it.mds.sdk.connettoremds.dpm.webservice.bean.EsitoType;
import it.mds.sdk.flusso.dpm.controller.FlussoDpmControllerRest;
import it.mds.sdk.flusso.dpm.models.DpmVerificaStatoResponse;
import it.mds.sdk.flusso.dpm.models.EsitoServizioEnum;
import it.mds.sdk.flusso.dpm.parser.regole.RecordDtoDpm;
import it.mds.sdk.flusso.dpm.parser.regole.conf.ConfigurazioneFlussoDpm;
import it.mds.sdk.flusso.dpm.service.FlussoDpmService;
import it.mds.sdk.gestoreesiti.GestoreRunLog;
import it.mds.sdk.gestoreesiti.modelli.Esito;
import it.mds.sdk.gestoreesiti.modelli.InfoRun;
import it.mds.sdk.gestorefile.GestoreFile;
import it.mds.sdk.libreriaregole.dtos.RecordDtoGenerico;
import it.mds.sdk.libreriaregole.parser.ParserRegole;
import it.mds.sdk.libreriaregole.parser.ParserTracciato;
import it.mds.sdk.libreriaregole.regole.beans.RegoleFlusso;
import it.mds.sdk.rest.persistence.entity.FlussoRequest;
import it.mds.sdk.rest.persistence.entity.RecordRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FlussoDpmControllerRestTest {

    @Spy
    private ConfigurazioneFlussoDpm conf;
    @Mock
    private ParserTracciato parserTracciato;
    @InjectMocks
    @Spy
    private FlussoDpmControllerRest controller;
    @Mock
    private ConfigurazioneFlussoDpm.Flusso flusso;
    @Mock
    private ConfigurazioneFlussoDpm.Rules rules;
    @Mock
    private RegoleFlusso regoleFlusso;
    @Mock
    private ParserRegole parserRegole;

    @Mock
    private GestoreFile gestoreFile;

    @Mock
    private GestoreRunLog gestoreRunLog;
    @Mock
    private InfoRun infoRun;
    @Mock
    private FlussoRequest flussoRequest;
    @Mock
    private File file;
    @Mock
    private FlussoDpmService flussoDpmService;

    @Mock
    private RecordRequest<RecordDtoDpm> recordRequest;
    private DpmVerificaStatoResponse response = mock(DpmVerificaStatoResponse.class);


    List<RecordDtoGenerico> recordList = new ArrayList<>();

    @BeforeEach
    void init(){
        MockitoAnnotations.openMocks(this);
        var record = Mockito.mock(RecordDtoGenerico.class);
        recordList.add(record);
//        response = DpmVerificaStatoResponse.builder()
//                .withDettagli(new DettaglioType())
//                .withEsitoServizio(EsitoServizioEnum.KO)
//                .withDescrizioneEsitoServizio("")
//                .withIdentificativoSoggettoAlimentante("")
//                .withIdRun("")
//                .build();
    }

    @Test
    void validaTracciatoTest(){
//        doReturn(flusso).when(conf).getFlusso();
//        doReturn("").when(flusso).getPercorso();
//        doReturn(recordList).when(parserTracciato).parseTracciato(any());
//        doReturn(rules).when(conf).getRules();
//        doReturn(regoleFlusso).when(parserRegole).parseRegole(any());
//        doReturn(gestoreFile).when(controller).getGestoreFileFromString(any());
//        doReturn(file).when(controller).getFileFromPath(any());
//        doReturn(true).when(file).exists();
//        doReturn(gestoreRunLog).when(controller).createGestoreRunLog(any(), any());
//        doReturn(infoRun).when(gestoreRunLog).creaRunLog(any(), any(), anyInt(), any());
//        doReturn(infoRun).when(gestoreRunLog).cambiaStatoRun(any(), any());
       Assertions.assertNull( controller.validaTracciato(flussoRequest, ""));
    }

    @Test
    void validaRecordTest(){
        List<Esito> esitoList = List.of(Mockito.mock(Esito.class));
        doReturn(flusso).when(conf).getFlusso();
        doReturn("").when(flusso).getPercorso();
        doReturn(recordList).when(parserTracciato).parseTracciato(any());
        doReturn(rules).when(conf).getRules();
        doReturn(regoleFlusso).when(parserRegole).parseRegole(any());
        doReturn(gestoreFile).when(controller).getGestoreFileFromString(any());
        doReturn(file).when(controller).getFileFromPath(any());
        doReturn(true).when(file).exists();
        doReturn(gestoreRunLog).when(controller).createGestoreRunLog(any(), any());
        doReturn(infoRun).when(gestoreRunLog).creaRunLog(any(), any(), anyInt(), any());
        doReturn(infoRun).when(gestoreRunLog).getRun(any());
        doReturn(infoRun).when(gestoreRunLog).cambiaStatoRun(any(), any());
        doReturn("").when(controller).getIdSoggettoAlimentante(any());
        doReturn(esitoList).when(flussoDpmService).startValidaRecord(any(), any(), any(), any(), any(), any());
        controller.validaRecord(recordRequest, "");
    }
    @Test
    void dpmVerificaStatoTest(){
        doReturn(gestoreFile).when(controller).getGestoreFileFromString(any());
        doReturn(gestoreRunLog).when(controller).createGestoreRunLog(any(), any());
        doReturn(infoRun).when(gestoreRunLog).creaRunLog(any(), any(), anyInt(), any());
        doReturn(infoRun).when(gestoreRunLog).getRun(any());
        doReturn(infoRun).when(gestoreRunLog).updateRun(any());
      //  when(flussoDpmService.dpmVerificaStato("a", "a","a","a","a","a")).thenReturn(response);
        doReturn(response).when(controller).getDpmVerificaStatoResponseFromService(any(), any(), any(), any(), any(), any());
        doReturn("na").when(response).getNumeroAtto();
        doReturn(EsitoType.EVASA_OK).when(response).getTipoEsito();
        doReturn(Timestamp.from(Instant.now())).when(response).getDataEmissioneRicevuta();
        controller.dpmVerificaStato(
                "nome",
                "id",
                "rsa",
                "cap",
                "cod",
                "ind"
        );

    }

    @Test
    void informazioniRunTest(){
        doReturn(gestoreFile).when(controller).getGestoreFileFromString(any());
        doReturn(gestoreRunLog).when(controller).createGestoreRunLog(any(), any());
        doReturn(infoRun).when(gestoreRunLog).creaRunLog(any(), any(), anyInt(), any());
        doReturn(infoRun).when(gestoreRunLog).getRun(any());
        doReturn(infoRun).when(gestoreRunLog).updateRun(any());
        controller.informazioniRun(
                "idrun",
                "idclient"
        );
    }
    @Test
    void informazioniRunTest2(){
        doReturn(gestoreFile).when(controller).getGestoreFileFromString(any());
        doReturn(gestoreRunLog).when(controller).createGestoreRunLog(any(), any());
        doReturn(infoRun).when(gestoreRunLog).creaRunLog(any(), any(), anyInt(), any());
        doReturn(infoRun).when(gestoreRunLog).getRun(any());
        doReturn(infoRun).when(gestoreRunLog).updateRun(any());
//        doReturn(infoRun).when(gestoreRunLog).getRun(any())
        Assertions.assertThrows(
                ResponseStatusException.class,
                ()->controller.informazioniRun(
                        null,
                        "idclient"
                )
        );
    }

    @Test
    void informazioniRunTest3(){
        doReturn(gestoreFile).when(controller).getGestoreFileFromString(any());
        doReturn(gestoreRunLog).when(controller).createGestoreRunLog(any(), any());
        doReturn(infoRun).when(gestoreRunLog).creaRunLog(any(), any(), anyInt(), any());
        doReturn(infoRun).when(gestoreRunLog).getRun(any());
        doReturn(infoRun).when(gestoreRunLog).updateRun(any());
//        doReturn(infoRun).when(gestoreRunLog).getRun(any())
        Assertions.assertThrows(
                ResponseStatusException.class,
                ()->controller.informazioniRun(
                        null,
                        null
                )
        );
    }
}
