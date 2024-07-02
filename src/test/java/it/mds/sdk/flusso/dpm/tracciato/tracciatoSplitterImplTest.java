/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.tracciato;

import it.mds.sdk.connettoremds.dpm.webservice.bean.*;
import it.mds.sdk.flusso.dpm.parser.regole.ParserTracciatoImpl;
import it.mds.sdk.flusso.dpm.parser.regole.RecordDtoDpm;
import it.mds.sdk.flusso.dpm.parser.regole.conf.ConfigurazioneFlussoDpm;
import it.mds.sdk.gestorefile.GestoreFile;
import it.mds.sdk.libreriaregole.dtos.RecordDtoGenerico;
import it.mds.sdk.libreriaregole.gestorevalidazione.GestoreValidazione;
import it.mds.sdk.libreriaregole.parser.ParserTracciato;
import it.mds.sdk.libreriaregole.tracciato.TracciatoSplitter;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.xml.sax.SAXException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@MockitoSettings(strictness = Strictness.LENIENT)
class tracciatoSplitterImplTest {

    private static final String FILE_TRACCIATO_DPM = "tracciato-dpm-test.csv";
    @InjectMocks
    @Spy
    private TracciatoSplitterImpl tracciatoSplitter;
    private GestoreFile gestoreFile = Mockito.mock(GestoreFile.class);
    private ObjectFactory objectFactory = Mockito.mock(ObjectFactory.class);
    private List<RecordDtoDpm> recordDtoDpms = new ArrayList<>();
    private XMLGregorianCalendar gregorian = Mockito.mock(XMLGregorianCalendar.class);
    private ConfigurazioneFlussoDpm configurazione = Mockito.mock(ConfigurazioneFlussoDpm.class);
    private Dpm dpm = Mockito.mock(Dpm.class);
    private TipoAttoType tipoAttoType = Mockito.mock(TipoAttoType.class);
    private FormatoAttoType formatoAttoType = Mockito.mock(FormatoAttoType.class);
    private YesNoType yesNoType = Mockito.mock(YesNoType.class);
    private TipoDisponenteType tipoDisponenteType = Mockito.mock(TipoDisponenteType.class);
    private AnagrafePersonaType anagrafePersonaType = Mockito.mock(AnagrafePersonaType.class);
    private XMLGregorianCalendar xmlGregorianCalendar = Mockito.mock(XMLGregorianCalendar.class);
    private AnagrafeGeoType anagrafeGeoType = Mockito.mock(AnagrafeGeoType.class);
    private SoggettoAlimentanteType soggettoAlimentanteType = Mockito.mock(SoggettoAlimentanteType.class);
    private ConfigurazioneFlussoDpm.XmlOutput xmlOutput = Mockito.mock(ConfigurazioneFlussoDpm.XmlOutput.class);


    @BeforeEach
    void init(){
        RecordDtoDpm recordDtoDpm = new RecordDtoDpm();
        recordDtoDpm.setDataSottoscrizione("data");
        recordDtoDpm.setDisponenti1CF("d");
        recordDtoDpm.setDisponenti1CapResidenza("d1cr");
        recordDtoDpm.setDisponenti1Cognome("cogno");
        recordDtoDpm.setDisponenti1CapResidenza("dcr");
        recordDtoDpm.setTipoAtto("tipoatto");
        recordDtoDpm.setFormatoAtto("formato");
        recordDtoDpm.setDonatoreMinorenne("donatoreminorenne");
        recordDtoDpm.setTipoDisponente("tipodispo");
        recordDtoDpm.setDonatoreNome("donatorenome");
        recordDtoDpm.setDisponenti2Cognome("a");
        recordDtoDpm.setFiduciari1Cognome("a");
        recordDtoDpm.setFiduciari2Cognome("a");
        recordDtoDpms.add(recordDtoDpm);
    }
    @Test
    void dividiTracciatoTest() throws DatatypeConfigurationException, JAXBException, IOException, SAXException {
        ConfigurazioneFlussoDpm configurazione = Mockito.mock(ConfigurazioneFlussoDpm.class);
        Dpm dpm = Mockito.mock(Dpm.class);
        TipoAttoType tipoAttoType = Mockito.mock(TipoAttoType.class);
        FormatoAttoType formatoAttoType = Mockito.mock(FormatoAttoType.class);
        YesNoType yesNoType = Mockito.mock(YesNoType.class);
        TipoDisponenteType tipoDisponenteType = Mockito.mock(TipoDisponenteType.class);
        AnagrafePersonaType anagrafePersonaType = Mockito.mock(AnagrafePersonaType.class);
        XMLGregorianCalendar xmlGregorianCalendar = Mockito.mock(XMLGregorianCalendar.class);
        AnagrafeGeoType anagrafeGeoType = Mockito.mock(AnagrafeGeoType.class);
        SoggettoAlimentanteType soggettoAlimentanteType = Mockito.mock(SoggettoAlimentanteType.class);
        ConfigurazioneFlussoDpm.XmlOutput xmlOutput = Mockito.mock(ConfigurazioneFlussoDpm.XmlOutput.class);

        Mockito.doReturn(gestoreFile).when(tracciatoSplitter).getGestoreFileFromString(any());
        Mockito.doReturn(objectFactory).when(tracciatoSplitter).createNewObjectFactory();
        Mockito.doReturn(configurazione).when(tracciatoSplitter).createConfigurazione();
        Mockito.doReturn(anagrafePersonaType).when(objectFactory).createAnagrafePersonaType();
        Mockito.doReturn(xmlGregorianCalendar).when(tracciatoSplitter).getXMLGregorianCalendarFromDataNascita(any());
        Mockito.doReturn(anagrafeGeoType).when(objectFactory).createAnagrafeGeoType();
        Mockito.doReturn(soggettoAlimentanteType).when(objectFactory).createSoggettoAlimentanteType();
        Mockito.doNothing().when(tracciatoSplitter).addDisponente(any(), any());
        Mockito.doReturn(xmlOutput).when(configurazione).getXmlOutput();
        Mockito.doReturn("").when(xmlOutput).getPercorso();

        doNothing().when(dpm).setTipoAtto(any());
        doNothing().when(dpm).setFormatoAtto(any());
        doReturn(gregorian).when(tracciatoSplitter).getGregorianCalendarFromString(any());
        doNothing().when(dpm).setDataSottoscrizione(any());
        doNothing().when(dpm).setDonatoreMinorenne(any());
        doReturn(tipoAttoType).when(tracciatoSplitter).setTipoAttoFromString(any());
        doReturn(formatoAttoType).when(tracciatoSplitter).setFormatoAttoFromString(any());
        doReturn(yesNoType).when(tracciatoSplitter).setYesNoTypeFromString(any());
        doReturn(tipoDisponenteType).when(tracciatoSplitter).setTipoDisponenteFromString(any());
        doNothing().when(dpm).setIdentificativoSoggettoAlimentante(any());
        doReturn(anagrafePersonaType).when(tracciatoSplitter).createAnagraficaPersonaType();

        Mockito.doReturn(dpm).when(objectFactory).createDpm();
        Mockito.doNothing().when(gestoreFile).scriviDto(any(), any(), any());
        tracciatoSplitter.dividiTracciato(recordDtoDpms, "id");
        //        ParserTracciato parserTracciato = new ParserTracciatoImpl();
//        Path resourceDirectory = Paths.get("src", "test", "resources");
//        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
//        File tracciato = new File(absolutePath + FileSystems.getDefault().getSeparator() + FILE_TRACCIATO_DPM);
//        List<RecordDtoGenerico> listaRecord = parserTracciato.parseTracciato(tracciato);
//        TracciatoSplitter<RecordDtoDpm> impl = new TracciatoSplitterImpl();
    }

