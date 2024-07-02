/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.parser.regole.conf;

import it.mds.sdk.gestoreesiti.conf.Configurazione;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
@Getter
@Component("configurazioneFlussoDpm")
public class ConfigurazioneFlussoDpm {

    Rules rules;
    XmlOutput xmlOutput;
    XmlInput xmlInput;
    Flusso flusso;


    public ConfigurazioneFlussoDpm() {
        this(leggiConfigurazione("config-flusso-dpm.properties"));
    }

    public ConfigurazioneFlussoDpm(final Properties conf) {

        this.rules = ConfigurazioneFlussoDpm.Rules.builder()
                .withPercorso(conf.getProperty("regole.percorso", ""))
                .build();
        this.xmlOutput = ConfigurazioneFlussoDpm.XmlOutput.builder()
                .withPercorso(conf.getProperty("xmloutput.percorso", ""))
                .build();
        this.xmlInput = ConfigurazioneFlussoDpm.XmlInput.builder()
                .withPercorso(conf.getProperty("xmlinput.percorso", ""))
                .build();
        this.flusso = ConfigurazioneFlussoDpm.Flusso.builder()
                .withPercorso(conf.getProperty("flusso.percorso", ""))
                .build();
    }

    @Value
    @Builder(setterPrefix = "with")
    public static class Rules {
        String percorso;
    }

    @Value
    @Builder(setterPrefix = "with")
    public static class XmlOutput {
        String percorso;
    }

    @Value
    @Builder(setterPrefix = "with")
    public static class Flusso {
        String percorso;
    }

    @Value
    @Builder(setterPrefix = "with")
    public static class XmlInput {
        String percorso;
    }


    private static Properties leggiConfigurazione(final String nomeFile) {
        final Properties prop = new Properties();

        if(ConfigurazioneFlussoDpm.class.getClassLoader() == null){
            log.trace("{}.getClassLoader() is null", ConfigurazioneFlussoDpm.class);
            throw new NullPointerException();
        }

        try (final InputStream is = ConfigurazioneFlussoDpm.class.getClassLoader().getResourceAsStream(nomeFile)) {
            prop.load(is);
        } catch (IOException e) {
            log.debug(e.getMessage(), e);
        }
        return prop;
    }
}
