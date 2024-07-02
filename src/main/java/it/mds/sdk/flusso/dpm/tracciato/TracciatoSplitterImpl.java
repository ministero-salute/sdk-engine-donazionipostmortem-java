/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.tracciato;

import it.mds.sdk.connettoremds.dpm.webservice.bean.*;
import it.mds.sdk.flusso.dpm.parser.regole.RecordDtoDpm;
import it.mds.sdk.flusso.dpm.parser.regole.conf.ConfigurazioneFlussoDpm;
import it.mds.sdk.gestorefile.GestoreFile;
import it.mds.sdk.gestorefile.factory.GestoreFileFactory;
import it.mds.sdk.libreriaregole.tracciato.TracciatoSplitter;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component("tracciatoSplitter")
public class TracciatoSplitterImpl implements TracciatoSplitter<RecordDtoDpm> {
    @Override
    public List<Path> dividiTracciato(Path tracciato) {
        return null;
    }

    @Override
    public List<Path> dividiTracciato(List<RecordDtoDpm> records, String idRun) {
        log.debug("{}.nuovaDataAggiornamento - records[{}] - idRun[{}] - BEGIN",
                this.getClass().getName(), records.stream().map(obj -> "" + obj).collect(Collectors.joining("|")), idRun);
        //per ogni record devo creare un xml con un solo dpm

        GestoreFile gestoreFile = getGestoreFileFromString("XML");
        ConfigurazioneFlussoDpm conf = createConfigurazione();
        ObjectFactory objectFactory = createNewObjectFactory();
        for (RecordDtoDpm r : records) {

            try {
                Dpm dpm = objectFactory.createDpm();
                if (r.getTipoAtto() != null && !r.getTipoAtto().isBlank()) {
                    dpm.setTipoAtto(setTipoAttoFromString(r.getTipoAtto()));
                }
                if (r.getFormatoAtto() != null && !r.getFormatoAtto().isBlank()) {
                    dpm.setFormatoAtto(setFormatoAttoFromString(r.getFormatoAtto()));
                }
                XMLGregorianCalendar datSottXMl =getGregorianCalendarFromString(r.getDataSottoscrizione());
                dpm.setDataSottoscrizione(datSottXMl);
                if (r.getDonatoreMinorenne() != null && !r.getDonatoreMinorenne().isBlank()) {
                    dpm.setDonatoreMinorenne(setYesNoTypeFromString(r.getDonatoreMinorenne()));
                }
                dpm.setIdentificativoSoggettoAlimentante(r.getIdSoggettoAlimentante());
                if (r.getTipoDisponente() != null && !r.getTipoDisponente().isBlank()) {
                    dpm.setTipoDisponente(setTipoDisponenteFromString(r.getTipoDisponente()));
                }

                //creo donatore se esiste che è unico
                if (r.getDonatoreNome() != null) {
                    AnagrafePersonaType anagrafePersonaTypeDonatore = objectFactory.createAnagrafePersonaType();
                    anagrafePersonaTypeDonatore.setNome(r.getDonatoreNome());
                    anagrafePersonaTypeDonatore.setCognome(r.getDonatoreCognome());
                    XMLGregorianCalendar dataDonatoreNascitaXMl = getXMLGregorianCalendarFromDataNascita(r.getDonatoreDataNascita());
                    anagrafePersonaTypeDonatore.setDataNascita(dataDonatoreNascitaXMl);
                    // creo luogo di nascita
                    AnagrafeGeoType anagrafeGeoTypeNascita = objectFactory.createAnagrafeGeoType();
                    anagrafeGeoTypeNascita.setCodiceComune(r.getDonatoreLuogoNascitaCodiceComune());
                    anagrafeGeoTypeNascita.setCodiceNazione(r.getDonatoreLuogoNascitaCodiceNazione());
                    anagrafeGeoTypeNascita.setCodiceProvincia(r.getDonatoreLuogoNascitaCodiceProvincia());
                    anagrafeGeoTypeNascita.setCodiceRegione(r.getDonatoreLuogoNascitaCodiceRegione());
                    anagrafePersonaTypeDonatore.setLuogoNascita(anagrafeGeoTypeNascita);

                    anagrafePersonaTypeDonatore.setCodiceFiscale(r.getDonatoreCF());
                    anagrafePersonaTypeDonatore.setIndirizzoResidenza(r.getDonatoreIndirizzoResidenza());
                    anagrafePersonaTypeDonatore.setCapResidenza(r.getDonatoreCapResidenza());

                    //creo luogo residenza
                    AnagrafeGeoType anagrafeGeoTypeResidenza = objectFactory.createAnagrafeGeoType();
                    anagrafeGeoTypeResidenza.setCodiceComune(r.getDonatoreLuogoResidenzaCodiceComune());
                    anagrafeGeoTypeResidenza.setCodiceNazione(r.getDonatoreLuogoResidenzaCodiceNazione());
                    anagrafeGeoTypeResidenza.setCodiceProvincia(r.getDonatoreLuogoResidenzaCodiceProvincia());
                    anagrafeGeoTypeResidenza.setCodiceRegione(r.getDonatoreLuogoResidenzaCodiceRegione());
                    anagrafePersonaTypeDonatore.setLuogoResidenza(anagrafeGeoTypeResidenza);
                    //inserisco in dpm
                    dpm.setDonatore(anagrafePersonaTypeDonatore);
                }

                //creo soggetto alimentante
                SoggettoAlimentanteType soggettoAlimentanteType = objectFactory.createSoggettoAlimentanteType();
                soggettoAlimentanteType.setCodiceSoggettoAlimentante(r.getSoggettoAlimentanteCodiceSoggettoAlimentante());
                soggettoAlimentanteType.setRegioneSoggettoAlimentante(r.getSoggettoAlimentanteRegioneSoggettoAlimentante());
                soggettoAlimentanteType.setCap(r.getSoggettoAlimentanteCap());
                soggettoAlimentanteType.setIndirizzo(r.getSoggettoAlimentanteIndirizzo());
                //inserisco in dpm
                dpm.setSoggettoAlimentante(soggettoAlimentanteType);
                addDisponente(dpm, creaAnagrafePersonaTypeDisponente(r));
                if (r.getDisponenti2Cognome() != null) {
                    dpm.getDisponenti().add(creaAnagrafePersonaTypeDisponente2(r));
                }

                if (r.getFiduciari1Cognome() != null) {
                    dpm.getFiduciari().add(creaAnagrafePersonaTypeFiduciario(r));
                }
                if (r.getFiduciari2Cognome() != null) {
                    dpm.getFiduciari().add(creaAnagrafePersonaTypeFiduciario2(r));
                }

                URL urlXsd = this.getClass().getClassLoader().getResource("dpm.xsd");
                log.debug("URL dell'XSD per la validazione idrun {} : {}", idRun, urlXsd);

                gestoreFile.scriviDto(dpm, conf.getXmlOutput().getPercorso() + "DPM_OUTPUT_" + idRun + ".xml", urlXsd);

            } catch (DatatypeConfigurationException e) {
                log.error("{}.nuovaDataAggiornamento - records[{}] - idRun[{}] - BEGIN",
                        this.getClass().getName(), records.stream().map(obj -> "" + obj).collect(Collectors.joining("|")), idRun, e);
            } catch (JAXBException | IOException | SAXException e) {
                throw new RuntimeException(e);
            }
        }


        return List.of(Path.of(conf.getXmlOutput().getPercorso() + "DPM_OUTPUT_" + idRun + ".xml"));
    }

