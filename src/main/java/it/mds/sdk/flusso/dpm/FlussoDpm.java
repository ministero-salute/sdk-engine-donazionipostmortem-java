/* SPDX-License-Identifier: BSD-3-Clause */

package it.mds.sdk.flusso.dpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
@ComponentScan({"it.mds.sdk.flusso.dpm.controller","it.mds.sdk.flusso.dpm", "it.mds.sdk.rest.persistence.entity","it.mds.sdk.libreriaregole.validator",
                "it.mds.sdk.flusso.dpm.service","it.mds.sdk.flusso.dpm.tracciato","it.mds.sdk.gestoreesiti","it.mds.sdk.flusso.dpm.parser.regole.conf",
                "it.mds.sdk.connettoremds","it.mds.sdk.connettoremds.parser.xml"})
@OpenAPIDefinition(info=@Info(title = "SDK Ministero Della Salute - Flusso DPM", version = "0.0.5-SNAPSHOT", description = "Flusso DPM"))
public class FlussoDpm {
    public static void main(String[] args) {
        SpringApplication.run(FlussoDpm.class, args);
    }
}
