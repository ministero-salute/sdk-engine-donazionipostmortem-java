/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.parser.xml;

import it.mds.sdk.connettoremds.dpm.webservice.bean.AnagrafePersonaType;
import it.mds.sdk.connettoremds.dpm.webservice.bean.Dpm;
import it.mds.sdk.flusso.dpm.parser.regole.RecordDtoDpm;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@Slf4j
@Component("parserXmlDpm")
public class ParserXml {

    public Dpm parseXmlDpm(String xmlToMds) {
        log.debug("{}.parseXmlDpm - xmlToMds[{}] - BEGIN",
                this.getClass().getName(), xmlToMds);
        try {
            JAXBContext jaxbContext = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[]{Dpm.class}, null);
            Unmarshaller jaxbUnmarshalled = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(xmlToMds);
            return (Dpm) jaxbUnmarshalled.unmarshal(reader);
        } catch (JAXBException e) {
            log.error("{}.parseXmlDpm - xmlToMds[{}]",
                    this.getClass().getName(), xmlToMds, e);
        }
        return null;
    }


    public RecordDtoDpm convertXMLToDto(Dpm dpm)
    {
        log.debug("{}.convertXMLToDto - dpm[{}] - BEGIN",
                this.getClass().getName(), dpm.toString());
        SimpleDateFormat sdf = getSimpleDataFormat();

        RecordDtoDpm recordDtoDpm = createRecordDtoDpm();
        recordDtoDpm.setDonatoreMinorenne(dpm.getDonatoreMinorenne().value());
        recordDtoDpm.setTipoAtto(dpm.getTipoAtto().value());
        if(dpm.getTipoDisponente()!=null && !dpm.getTipoDisponente().value().isEmpty()) {
            recordDtoDpm.setTipoDisponente(dpm.getTipoDisponente().value());
        }
        recordDtoDpm.setFormatoAtto(dpm.getFormatoAtto().value());
        if(dpm.getDonatore()!=null) {
            recordDtoDpm.setDonatoreLuogoNascitaCodiceNazione(dpm.getDonatore().getLuogoNascita().getCodiceNazione());
            recordDtoDpm.setDonatoreLuogoNascitaCodiceRegione(dpm.getDonatore().getLuogoNascita().getCodiceRegione());
            recordDtoDpm.setDonatoreLuogoNascitaCodiceProvincia(dpm.getDonatore().getLuogoNascita().getCodiceProvincia());
            recordDtoDpm.setDonatoreLuogoNascitaCodiceComune(dpm.getDonatore().getLuogoNascita().getCodiceComune());

            recordDtoDpm.setDonatoreLuogoResidenzaCodiceNazione(dpm.getDonatore().getLuogoResidenza().getCodiceNazione());
            recordDtoDpm.setDonatoreLuogoResidenzaCodiceRegione(dpm.getDonatore().getLuogoResidenza().getCodiceRegione());
            recordDtoDpm.setDonatoreLuogoResidenzaCodiceProvincia(dpm.getDonatore().getLuogoResidenza().getCodiceProvincia());
            recordDtoDpm.setDonatoreLuogoResidenzaCodiceComune(dpm.getDonatore().getLuogoResidenza().getCodiceComune());

            recordDtoDpm.setDonatoreNome(dpm.getDonatore().getNome());
            recordDtoDpm.setDonatoreCognome(dpm.getDonatore().getCognome());
            recordDtoDpm.setDonatoreCF(dpm.getDonatore().getCodiceFiscale());
            Date dataNascita = getGregorianCalendarDateTime(dpm.getDonatore().getDataNascita());
            recordDtoDpm.setDonatoreDataNascita(sdf.format(dataNascita));
            recordDtoDpm.setDonatoreIndirizzoResidenza(dpm.getDonatore().getIndirizzoResidenza());
            recordDtoDpm.setDonatoreCapResidenza(dpm.getDonatore().getCapResidenza());
        }
        if(dpm.getDisponenti().get(0)!=null){
            AnagrafePersonaType disponenti1= dpm.getDisponenti().get(0);
            recordDtoDpm.setDisponenti1ConsensoEmail(disponenti1.getConsensoEmail().value());
            recordDtoDpm.setDisponenti1LuogoNascitaCodiceNazione(disponenti1.getLuogoNascita().getCodiceNazione());
            recordDtoDpm.setDisponenti1LuogoNascitaCodiceRegione(disponenti1.getLuogoNascita().getCodiceRegione());
            recordDtoDpm.setDisponenti1LuogoNascitaCodiceProvincia(disponenti1.getLuogoNascita().getCodiceProvincia());
            recordDtoDpm.setDisponenti1LuogoNascitaCodiceComune(disponenti1.getLuogoNascita().getCodiceComune());

            recordDtoDpm.setDisponenti1LuogoResidenzaCodiceNazione(disponenti1.getLuogoResidenza().getCodiceNazione());
            recordDtoDpm.setDisponenti1LuogoResidenzaCodiceRegione(disponenti1.getLuogoResidenza().getCodiceRegione());
            recordDtoDpm.setDisponenti1LuogoResidenzaCodiceProvincia(disponenti1.getLuogoResidenza().getCodiceProvincia());
            recordDtoDpm.setDisponenti1LuogoResidenzaCodiceComune(disponenti1.getLuogoResidenza().getCodiceComune());

            recordDtoDpm.setDisponenti1Nome(disponenti1.getNome());
            recordDtoDpm.setDisponenti1Cognome(disponenti1.getCognome());
            recordDtoDpm.setDisponenti1CF(disponenti1.getCodiceFiscale());
            Date dataNascitaDisp = getGregorianCalendarDateTime(disponenti1.getDataNascita());
            recordDtoDpm.setDisponenti1DataNascita(sdf.format(dataNascitaDisp));
            recordDtoDpm.setDisponenti1IndirizzoResidenza(disponenti1.getIndirizzoResidenza());
            recordDtoDpm.setDisponenti1CapResidenza(disponenti1.getCapResidenza());
            recordDtoDpm.setDisponenti1Email(disponenti1.getEmail());
        }

        if(dpm.getDisponenti().size()>1 && dpm.getDisponenti().get(1)!=null){
            AnagrafePersonaType disponenti2= dpm.getDisponenti().get(0);
            recordDtoDpm.setDisponenti2ConsensoEmail(disponenti2.getConsensoEmail().value());
            recordDtoDpm.setDisponenti2LuogoNascitaCodiceNazione(disponenti2.getLuogoNascita().getCodiceNazione());
            recordDtoDpm.setDisponenti2LuogoNascitaCodiceRegione(disponenti2.getLuogoNascita().getCodiceRegione());
            recordDtoDpm.setDisponenti2LuogoNascitaCodiceProvincia(disponenti2.getLuogoNascita().getCodiceProvincia());
            recordDtoDpm.setDisponenti2LuogoNascitaCodiceComune(disponenti2.getLuogoNascita().getCodiceComune());

            recordDtoDpm.setDisponenti2LuogoResidenzaCodiceNazione(disponenti2.getLuogoResidenza().getCodiceNazione());
            recordDtoDpm.setDisponenti2LuogoResidenzaCodiceRegione(disponenti2.getLuogoResidenza().getCodiceRegione());
            recordDtoDpm.setDisponenti2LuogoResidenzaCodiceProvincia(disponenti2.getLuogoResidenza().getCodiceProvincia());
            recordDtoDpm.setDisponenti2LuogoResidenzaCodiceComune(disponenti2.getLuogoResidenza().getCodiceComune());

            recordDtoDpm.setDisponenti2Nome(disponenti2.getNome());
            recordDtoDpm.setDisponenti2Cognome(disponenti2.getCognome());
            recordDtoDpm.setDisponenti2CF(disponenti2.getCodiceFiscale());
            Date dataNascitaDisp = getGregorianCalendarDateTime(disponenti2.getDataNascita());
            recordDtoDpm.setDisponenti2DataNascita(sdf.format(dataNascitaDisp));
            recordDtoDpm.setDisponenti2IndirizzoResidenza(disponenti2.getIndirizzoResidenza());
            recordDtoDpm.setDisponenti2CapResidenza(disponenti2.getCapResidenza());
            recordDtoDpm.setDisponenti2Email(disponenti2.getEmail());
        }
        if(dpm.getFiduciari().get(0)!=null){
            AnagrafePersonaType fiduciario1= dpm.getDisponenti().get(0);
            recordDtoDpm.setFiduciari1ConsensoEmail(fiduciario1.getConsensoEmail().value());
            recordDtoDpm.setFiduciari1LuogoNascitaCodiceNazione(fiduciario1.getLuogoNascita().getCodiceNazione());
            recordDtoDpm.setFiduciari1LuogoNascitaCodiceRegione(fiduciario1.getLuogoNascita().getCodiceRegione());
            recordDtoDpm.setFiduciari1LuogoNascitaCodiceProvincia(fiduciario1.getLuogoNascita().getCodiceProvincia());
            recordDtoDpm.setFiduciari1LuogoNascitaCodiceComune(fiduciario1.getLuogoNascita().getCodiceComune());

            recordDtoDpm.setFiduciari1LuogoResidenzaCodiceNazione(fiduciario1.getLuogoResidenza().getCodiceNazione());
            recordDtoDpm.setFiduciari1LuogoResidenzaCodiceRegione(fiduciario1.getLuogoResidenza().getCodiceRegione());
            recordDtoDpm.setFiduciari1LuogoResidenzaCodiceProvincia(fiduciario1.getLuogoResidenza().getCodiceProvincia());
            recordDtoDpm.setFiduciari1LuogoResidenzaCodiceComune(fiduciario1.getLuogoResidenza().getCodiceComune());

            recordDtoDpm.setFiduciari1Nome(fiduciario1.getNome());
            recordDtoDpm.setFiduciari1Cognome(fiduciario1.getCognome());
            recordDtoDpm.setFiduciari1CF(fiduciario1.getCodiceFiscale());
            Date dataNascitaFiduciario1 = getGregorianCalendarDateTime(fiduciario1.getDataNascita());
            recordDtoDpm.setFiduciari1DataNascita(sdf.format(dataNascitaFiduciario1));
            recordDtoDpm.setFiduciari1IndirizzoResidenza(fiduciario1.getIndirizzoResidenza());
            recordDtoDpm.setFiduciari1CapResidenza(fiduciario1.getCapResidenza());
            recordDtoDpm.setFiduciari1Email(fiduciario1.getEmail());
        }
        if(dpm.getFiduciari().get(1)!=null){
            AnagrafePersonaType fiduciario2= dpm.getDisponenti().get(0);
            recordDtoDpm.setFiduciari2ConsensoEmail(fiduciario2.getConsensoEmail().value());
            recordDtoDpm.setFiduciari2LuogoNascitaCodiceNazione(fiduciario2.getLuogoNascita().getCodiceNazione());
            recordDtoDpm.setFiduciari2LuogoNascitaCodiceRegione(fiduciario2.getLuogoNascita().getCodiceRegione());
            recordDtoDpm.setFiduciari2LuogoNascitaCodiceProvincia(fiduciario2.getLuogoNascita().getCodiceProvincia());
            recordDtoDpm.setFiduciari2LuogoNascitaCodiceComune(fiduciario2.getLuogoNascita().getCodiceComune());

            recordDtoDpm.setFiduciari2LuogoResidenzaCodiceNazione(fiduciario2.getLuogoResidenza().getCodiceNazione());
            recordDtoDpm.setFiduciari2LuogoResidenzaCodiceRegione(fiduciario2.getLuogoResidenza().getCodiceRegione());
            recordDtoDpm.setFiduciari2LuogoResidenzaCodiceProvincia(fiduciario2.getLuogoResidenza().getCodiceProvincia());
            recordDtoDpm.setFiduciari2LuogoResidenzaCodiceComune(fiduciario2.getLuogoResidenza().getCodiceComune());

            recordDtoDpm.setFiduciari2Nome(fiduciario2.getNome());
            recordDtoDpm.setFiduciari2Cognome(fiduciario2.getCognome());
            recordDtoDpm.setFiduciari2CF(fiduciario2.getCodiceFiscale());
            Date dataNascitaFiduciario2= getGregorianCalendarDateTime(fiduciario2.getDataNascita());
            recordDtoDpm.setFiduciari2DataNascita(sdf.format(dataNascitaFiduciario2));
            recordDtoDpm.setFiduciari2IndirizzoResidenza(fiduciario2.getIndirizzoResidenza());
            recordDtoDpm.setFiduciari2CapResidenza(fiduciario2.getCapResidenza());
            recordDtoDpm.setFiduciari2Email(fiduciario2.getEmail());
        }

        recordDtoDpm.setSoggettoAlimentanteRegioneSoggettoAlimentante(dpm.getSoggettoAlimentante().getRegioneSoggettoAlimentante());
        recordDtoDpm.setSoggettoAlimentanteCap(dpm.getSoggettoAlimentante().getCap());
        recordDtoDpm.setSoggettoAlimentanteCodiceSoggettoAlimentante(dpm.getSoggettoAlimentante().getCodiceSoggettoAlimentante());
        recordDtoDpm.setSoggettoAlimentanteIndirizzo(dpm.getSoggettoAlimentante().getIndirizzo());
        recordDtoDpm.setIdSoggettoAlimentante(dpm.getIdentificativoSoggettoAlimentante());
        recordDtoDpm.setDataSottoscrizione(sdf.format(getGregorianCalendarDateTime(dpm.getDataSottoscrizione())));


        return recordDtoDpm;
    }

    public Date getGregorianCalendarDateTime(XMLGregorianCalendar dataNascita) {
        return dataNascita.toGregorianCalendar().getTime();
    }

    public RecordDtoDpm createRecordDtoDpm() {
        return new RecordDtoDpm();
    }

    public SimpleDateFormat getSimpleDataFormat() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

}