    public void addDisponente(Dpm dpm, AnagrafePersonaType creaAnagrafePersonaTypeDisponente) {
        dpm.getDisponenti().add(creaAnagrafePersonaTypeDisponente);
    }

    public XMLGregorianCalendar getXMLGregorianCalendarFromDataNascita(String donatoreDataNascita) throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(donatoreDataNascita);
    }

    public YesNoType setYesNoTypeFromString(String donatoreMinorenne) {
        return YesNoType.fromValue(donatoreMinorenne);
    }

    public TipoDisponenteType setTipoDisponenteFromString(String tipoDisponente) {
        return TipoDisponenteType.fromValue(tipoDisponente);
    }

    public XMLGregorianCalendar getGregorianCalendarFromString(String dataSottoscrizione) throws DatatypeConfigurationException {
        return  DatatypeFactory.newInstance().newXMLGregorianCalendar(dataSottoscrizione);
    }

    public FormatoAttoType setFormatoAttoFromString(String formatoAtto) {
        return FormatoAttoType.fromValue(formatoAtto);
    }

    public TipoAttoType setTipoAttoFromString(String tipoAtto) {
        return TipoAttoType.fromValue(tipoAtto);
    }

    public ConfigurazioneFlussoDpm createConfigurazione() {
        return new ConfigurazioneFlussoDpm();
    }

    public ObjectFactory createNewObjectFactory() {
        return new ObjectFactory();
    }

    public GestoreFile getGestoreFileFromString(String xml) {
        return GestoreFileFactory.getGestoreFile(xml);
    }


    private AnagrafeGeoType creaLuogoResidenzaDisponente(RecordDtoDpm recordDtoDpm) {
        log.debug("{}.creaLuogoResidenzaDisponente - recordDtoDpm[{}] - BEGIN",
                this.getClass().getName(), recordDtoDpm);
        AnagrafeGeoType anagrafeGeoTypeResidenza = createAnagrafeGeoType();
        anagrafeGeoTypeResidenza.setCodiceComune(recordDtoDpm.getDisponenti1LuogoResidenzaCodiceComune());
        anagrafeGeoTypeResidenza.setCodiceNazione(recordDtoDpm.getDisponenti1LuogoResidenzaCodiceNazione());
        anagrafeGeoTypeResidenza.setCodiceProvincia(recordDtoDpm.getDisponenti1LuogoResidenzaCodiceProvincia());
        anagrafeGeoTypeResidenza.setCodiceRegione(recordDtoDpm.getDisponenti1LuogoResidenzaCodiceRegione());
        return anagrafeGeoTypeResidenza;
    }

    private AnagrafeGeoType creaLuogoNascitaDisponente(RecordDtoDpm recordDtoDpm) {
        log.debug("{}.creaLuogoNascitaDisponente - recordDtoDpm[{}] - BEGIN",
                this.getClass().getName(), recordDtoDpm);
        AnagrafeGeoType anagrafeGeoTypeNascita = createAnagrafeGeoType();
        anagrafeGeoTypeNascita.setCodiceComune(recordDtoDpm.getDisponenti1LuogoNascitaCodiceComune());
        anagrafeGeoTypeNascita.setCodiceNazione(recordDtoDpm.getDisponenti1LuogoNascitaCodiceNazione());
        anagrafeGeoTypeNascita.setCodiceProvincia(recordDtoDpm.getDisponenti1LuogoNascitaCodiceProvincia());
        anagrafeGeoTypeNascita.setCodiceRegione(recordDtoDpm.getDisponenti1LuogoNascitaCodiceRegione());
        return anagrafeGeoTypeNascita;
    }

    public AnagrafeGeoType createAnagrafeGeoType() {
        return new AnagrafeGeoType();
    }

    private AnagrafePersonaType creaAnagrafePersonaTypeDisponente(RecordDtoDpm recordDtoDpm) throws DatatypeConfigurationException {
        log.debug("{}.creaAnagrafePersonaTypeDisponente - recordDtoDpm[{}] - BEGIN",
                this.getClass().getName(), recordDtoDpm);
        AnagrafePersonaType anagrafePersonaTypeDisponente = createAnagraficaPersonaType();
        anagrafePersonaTypeDisponente.setNome(recordDtoDpm.getDisponenti1Nome());
        anagrafePersonaTypeDisponente.setCognome(recordDtoDpm.getDisponenti1Cognome());
        XMLGregorianCalendar dataDispNascitaXMl = getXMLGregorianCalendarFromDataNascita(recordDtoDpm.getDisponenti1DataNascita());
        anagrafePersonaTypeDisponente.setDataNascita(dataDispNascitaXMl);
        anagrafePersonaTypeDisponente.setLuogoResidenza(creaLuogoResidenzaDisponente(recordDtoDpm));
        anagrafePersonaTypeDisponente.setLuogoNascita(creaLuogoNascitaDisponente(recordDtoDpm));
        anagrafePersonaTypeDisponente.setCodiceFiscale(recordDtoDpm.getDisponenti1CF());
        anagrafePersonaTypeDisponente.setIndirizzoResidenza(recordDtoDpm.getDisponenti1IndirizzoResidenza());
        anagrafePersonaTypeDisponente.setCapResidenza(recordDtoDpm.getDisponenti1CapResidenza());
        anagrafePersonaTypeDisponente.setEmail(recordDtoDpm.getDisponenti1Email());
        if (recordDtoDpm.getDisponenti1ConsensoEmail() != null && !recordDtoDpm.getDisponenti1ConsensoEmail().isBlank()) {
            anagrafePersonaTypeDisponente.setConsensoEmail(YesNoType.fromValue(recordDtoDpm.getDisponenti1ConsensoEmail()));
        }

        return anagrafePersonaTypeDisponente;
    }

    public AnagrafePersonaType createAnagraficaPersonaType() {
        return new AnagrafePersonaType();
    }

