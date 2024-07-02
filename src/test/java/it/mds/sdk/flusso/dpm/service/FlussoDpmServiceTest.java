/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.service;

import it.mds.sdk.connettoremds.ConnettoreMds;
import it.mds.sdk.connettoremds.dpm.webservice.bean.Dpm;
import it.mds.sdk.connettoremds.dpm.webservice.bean.verifica.stato.DpmMdsResponse;
import it.mds.sdk.connettoremds.exception.ConnettoreMdsException;
import it.mds.sdk.connettoremds.parser.xml.ParserFirma;
import it.mds.sdk.connettoremds.parser.xml.exception.ValidazioneFirmaException;
import it.mds.sdk.flusso.dpm.FlussoDpm;
import it.mds.sdk.flusso.dpm.parser.regole.ParserTracciatoImpl;
import it.mds.sdk.flusso.dpm.parser.regole.RecordDtoDpm;
import it.mds.sdk.flusso.dpm.parser.xml.ParserXml;
import it.mds.sdk.gestoreesiti.GestoreRunLog;
import it.mds.sdk.gestoreesiti.modelli.Esito;
import it.mds.sdk.gestoreesiti.modelli.InfoRun;
import it.mds.sdk.gestoreesiti.modelli.ModalitaOperativa;
import it.mds.sdk.gestorefile.exception.XSDNonSupportedException;
import it.mds.sdk.libreriaregole.dtos.RecordDtoGenerico;
import it.mds.sdk.libreriaregole.gestorevalidazione.GestoreValidazione;
import it.mds.sdk.libreriaregole.parser.ParserRegole;
import it.mds.sdk.libreriaregole.parser.ParserTracciato;
import it.mds.sdk.libreriaregole.regole.beans.RegoleFlusso;
import it.mds.sdk.libreriaregole.tracciato.TracciatoSplitter;
import it.mds.sdk.libreriaregole.validator.ValidationEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FlussoDpmServiceTest {

    @InjectMocks
    @Spy
    private FlussoDpmService flusso;
    private GestoreValidazione gestoreValidazione = Mockito.mock(GestoreValidazione.class);
    private GestoreRunLog gestoreRunLog = Mockito.mock(GestoreRunLog.class);
    private InfoRun infoRun = Mockito.mock(InfoRun.class);

    @Mock
    private TracciatoSplitter<RecordDtoDpm> tracciatoSplitter;
    @Mock
    private ParserFirma parserFirma;
    @Mock
    private ParserXml parserXml;
    @Mock
    private ConnettoreMds connettoreMds;

    private List<Esito> listaEsitiOk = new ArrayList<>();
    private List<Esito> listaEsitiKO = new ArrayList<>();
    private List<Path> pathList = new ArrayList<>();
    private RecordDtoGenerico recordDtoGenerico = Mockito.mock(RecordDtoGenerico.class);
    private RegoleFlusso regoleFlusso = Mockito.mock(RegoleFlusso.class);
    private RecordDtoDpm recordDtoDpm = Mockito.mock(RecordDtoDpm.class);
    private ValidationEngine validationEngine = Mockito.mock(ValidationEngine.class);
    private ParserRegole parserRegole = Mockito.mock(ParserRegole.class);
    private ParserTracciato parserTracciato = Mockito.mock(ParserTracciato.class);


    @BeforeEach
    void init(){
        listaEsitiOk.add(new Esito("a", "b", true, null));
        listaEsitiKO.add(new Esito("a", "b", false, null));

    }

    @Test
    void startValidaRecordTestOk(){
        doReturn(gestoreValidazione).when(flusso).createGestoreValidazione(any(), any(), any());
        doReturn(infoRun).when(gestoreRunLog).getRun(any());
        doReturn(listaEsitiOk).when(gestoreValidazione).gestioneValidazioneRecord(any(), any(), any());
        doReturn(pathList).when(tracciatoSplitter).dividiTracciato(any(), any());
        doReturn(recordDtoDpm).when(flusso).getRecordDpmFromGenerico(any());
        flusso.startValidaRecord(recordDtoGenerico, regoleFlusso, "idRun", "idClient", ModalitaOperativa.T, gestoreRunLog);
    }

    @Test
    void startValidaRecordTestOk2(){
        doReturn(gestoreValidazione).when(flusso).createGestoreValidazione(any(), any(), any());
        doReturn(infoRun).when(gestoreRunLog).getRun(any());
        doReturn(listaEsitiKO).when(gestoreValidazione).gestioneValidazioneRecord(any(), any(), any());
        doReturn(pathList).when(tracciatoSplitter).dividiTracciato(any(), any());
        doReturn(recordDtoDpm).when(flusso).getRecordDpmFromGenerico(any());
        doNothing().when(infoRun).setDescrizioneStatoEsecuzione(any());
        doReturn(infoRun).when(gestoreRunLog).cambiaStatoRun(any(), any());
        doReturn(infoRun).when(gestoreRunLog).updateRun(any());
        flusso.startValidaRecord(recordDtoGenerico, regoleFlusso, "idRun", "idClient", ModalitaOperativa.T, gestoreRunLog);
    }

    @Test
    void startValidaRecordTestKO_XSDNonSupportedException()
    {
        doReturn(gestoreValidazione).when(flusso).createGestoreValidazione(any(), any(), any());
        doReturn(infoRun).when(gestoreRunLog).getRun(any());
        doReturn(listaEsitiOk).when(gestoreValidazione).gestioneValidazioneRecord(any(), any(), any());
        doThrow(XSDNonSupportedException.class).when(tracciatoSplitter).dividiTracciato(any(), any());
        doReturn(recordDtoDpm).when(flusso).getRecordDpmFromGenerico(any());
        doReturn(infoRun).when(gestoreRunLog).cambiaStatoRun(any(), any());
        flusso.startValidaRecord(recordDtoGenerico, regoleFlusso, "idRun", "idClient", ModalitaOperativa.T, gestoreRunLog);
    }

    @Test
    void getRecordDpmFromGenericoTest(){
        RecordDtoDpm dpm = Mockito.mock(RecordDtoDpm.class);
        var response = flusso.getRecordDpmFromGenerico(dpm);
        Assertions.assertEquals(response, dpm);
    }

    @Test
    void createGestoreValidazioneTest(){
        flusso.createGestoreValidazione(validationEngine, parserRegole, parserTracciato);
    }

    @Test
    void startInvioMdsXmlRecordTestOk3() throws ValidazioneFirmaException, ConnettoreMdsException {
        String xmltoMds = "";
        Dpm dpm = Mockito.mock(Dpm.class);
        File file = Mockito.mock(File.class);
        DpmMdsResponse dpmMdsResponse = Mockito.mock(DpmMdsResponse.class);
        doReturn(xmltoMds).when(parserFirma).checkFirmaXmlP7MConn(any());
        doReturn(dpm).when(parserXml).parseXmlDpm(any());
        doReturn(recordDtoDpm).when(parserXml).convertXMLToDto(any());
        doReturn(infoRun).when(gestoreRunLog).getRun(any());
        doNothing().when(infoRun).setIdentificativoSoggettoAlimentante(any());
        doReturn(infoRun).when(gestoreRunLog).updateRun(any());
        doReturn(gestoreValidazione).when(flusso).createGestoreValidazione(any(), any(), any());
        doReturn(listaEsitiKO).when(gestoreValidazione).gestioneValidazioneRecord(any(), any(), any());
        doReturn(dpmMdsResponse).when(connettoreMds).invioTracciatoDonazionePostMortem(any(),any(),any(), any(), any());
        flusso.startInvioMdsXmlRecord(file, regoleFlusso, "id", "id", ModalitaOperativa.T, gestoreRunLog);
    }

    @Test
    void startInvioMdsXmlRecordTestKO1() throws ValidazioneFirmaException, ConnettoreMdsException {
        String xmltoMds = "";
        Dpm dpm = Mockito.mock(Dpm.class);
        File file = Mockito.mock(File.class);
        DpmMdsResponse dpmMdsResponse = Mockito.mock(DpmMdsResponse.class);
        doThrow(ValidazioneFirmaException.class).when(parserFirma).checkFirmaXmlP7MConn(any());
        doReturn(infoRun).when(gestoreRunLog).cambiaStatoRun(any(), any());
        doNothing().when(infoRun).setDescrizioneStatoEsecuzione(any());
        doReturn(infoRun).when(gestoreRunLog).updateRun(any());
        flusso.startInvioMdsXmlRecord(file, regoleFlusso, "id", "id", ModalitaOperativa.T, gestoreRunLog);
   }

    @Test
    void startInvioMdsXmlRecordTestOk2() throws ValidazioneFirmaException, ConnettoreMdsException {
        String xmltoMds = "";
        Dpm dpm = Mockito.mock(Dpm.class);
        File file = Mockito.mock(File.class);
        DpmMdsResponse dpmMdsResponse = Mockito.mock(DpmMdsResponse.class);
        doReturn(xmltoMds).when(parserFirma).checkFirmaXmlP7MConn(any());
//        doReturn(infoRun).when(gestoreRunLog).cambiaStatoRun(any(), any());
//        doNothing().when(infoRun.setDescrizioneStatoEsecuzione(any());
//        doReturn(infoRun).when(gestoreRunLog.updateRun(any()));
        doReturn(dpm).when(parserXml).parseXmlDpm(any());
        doReturn(recordDtoDpm).when(parserXml).convertXMLToDto(any());
        doReturn(infoRun).when(gestoreRunLog).getRun(any());
        doReturn(infoRun).when(gestoreRunLog).cambiaStatoRun(any(), any());
        doNothing().when(infoRun).setIdentificativoSoggettoAlimentante(any());
        doReturn(infoRun).when(gestoreRunLog).updateRun(any());
        doReturn(gestoreValidazione).when(flusso).createGestoreValidazione(any(), any(), any());
        doReturn(listaEsitiOk).when(gestoreValidazione).gestioneValidazioneRecord(any(), any(), any());
        doThrow(ConnettoreMdsException.class).when(connettoreMds).invioTracciatoDonazionePostMortem(any(),any(),any(), any(), any());
        flusso.startInvioMdsXmlRecord(file, regoleFlusso, "id", "id", ModalitaOperativa.T, gestoreRunLog);
    }

    @Test
    public void dpmVerificaStatoTestOk() throws ConnettoreMdsException {
        DpmMdsResponse dpmMdsResponse = Mockito.mock(DpmMdsResponse.class);
        doReturn(dpmMdsResponse).when(connettoreMds).verificaElaborazioneDonazionePostMortem(any(), any(), any());
        flusso.dpmVerificaStato("id", "reg", "cap", "cod", "ind", "id");
    }

    @Test
    public void dpmVerificaStatoTestKO() throws ConnettoreMdsException {
        doThrow(ConnettoreMdsException.class).when(connettoreMds).verificaElaborazioneDonazionePostMortem(any(), any(), any());
        flusso.dpmVerificaStato("id", "reg", "cap", "cod", "ind", "id");
    }
}
