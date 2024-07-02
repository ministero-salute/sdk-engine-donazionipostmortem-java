/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.parser.regole;

import it.mds.sdk.libreriaregole.dtos.RecordDtoGenerico;
import it.mds.sdk.libreriaregole.parser.ParserTracciato;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * La classe implementa l'interfaccia ParserTracciato e il suo metodo parseTracciato(File tracciato)
 */
@Slf4j
@Component("parserTracciato")
public class ParserTracciatoImpl implements ParserTracciato {

    /**
     * Il metodo converte un File.csv in una List<RecordDtoGenerico> usando come separatore "~"
     *
     * @param tracciato, File.csv di input
     * @return una lista di RecordDtoDir
     */
    @Override
    public List<RecordDtoGenerico> parseTracciato(File tracciato) {
        try (FileReader fileReader = new FileReader(tracciato)) {
            return new CsvToBeanBuilder(fileReader)
                    .withType(RecordDtoDpm.class)
                    .withSeparator('~')
                    .withSkipLines(1)   //Salta la prima riga del file CSV
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                    .build()
                    .parse();

        } catch (FileNotFoundException e) {
            log.debug(e.getMessage(), e);
        } catch (IOException e) {
            log.debug(e.getMessage(), e);
        }

        return null;
    }

    @Override
    public List<RecordDtoGenerico> parseTracciatoBlocco(File file, int inizio, int fine) {
        return null;
    }
}
