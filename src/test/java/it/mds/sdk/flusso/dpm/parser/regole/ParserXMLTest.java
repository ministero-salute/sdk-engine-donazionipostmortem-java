/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.parser.regole;

import it.mds.sdk.connettoremds.dpm.webservice.bean.*;
import it.mds.sdk.flusso.dpm.parser.xml.ParserXml;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ParserXMLTest {

    @InjectMocks
    @Spy
    private ParserXml parserXml;
    @Mock
    private RecordDtoDpm recordDtoDpm ;
    @Mock
    private Dpm dpm;
    @Mock
    private SoggettoAlimentanteType soggettoAlimentanteType;

    AnagrafePersonaType aptb;
    AnagrafeGeoType agt;
    private SimpleDateFormat simpleDateFormat = Mockito.mock(SimpleDateFormat.class);

    @BeforeEach
    void init(){
       var cal= Mockito.mock(XMLGregorianCalendar.class);
        agt = new AnagrafeGeoType();
        agt.setCodiceComune("com");
        agt.setCodiceNazione("cn");
        agt.setCodiceProvincia("cp");
        agt.setCodiceRegione("cr");


       aptb = new AnagrafePersonaType();
        aptb.setCognome("cogn");
        aptb.setCapResidenza("cap");
        aptb.setCodiceFiscale("cf");
        aptb.setEmail("email");
        aptb.setConsensoEmail(YesNoType.NO);
        aptb.setIndirizzoResidenza("ir");
        aptb.setLuogoNascita(agt);
        aptb.setLuogoResidenza(agt);
        aptb.setDataNascita(cal);
    }

    @Test
    void convertXMLToDtoTest(){
        var list = List.of(aptb);
        Mockito.doReturn(recordDtoDpm).when(parserXml).createRecordDtoDpm();
        Mockito.doReturn(simpleDateFormat).when(parserXml).getSimpleDataFormat();
        Mockito.doReturn(YesNoType.NO).when(dpm).getDonatoreMinorenne();
        Mockito.doReturn(TipoAttoType.REVOCA_CONSENSO).when(dpm).getTipoAtto();
        Mockito.doReturn(FormatoAttoType.FORMA_AUDIO_VIDEO).when(dpm).getFormatoAtto();

        Mockito.doReturn(List.of(aptb, aptb)).when(dpm).getDisponenti();
        Mockito.doReturn(List.of(aptb, aptb)).when(dpm).getFiduciari();
        Mockito.doReturn(new Date()).when(parserXml).getGregorianCalendarDateTime(any());
        Mockito.doReturn(soggettoAlimentanteType).when(dpm).getSoggettoAlimentante();
        Mockito.doReturn("").when(soggettoAlimentanteType).getRegioneSoggettoAlimentante();
        Mockito.doReturn("").when(soggettoAlimentanteType).getCodiceSoggettoAlimentante();
        Mockito.doReturn(aptb).when(dpm).getDonatore();
        parserXml.convertXMLToDto(dpm);
    }
}