//    private AnagrafeGeoType creaLuogoResidenzaDisponente2(RecordDtoDpm recordDtoDpm) {
//        log.debug("{}.creaLuogoResidenzaDisponente2 - recordDtoDpm[{}] - BEGIN",
//                this.getClass().getName(), recordDtoDpm);
//        AnagrafeGeoType anagrafeGeoTypeResidenza = new AnagrafeGeoType();
//        anagrafeGeoTypeResidenza.setCodiceComune(recordDtoDpm.getDisponenti2LuogoResidenzaCodiceComune());
//        anagrafeGeoTypeResidenza.setCodiceNazione(recordDtoDpm.getDisponenti2LuogoResidenzaCodiceNazione());
//        anagrafeGeoTypeResidenza.setCodiceProvincia(recordDtoDpm.getDisponenti2LuogoResidenzaCodiceProvincia());
//        anagrafeGeoTypeResidenza.setCodiceRegione(recordDtoDpm.getDisponenti2LuogoResidenzaCodiceRegione());
//        return anagrafeGeoTypeResidenza;
//    }
//
//    private AnagrafeGeoType creaLuogoNascitaDisponente2(RecordDtoDpm recordDtoDpm) {
//        log.debug("{}.creaLuogoNascitaDisponente2 - recordDtoDpm[{}] - BEGIN",
//                this.getClass().getName(), recordDtoDpm);
//        AnagrafeGeoType anagrafeGeoTypeNascita = new AnagrafeGeoType();
//        anagrafeGeoTypeNascita.setCodiceComune(recordDtoDpm.getDisponenti2LuogoNascitaCodiceComune());
//        anagrafeGeoTypeNascita.setCodiceNazione(recordDtoDpm.getDisponenti2LuogoNascitaCodiceNazione());
//        anagrafeGeoTypeNascita.setCodiceProvincia(recordDtoDpm.getDisponenti2LuogoNascitaCodiceProvincia());
//        anagrafeGeoTypeNascita.setCodiceRegione(recordDtoDpm.getDisponenti2LuogoNascitaCodiceRegione());
//        return anagrafeGeoTypeNascita;
//    }

    private AnagrafePersonaType creaAnagrafePersonaTypeDisponente2(RecordDtoDpm recordDtoDpm) throws DatatypeConfigurationException {
        log.debug("{}.creaAnagrafePersonaTypeDisponente2 - recordDtoDpm[{}] - BEGIN",
                this.getClass().getName(), recordDtoDpm);
        AnagrafePersonaType anagrafePersonaTypeDisponente = new AnagrafePersonaType();
        anagrafePersonaTypeDisponente.setNome(recordDtoDpm.getDisponenti2Nome());
        anagrafePersonaTypeDisponente.setCognome(recordDtoDpm.getDisponenti2Cognome());
        XMLGregorianCalendar dataDispNascitaXMl = getXMLGregorianCalendarFromDataNascita(recordDtoDpm.getDisponenti2DataNascita());
        anagrafePersonaTypeDisponente.setDataNascita(dataDispNascitaXMl);
        anagrafePersonaTypeDisponente.setLuogoResidenza(creaLuogoResidenzaDisponente(recordDtoDpm));
        anagrafePersonaTypeDisponente.setLuogoNascita(creaLuogoNascitaDisponente(recordDtoDpm));
        anagrafePersonaTypeDisponente.setCodiceFiscale(recordDtoDpm.getDisponenti2CF());
        anagrafePersonaTypeDisponente.setIndirizzoResidenza(recordDtoDpm.getDisponenti2IndirizzoResidenza());
        anagrafePersonaTypeDisponente.setCapResidenza(recordDtoDpm.getDisponenti2CapResidenza());
        anagrafePersonaTypeDisponente.setEmail(recordDtoDpm.getDisponenti2Email());
        if (recordDtoDpm.getDisponenti2ConsensoEmail() != null && !recordDtoDpm.getDisponenti2ConsensoEmail().isBlank()) {
            anagrafePersonaTypeDisponente.setConsensoEmail(YesNoType.fromValue(recordDtoDpm.getDisponenti2ConsensoEmail()));
        }

        return anagrafePersonaTypeDisponente;
    }


    private AnagrafeGeoType creaLuogoResidenzaFiduciari(RecordDtoDpm recordDtoDpm) {
        log.debug("{}.creaLuogoResidenzaFiduciari - recordDtoDpm[{}] - BEGIN",
                this.getClass().getName(), recordDtoDpm);
        AnagrafeGeoType anagrafeGeoTypeResidenza = new AnagrafeGeoType();
        anagrafeGeoTypeResidenza.setCodiceComune(recordDtoDpm.getFiduciari1LuogoResidenzaCodiceComune());
        anagrafeGeoTypeResidenza.setCodiceNazione(recordDtoDpm.getFiduciari1LuogoResidenzaCodiceNazione());
        anagrafeGeoTypeResidenza.setCodiceProvincia(recordDtoDpm.getFiduciari1LuogoResidenzaCodiceProvincia());
        anagrafeGeoTypeResidenza.setCodiceRegione(recordDtoDpm.getFiduciari1LuogoResidenzaCodiceRegione());
        return anagrafeGeoTypeResidenza;
    }

    private AnagrafeGeoType creaLuogoNascitaFiduciari(RecordDtoDpm recordDtoDpm) {
        log.debug("{}.creaLuogoNascitaFiduciari - recordDtoDpm[{}] - BEGIN",
                this.getClass().getName(), recordDtoDpm);
        AnagrafeGeoType anagrafeGeoTypeNascita = new AnagrafeGeoType();
        anagrafeGeoTypeNascita.setCodiceComune(recordDtoDpm.getFiduciari1LuogoNascitaCodiceComune());
        anagrafeGeoTypeNascita.setCodiceNazione(recordDtoDpm.getFiduciari1LuogoNascitaCodiceNazione());
        anagrafeGeoTypeNascita.setCodiceProvincia(recordDtoDpm.getFiduciari1LuogoNascitaCodiceProvincia());
        anagrafeGeoTypeNascita.setCodiceRegione(recordDtoDpm.getFiduciari1LuogoNascitaCodiceRegione());
        return anagrafeGeoTypeNascita;
    }

    private AnagrafePersonaType creaAnagrafePersonaTypeFiduciario(RecordDtoDpm recordDtoDpm) throws DatatypeConfigurationException {
        log.debug("{}.creaAnagrafePersonaTypeFiduciario - recordDtoDpm[{}] - BEGIN",
                this.getClass().getName(), recordDtoDpm);
        AnagrafePersonaType anagrafePersonaTypeFiduciario = new AnagrafePersonaType();
        anagrafePersonaTypeFiduciario.setNome(recordDtoDpm.getFiduciari1Nome());
        anagrafePersonaTypeFiduciario.setCognome(recordDtoDpm.getFiduciari1Cognome());
        XMLGregorianCalendar dataFiduciarioNascitaXMl = getXMLGregorianCalendarFromDataNascita(recordDtoDpm.getFiduciari1DataNascita());
        anagrafePersonaTypeFiduciario.setDataNascita(dataFiduciarioNascitaXMl);
        anagrafePersonaTypeFiduciario.setLuogoResidenza(creaLuogoResidenzaFiduciari(recordDtoDpm));
        anagrafePersonaTypeFiduciario.setLuogoNascita(creaLuogoNascitaFiduciari(recordDtoDpm));
        anagrafePersonaTypeFiduciario.setCodiceFiscale(recordDtoDpm.getFiduciari1CF());
        anagrafePersonaTypeFiduciario.setIndirizzoResidenza(recordDtoDpm.getFiduciari1IndirizzoResidenza());
        anagrafePersonaTypeFiduciario.setCapResidenza(recordDtoDpm.getFiduciari1CapResidenza());
        anagrafePersonaTypeFiduciario.setEmail(recordDtoDpm.getFiduciari1Email());
        if (recordDtoDpm.getFiduciari1ConsensoEmail() != null && !recordDtoDpm.getFiduciari1ConsensoEmail().isBlank()) {
            anagrafePersonaTypeFiduciario.setConsensoEmail(YesNoType.fromValue(recordDtoDpm.getFiduciari1ConsensoEmail()));
        }

        return anagrafePersonaTypeFiduciario;
    }


