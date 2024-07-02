/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm.parser.regole;

import it.mds.sdk.libreriaregole.dtos.RecordDtoGenerico;
import it.mds.sdk.libreriaregole.parser.ParserTracciato;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class ParserTracciatoImplTest {

    private static final String FILE_TRACCIATO_DPM = "tracciato-dpm-test.csv";

    @Test
    void validaTracciatoOK() {
        ParserTracciato parserTracciato = new ParserTracciatoImpl();
        Path resourceDirectory = Paths.get("src", "test", "resources");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        File tracciato = new File(absolutePath + FileSystems.getDefault().getSeparator() + FILE_TRACCIATO_DPM);

        List<RecordDtoGenerico> listaRecord = parserTracciato.parseTracciato(tracciato);
        listaRecord.forEach(System.out::println);
        assertFalse(ArrayUtils.isEmpty(listaRecord.toArray()));
    }

}