    @Test
    void dividiTracciatoTestKO() throws DatatypeConfigurationException, JAXBException, IOException, SAXException {

        Mockito.doReturn(gestoreFile).when(tracciatoSplitter).getGestoreFileFromString(any());
        Mockito.doReturn(objectFactory).when(tracciatoSplitter).createNewObjectFactory();
        Mockito.doReturn(configurazione).when(tracciatoSplitter).createConfigurazione();
        Mockito.doReturn(anagrafePersonaType).when(objectFactory).createAnagrafePersonaType();
        Mockito.doReturn(xmlGregorianCalendar).when(tracciatoSplitter).getXMLGregorianCalendarFromDataNascita(any());
        Mockito.doReturn(anagrafeGeoType).when(objectFactory).createAnagrafeGeoType();
        Mockito.doReturn(soggettoAlimentanteType).when(objectFactory).createSoggettoAlimentanteType();
        Mockito.doNothing().when(tracciatoSplitter).addDisponente(any(), any());
        Mockito.doReturn(xmlOutput).when(configurazione).getXmlOutput();
        Mockito.doReturn("").when(xmlOutput).getPercorso();

        doNothing().when(dpm).setTipoAtto(any());
        doNothing().when(dpm).setFormatoAtto(any());
        doReturn(gregorian).when(tracciatoSplitter).getGregorianCalendarFromString(any());
        doNothing().when(dpm).setDataSottoscrizione(any());
        doNothing().when(dpm).setDonatoreMinorenne(any());
        doReturn(tipoAttoType).when(tracciatoSplitter).setTipoAttoFromString(any());
        doReturn(formatoAttoType).when(tracciatoSplitter).setFormatoAttoFromString(any());
        doReturn(yesNoType).when(tracciatoSplitter).setYesNoTypeFromString(any());
        doReturn(tipoDisponenteType).when(tracciatoSplitter).setTipoDisponenteFromString(any());
        doNothing().when(dpm).setIdentificativoSoggettoAlimentante(any());
        doReturn(anagrafePersonaType).when(tracciatoSplitter).createAnagraficaPersonaType();

        Mockito.doReturn(dpm).when(objectFactory).createDpm();
        Mockito.doThrow(IOException.class).when(gestoreFile).scriviDto(any(), any(), any());
        Assertions.assertThrows(
                RuntimeException.class,
                ()-> tracciatoSplitter.dividiTracciato(recordDtoDpms, "id")
        );
        //        ParserTracciato parserTracciato = new ParserTracciatoImpl();
//        Path resourceDirectory = Paths.get("src", "test", "resources");
//        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
//        File tracciato = new File(absolutePath + FileSystems.getDefault().getSeparator() + FILE_TRACCIATO_DPM);
//        List<RecordDtoGenerico> listaRecord = parserTracciato.parseTracciato(tracciato);
//        TracciatoSplitter<RecordDtoDpm> impl = new TracciatoSplitterImpl();
    }

//    @Test
//    void creaLuogoResidenzaDisponenteTest(){
//        doReturn(AnagrafeGeoType.class).when(tracciatoSplitter).createAnagrafeGeoType();
//        RecordDtoDpm recordDtoDpm = new RecordDtoDpm();
//        recordDtoDpm.setDisponenti1LuogoResidenzaCodiceComune("");
//        recordDtoDpm.setDisponenti1LuogoResidenzaCodiceNazione("");
//        recordDtoDpm.setDisponenti1LuogoResidenzaCodiceProvincia("");
//        tracciatoSplitter.crealuog
//    }

}