//    private AnagrafeGeoType creaLuogoResidenzaFiduciari2(RecordDtoDpm recordDtoDpm) {
//        log.debug("{}.creaLuogoResidenzaFiduciari2 - recordDtoDpm[{}] - BEGIN",
//                this.getClass().getName(), recordDtoDpm);
//        AnagrafeGeoType anagrafeGeoTypeResidenza = new AnagrafeGeoType();
//        anagrafeGeoTypeResidenza.setCodiceComune(recordDtoDpm.getFiduciari2LuogoResidenzaCodiceComune());
//        anagrafeGeoTypeResidenza.setCodiceNazione(recordDtoDpm.getFiduciari2LuogoResidenzaCodiceNazione());
//        anagrafeGeoTypeResidenza.setCodiceProvincia(recordDtoDpm.getFiduciari2LuogoResidenzaCodiceProvincia());
//        anagrafeGeoTypeResidenza.setCodiceRegione(recordDtoDpm.getFiduciari2LuogoResidenzaCodiceRegione());
//        return anagrafeGeoTypeResidenza;
//    }
//
//    private AnagrafeGeoType creaLuogoNascitaFiduciari2(RecordDtoDpm recordDtoDpm) {
//        log.debug("{}.creaLuogoNascitaFiduciari2 - recordDtoDpm[{}] - BEGIN",
//                this.getClass().getName(), recordDtoDpm);
//        AnagrafeGeoType anagrafeGeoTypeNascita = new AnagrafeGeoType();
//        anagrafeGeoTypeNascita.setCodiceComune(recordDtoDpm.getFiduciari2LuogoNascitaCodiceComune());
//        anagrafeGeoTypeNascita.setCodiceNazione(recordDtoDpm.getFiduciari2LuogoNascitaCodiceNazione());
//        anagrafeGeoTypeNascita.setCodiceProvincia(recordDtoDpm.getFiduciari2LuogoNascitaCodiceProvincia());
//        anagrafeGeoTypeNascita.setCodiceRegione(recordDtoDpm.getFiduciari2LuogoNascitaCodiceRegione());
//        return anagrafeGeoTypeNascita;
//    }

    private AnagrafePersonaType creaAnagrafePersonaTypeFiduciario2(RecordDtoDpm recordDtoDpm) throws DatatypeConfigurationException {
        log.debug("{}.creaAnagrafePersonaTypeFiduciario2 - recordDtoDpm[{}] - BEGIN",
                this.getClass().getName(), recordDtoDpm);
        AnagrafePersonaType anagrafePersonaTypeFiduciario = new AnagrafePersonaType();
        anagrafePersonaTypeFiduciario.setNome(recordDtoDpm.getFiduciari2Nome());
        anagrafePersonaTypeFiduciario.setCognome(recordDtoDpm.getFiduciari2Cognome());
        XMLGregorianCalendar dataFiduciarioNascitaXMl = getXMLGregorianCalendarFromDataNascita(recordDtoDpm.getFiduciari2DataNascita());
        anagrafePersonaTypeFiduciario.setDataNascita(dataFiduciarioNascitaXMl);
        anagrafePersonaTypeFiduciario.setLuogoResidenza(creaLuogoResidenzaFiduciari(recordDtoDpm));
        anagrafePersonaTypeFiduciario.setLuogoNascita(creaLuogoNascitaFiduciari(recordDtoDpm));
        anagrafePersonaTypeFiduciario.setCodiceFiscale(recordDtoDpm.getFiduciari2CF());
        anagrafePersonaTypeFiduciario.setIndirizzoResidenza(recordDtoDpm.getFiduciari2IndirizzoResidenza());
        anagrafePersonaTypeFiduciario.setCapResidenza(recordDtoDpm.getFiduciari2CapResidenza());
        anagrafePersonaTypeFiduciario.setEmail(recordDtoDpm.getFiduciari2Email());
        if (recordDtoDpm.getFiduciari2ConsensoEmail() != null && !recordDtoDpm.getFiduciari2ConsensoEmail().isBlank()) {
            anagrafePersonaTypeFiduciario.setConsensoEmail(YesNoType.fromValue(recordDtoDpm.getFiduciari2ConsensoEmail()));
        }

        return anagrafePersonaTypeFiduciario;
    }
}
