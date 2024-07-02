/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.parser.regole;

import it.mds.sdk.libreriaregole.parser.ParserRegole;
import it.mds.sdk.libreriaregole.parser.ParserTracciato;
import it.mds.sdk.libreriaregole.regole.beans.RegoleFlusso;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class ValidaFlussoTest {
    private static final String FILE_REGOLE = "regole-dpm.xml";

    @Test
    void validaFlusso() {
        Path resourceDirectory = Paths.get("src", "test", "resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        File regoleXML = new File(absolutePath + FileSystems.getDefault().getSeparator() + FILE_REGOLE);
        ParserRegole parserRegole = new ParserRegoleImpl();
        try {
            jakarta.xml.bind.JAXBContext jaxbContext = JAXBContextFactory
                    .createContext(new Class[]{RegoleFlusso.class}, null);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(parserRegole.parseRegole(new File("non/esisto")), new File(regoleXML.getPath()));
        } catch (JAXBException e) {
            log.debug(e.getMessage(), e);
        }

    }

    @Test
    void parseRegole() {
        ParserTracciato parserTracciato = new ParserTracciatoImpl();
        Path resourceDirectory = Paths.get("src", "test", "resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        File regoleXML = new File(absolutePath + FileSystems.getDefault().getSeparator() + FILE_REGOLE);
        ParserRegole parserRegole = new ParserRegoleImpl();
        RegoleFlusso regoleFlusso = parserRegole.parseRegole(regoleXML);

        try {
            jakarta.xml.bind.JAXBContext jaxbContext = JAXBContextFactory
                    .createContext(new Class[]{RegoleFlusso.class}, null);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(regoleFlusso, new File(regoleXML.getPath()));
        } catch (JAXBException e) {
            log.debug(e.getMessage(), e);
        }
        System.out.println("Parse regole finito!");
    }

}
