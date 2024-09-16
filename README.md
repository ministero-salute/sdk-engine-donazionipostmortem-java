# **1. Introduzione**

## ***1.1 Obiettivi del documento***

Il Ministero della Salute (MdS) metterà a disposizione degli Enti, da cui riceve dati, applicazioni SDK specifiche per flusso logico e tenoclogie applicative (Java, PHP e C#) per verifica preventiva (in casa Ente) della qualità del dato prodotto.

![](img/Aspose.Words.70978c07-bd08-414e-a817-6117f6d4a28a.002.png)

Nel presente documento sono fornite la struttura e la sintassi dei tracciati previsti dalla soluzione SDK per avviare il proprio processo elaborativo, nonché i relativi schemi xsd di convalida e i controlli di merito sulla qualità, completezza e coerenza dei dati.

Gli obiettivi del documento sono:

- fornire una descrizione funzionale chiara e consistente dei tracciati di input a SDK;
- fornire le regole funzionali per la verifica di qualità, completezza e coerenza dei dati;
- fornire le linee guida per l’infasamento del dato lato MdS.

In generale, la soluzione SDK è costituita da 2 diversi moduli applicativi (Access Layer e Validation Engine) per abilitare

- l’interoperabilità con il contesto tecnologico dell’Ente in cui la soluzione sarà installata;
- la validazione del dato ed il suo successivo invio verso il MdS.

La figura che segue descrive la soluzione funzionale ed i relativi benefici attesi.

![](img/Aspose.Words.70978c07-bd08-414e-a817-6117f6d4a28a.003.png)

## ***1.2 Acronimi***

Nella tabella riportata di seguito sono elencati tutti gli acronimi e le definizioni adottati nel presente documento.


|**#**|**Acronimo / Riferimento**|**Definizione**|
| - | - | - |
|1|SDK|Software Development Kit|
|2|DPM|Rappresenta i contenuti informativi della disposizione relativa alla donazione post mortem|


# **2 Architettura SDK**
## ***2.1 Architettura funzionale***

Di seguito una rappresentazione del processo di gestione e trasferimento dei flussi dall’ente verso l’area MdS attraverso l’utilizzo dell’applicativo SDK.

![](img/Aspose.Words.70978c07-bd08-414e-a817-6117f6d4a28a.004.png)

Come mostrato in figura, per implementare il processo end-to-end la soluzione SDK per DPM metterà a disposizione dell’applicazione sorgente (Ente) 3 diverse API.

**API 1: validazione scheda donazione.** L’applicazione sorgente (Ente) avrà cura di chiedere a SDK la validazione di una scheda di donazione (richiesta JSON). SDK provvederà a validare il dataset secondo le regole descritte in questo documento, restituendo in modo sincrono all’applicazione sorgente un esito di validazione della scheda (ok/ko).

**API 2: invio file verso MdS.** In un secondo momento (asincrono rispetto al punto 1), l’applicazione sorgente provvederà a richiedere a SDK una trasmissione di un file verso MdS. In tal caso, in input a SDK sarà fornito il file XML generato dell'API 1, firmato (a carico dell'utilizzatore) e SDK provvederà a:
   1. Verificare la correttezza formale del XML (validazione XSD);
   1. Verificare che il dato contenuto nel payload rispetti le regole di business descritte in questo documento;
   1. Verificare che la firma sia formalmente corretta (NOTA: la validazione della firma non sarà gestita lato SDK).


**API 3: verifica stato di elaborazione MdS.** In un terzo momento (asincrono rispetto al punto 2), l’applicazione sorgente provvederà a invocare un servizio SDK per verificare l’effettiva acquisizione della richiesta DPM lato MdS. SDK provvederà a inoltrare la richiesta verso MdS comunicandone l’esito all’applicazione sorgente (pass-through).

**Nota: l’SDK elaborerà un solo file di input per esecuzione.**

In generale, gli step di validazione del dato lato SDK possono essere sintetizzati come segue:

1. Per ogni richiesta proveniente dal sistema sorgente, SDK genererà un identificativo univoco (ID\_RUN).
1. I record del flusso verranno sottoposti alle logiche di validazione e controllo definite nel Validation Engine. Nel processare il dato, Validation Engine acquisirà da MdS eventuali anagrafiche di validazione del dato stesso. Laddove in input venga fornito un file XML, la soluzione SDK controllerà il payload (controlli di business) e di validazione struttura firma (non sarà in carico a SDK il check di validità della firma nell’header dell’XML)
1. Al termine di ogni processo elaborativo SDK produrrà:
   1. File di log contentente il riepilogo dell’esito dell’elaborazione. I file di log saranno memorizzati in cartelle ad hoc e conterranno gli esiti di processamento di tutte le elaborazioni (ID\_RUN).
   1. Produrrà una response verso l’applicazione sorgente contenente l’esito dell’elaborazione (ok oppure lista degli scarti).
1. Ove previsto dalla specifica API, saranno inviati i files al MdS attraverso i servizi già disponibili. MdS esporrà, inoltre, servizi per verificare lo stato della richiesta precedentemente inserita.

## ***2.2 Architettura di integrazione***

Per quanto riguarda l’architettura di integrazione prevista per la gestione del DPM si evidenziano in particolare i seguenti punti:

- Tutti i dati scambiati fra SDK e MdS saranno veicolati tramite Porta di Interoperabilità (PDI);
- Il MdS esporrà servizi (API) per il download di dati anagrafici, verifica acquisizione flusso, verifica dello stato di elaborazione.


# **3. Funzionamento della soluzione SDK**


## ***3.1 Controlli di validazione del dato (business rules)***

Gli errori sono distinti fra scarti (mancato invio del record) e anomalie (invio del record con una segnalazione di warning).

Al verificarsi anche di un solo errore di scarto, tra quelli descritti, il record oggetto di controllo sarà inserito tra i record scartati.

I controlli applicativi saranno implementati a partire dall’acquisizione dei seguenti dati anagrafici, disponibili in ambito MdS, e recuperati con servizi ad hoc (Service Layer mediante PDI), di seguito i nomi logici delle anagrafiche:

- Regioni
- Province
- Comuni
- Codifica nazioni ISO-Alpha2

Il dato anagrafico sarà presente sottoforma di tabella composta da tre colonne:

- Valore (in cui è riportato il dato, nel caso di più valori, sarà usato il carattere # come separatore)
- Data inizio validità (rappresenta la data di inizio validità del campo Valore)

  - Formato: AAAA-MM-DD
  - Notazione inizio validità permanente: **1900-01-01**


- Data Fine Validità (rappresenta la data di fine validità del campo Valore)

  -  Formato: AAAA-MM-DD
  - Notazione fine validità permanente: **9999-12-31**


Le tabelle anagrafiche di questo flusso avranno sempre le date di validità settate con i valori di default (**1900-01-01/9999-12-31**).

Di seguito viene mostrato un caso limite di anagrafica DPM in cui sono presenti dei duplicati:


|ID|VALUE|VALID\_FROM|VALID\_TO|
| - | - | - | - |
|1|VALORE 1|1900-01-01|9999-12-31|
|2|VALORE 1|1900-01-01|9999-12-31|
|3|VALORE 1|1900-01-01|9999-12-31|
|4|VALORE 1|1900-01-01|9999-12-31|

Diremo che il dato presente sul tracciato di input è valido se e solo se:

∃ VALUE\_R = VALUE\_A (esiste almeno un valore valido)


Dove:

- VALUE\_R (rappresenta i campi del tracciato di input coinvolti nei controlli della specifica BR)

- VALUE\_A (rappresenta i campi dell’anagrafica coinvolti nei controlli della specifica BR)

## ***3.2 API 1: validazione scheda donazione***

**Nota**: Per i dettagli tecnici su chiamate e risposte (JSON) consulta il manuale d'integrazione.

Per API 1 è prevista in input una request JSON, contenente i dati relativi alla singola richiesta (donazione o revoca consenso) con il seguente tracciato di input:

|**Nome elemento padre**|**Nome campo**|**Key**|**Descrizione**|**Tipo** |**Obbligatorietà**|**Informazioni di Dominio**|**Lunghezza campo**|**Tracciato di Output**|
| :-: | :-: | :-: | :-: | :-: | :-: | :-: | :-: | :-: |
|dpm|donatoreMinorenne| |Indica se la manifestazione del consenso è relativa ad un minore di età.|A|OBB|Valori ammessi:<br>SI: Se il donatore è un minore di età;<br>NO: Se il donatore è maggiorenne.|2|Donazioni Post Mortem|
|dpm|tipoAtto| |Indica la tipologia delle informazioni trasmettesse alla Banca dati nazionale|A|OBB|Valori ammessi:<br>MANIFESTAZIONE\_CONSENSO: Se i contenuti informativi trasmessi rappresentano un atto di manifestazione del consenso alla donazione;<br>REVOCA\_CONSENSO: Se i contenuti informativi trasmessi rappresentano un atto di revoca del consenso alla donazione precedentemente trasmesso;| |Donazioni Post Mortem|
|dpm|tipoDisponente| |Tipo di disponente|A|OBB|Valori Ammessi:<br>GENITORE: se si dispone il consenso per conto di un minore di età in qualità di genitore esercente la patria potestà<br>TUTORE\_SOGGETTI\_AFFIDATARI: se si dispone il consenso per conto di un minore di età in qualità di tutore o soggetto affidatario ai sensi della legge 4 maggio 1983, n. 184| |Donazioni Post Mortem|
|dpm|formatoAtto| |Indica il formato in cui il disponente ha depositato l’atto presso il soggetto alimentante|A|OBB|Valori ammessi:<br>• FORMA\_SCRITTA<br>• FORMA\_AUDIO\_VIDEO| |Donazioni Post Mortem|
|dpm/donatore/luogoNascita|codiceNazione| |Indica lo Stato presso cui è nato il donatore|A|OBB|La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|2|Donazioni Post Mortem|
|dpm/donatore/luogoNascita|codiceRegione| |Identifica la regione di nascita del donatore|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/donatore/luogoNascita|codiceProvincia| |Identifica la provincia di nascita del donatore|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/donatore/luogoNascita|codiceComune| |Identifica il comune di nascita del donatore|A|OBB|Il codice da utilizzare è quello previsto dalla codifica ISTAT, i cui primi tre caratteri individuano la provincia e i successivi un progressivo all’interno di ciascuna provincia che individua il singolo Comune. Nel caso in cui il disponente è nato all’estero va indicato il codice 999999.|6|Donazioni Post Mortem|
|dpm/donatore/luogoResidenza|codiceNazione| |Indica lo Stato presso cui è residente il donatore|A|OBB|La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|2|Donazioni Post Mortem|
|dpm/donatore/luogoResidenza|codiceRegione| |Identifica la regione di residenza del donatore|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT.<br>Nel caso in cui il disponente è residnete all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/donatore/luogoResidenza|codiceProvincia| |Identifica la provincia di residenza del donatore|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT.<br>Nel caso in cui il disponente è residnete all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/donatore/luogoResidenza|codiceComune| |Comune nella cui anagrafe (Anagrafe della Popolazione Residente) è iscritto il donatore|A|OBB|Il codice da utilizzare è quello previsto dalla codifica ISTAT, i cui primi tre caratteri individuano la provincia e i successivi un progressivo all’interno di ciascuna provincia che individua il singolo Comune.<br>Nel caso in cui il disponente è residente all’estero va indicato il codice 999999.|6|Donazioni Post Mortem|
|dpm/donatore|nome| |Identifica il nome del donatore |A|OBB|Testo libero per indicare il nome del donatore|100|Donazioni Post Mortem|
|dpm/donatore|cognome| |Identifica il cognome del donatore|A|OBB|` `Testo libero per indicare il cognome del donatore|100|Donazioni Post Mortem|
|dpm/donatore|codiceFiscale| |Codice fiscale del donatore|AN|OBB|Formato: codice fiscale a 16 caratteri |16|Donazioni Post Mortem|
|dpm/donatore|dataNascita| |Identifica la data di nascita del donatore|D|OBB|Formato: YYYY-MM-GG|10|Donazioni Post Mortem|
|dpm/donatore|indirizzoResidenza| |Indica l’indirizzo di residenza del donatore.|AN|OBB|Contiene nell’ordine il qualificatore (via, corso, piazza..) il nome della via e il numero civico.|200|Donazioni Post Mortem|
|dpm/donatore|capResidenza| |Indica il CAP di residenza del donatore|N|OBB|Testo numerico per indicare il cap di residenza del donatore|5|Donazioni Post Mortem|
|dpm/disponenti|consensoEmail| |Indica il consenso all’invio della e-mail di notifica di avvenuta registrazione dei contenuti informativi nella banca dati nazionale|A|OBB|Valori ammessi:<br>SI: Se si intende ricevere la notifica via mail;<br>NO: Se non si intende ricevere la notifica via mail.|2|Donazioni Post Mortem|
|dpm/disponenti/luogoNascita|codiceNazione| |Indica lo Stato presso cui è nato il disponente|A|OBB|La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|2|Donazioni Post Mortem|
|dpm/disponenti/luogoNascita|codiceRegione| |Identifica la regione di nascita del disponente|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/disponenti/luogoNascita|codiceProvincia| |Identifica la provincia di nascita del disponente|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/disponenti/luogoNascita|codiceComune| |Identifica il comune di nascita del disponente|A|OBB|Il codice da utilizzare è quello previsto dalla codifica ISTAT, i cui primi tre caratteri individuano la provincia e i successivi un progressivo all’interno di ciascuna provincia che individua il singolo Comune. Nel caso in cui il disponente è nato all’estero va indicato il codice 999999.|6|Donazioni Post Mortem|
|dpm/disponenti/luogoResidenza|codiceNazione| |Indica lo Stato presso cui è residente il disponente|A|OBB|La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|2|Donazioni Post Mortem|
|dpm/disponenti/luogoResidenza|codiceRegione| |Identifica la regione di residenza del disponente|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/disponenti/luogoResidenza|codiceProvincia| |Identifica la provincia di residenza del disponente|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/disponenti/luogoResidenza|codiceComune| |Comune nella cui anagrafe (Anagrafe della Popolazione Residente) è iscritto il disponente|A|OBB|Il codice da utilizzare è quello previsto dalla codifica ISTAT, i cui primi tre caratteri individuano la provincia e i successivi un progressivo all’interno di ciascuna provincia che individua il singolo Comune.<br>Nel caso in cui il disponente è residente all’estero va indicato il codice 999999.|6|Donazioni Post Mortem|
|dpm/disponenti|nome| |Identifica il nome del disponente|A|OBB|Testo libero per indicare il nome del disponente|100|Donazioni Post Mortem|
|dpm/disponenti|cognome| |Identifica il cognome del disponente|A|OBB|Testo libero per indicare il cognome del disponente|100|Donazioni Post Mortem|
|dpm/disponenti|codiceFiscale| |Codice fiscale del disponente|AN|OBB|Formato: codice fiscale a 16 caratteri |16|Donazioni Post Mortem|
|dpm/disponenti|dataNascita| |Identifica la data di nascita del disponente|D|OBB|Formato: YYYY-MM-GG|10|Donazioni Post Mortem|
|dpm/disponenti|indirizzoResidenza| |Indica l’indirizzo di residenza del disponente.|AN|OBB|Contiene nell’ordine il qualificatore (via, corso, piazza..) il nome della via e il numero civico.|100|Donazioni Post Mortem|
|dpm/disponenti|capResidenza| |Indica il CAP di residenza del disponente|N|OBB|Testo numerico per indicare il cap di residenza del donatore|5|Donazioni Post Mortem|
|dpm/disponenti|email| |Indica la mail del disponente alla quale inviare la mail di comunicazione di inserimento dei contenuti informativi nella banca dati nazionale|AN|OBB|Testo libero in formato indirizzo email valido| |Donazioni Post Mortem|
|dpm/disponenti\*\*|consensoEmail| |Indica il consenso all’invio della e-mail di notifica di avvenuta registrazione dei contenuti informativi nella banca dati nazionale|A|OBB|Valori ammessi:<br>SI: Se si intende ricevere la notifica via mail;<br>NO: Se non si intende ricevere la notifica via mail.|2|Donazioni Post Mortem|
|dpm/disponenti\*\*/luogoNascita|codiceNazione| |Indica lo Stato presso cui è nato il disponente|A|OBB|La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|2|Donazioni Post Mortem|
|dpm/disponenti\*\*/luogoNascita|codiceRegione| |Identifica la regione di nascita del disponente|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/disponenti\*\*/luogoNascita|codiceProvincia| |Identifica la provincia di nascita del disponente|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/disponenti\*\*/luogoNascita|codiceComune| |Identifica il comune di nascita del disponente|A|OBB|Il codice da utilizzare è quello previsto dalla codifica ISTAT, i cui primi tre caratteri individuano la provincia e i successivi un progressivo all’interno di ciascuna provincia che individua il singolo Comune. Nel caso in cui il disponente è nato all’estero va indicato il codice 999999.|6|Donazioni Post Mortem|
|dpm/disponenti\*\*/luogoResidenza|codiceNazione| |Indica lo Stato presso cui è residente il disponente|A|OBB|La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|2|Donazioni Post Mortem|
|dpm/disponenti\*\*/luogoResidenza|codiceRegione| |Identifica la regione di residenza del disponente|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/disponenti\*\*/luogoResidenza|codiceProvincia| |Identifica la provincia di residenza del disponente|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/disponenti\*\*/luogoResidenza|codiceComune| |Comune nella cui anagrafe (Anagrafe della Popolazione Residente) è iscritto il disponente|A|OBB|Il codice da utilizzare è quello previsto dalla codifica ISTAT, i cui primi tre caratteri individuano la provincia e i successivi un progressivo all’interno di ciascuna provincia che individua il singolo Comune.<br>Nel caso in cui il disponente è residente all’estero va indicato il codice 999999.|6|Donazioni Post Mortem|
|dpm/disponenti\*\*|nome| |Identifica il nome del disponente|A|OBB|Testo libero per indicare il nome del disponente|100|Donazioni Post Mortem|
|dpm/disponenti\*\*|cognome| |Identifica il cognome del disponente|A|OBB|Testo libero per indicare il cognome del disponente|100|Donazioni Post Mortem|
|dpm/disponenti\*\*|codiceFiscale| |Codice fiscale del disponente|AN|OBB|Formato: codice fiscale a 16 caratteri |16|Donazioni Post Mortem|
|dpm/disponenti\*\*|dataNascita| |Identifica la data di nascita del disponente|D|OBB|Formato: YYYY-MM-GG|10|Donazioni Post Mortem|
|dpm/disponenti\*\*|indirizzoResidenza| |Indica l’indirizzo di residenza del disponente.|AN|OBB|Contiene nell’ordine il qualificatore (via, corso, piazza..) il nome della via e il numero civico.|100|Donazioni Post Mortem|
|dpm/disponenti\*\*|capResidenza| |Indica il CAP di residenza del disponente|N|OBB|Testo numerico per indicare il cap di residenza del donatore|5|Donazioni Post Mortem|
|dpm/disponenti\*\*|email| |Indica la mail del disponente alla quale inviare la mail di comunicazione di inserimento dei contenuti informativi nella banca dati nazionale|AN|OBB|Testo libero in formato indirizzo email valido| |Donazioni Post Mortem|
|dpm/fiduciari|consensoEmail| |Indica il consenso all’invio della e-mail di notifica di avvenuta registrazione dei contenuti informativi nella banca dati nazionale|A|OBB|Valori ammessi:<br>SI: Se si intende ricevere la notifica via mail;<br>NO: Se non si intende ricevere la notifica via mail.|2|Donazioni Post Mortem|
|dpm/fiduciari/luogoNascita|codiceNazione| |Indica lo Stato presso cui è nato il fiduciario|A|OBB|La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|2|Donazioni Post Mortem|
|dpm/fiduciari/luogoNascita|codiceRegione| |Identifica la regione di nascita del fiduciario|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/fiduciari/luogoNascita|codiceProvincia| |Identifica la provincia di nascita del fiduciario|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/fiduciari/luogoNascita|codiceComune| |Identifica il comune di nascita del fiduciario|A|OBB|Il codice da utilizzare è quello previsto dalla codifica ISTAT, i cui primi tre caratteri individuano la provincia e i successivi un progressivo all’interno di ciascuna provincia che individua il singolo Comune. Nel caso in cui il disponente è nato all’estero va indicato il codice 999999.|6|Donazioni Post Mortem|
|dpm/fiduciari/luogoResidenza|codiceNazione| |Indica lo Stato presso cui è residente il fiduciario|A|OBB|La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|2|Donazioni Post Mortem|
|dpm/fiduciari/luogoResidenza|codiceRegione| |Identifica la regione di residenza del fiduciario|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/fiduciari/luogoResidenza|codiceProvincia| |Identifica la provincia di residenza del fiduciario|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/fiduciari/luogoResidenza|codiceComune| |Comune nella cui anagrafe (Anagrafe della Popolazione Residente) è iscritto il fiduciario|A|OBB|Il codice da utilizzare è quello previsto dalla codifica ISTAT, i cui primi tre caratteri individuano la provincia e i successivi un progressivo all’interno di ciascuna provincia che individua il singolo Comune. Nel caso in cui il disponente è nato all’estero va indicato il codice 999999.|6|Donazioni Post Mortem|
|dpm/fiduciari|nome| |Identifica il nome del fiduciario|AN|OBB|Testo libero per indicare il nome del fiduciario|100|Donazioni Post Mortem|
|dpm/fiduciari|cognome| |Identifica il cognome del fiduciario|AN|OBB|` `Testo libero per indicare il cognome del fiduciario|100|Donazioni Post Mortem|
|dpm/fiduciari|codiceFiscale| |Codice fiscale del fiduciario|AN|OBB|Formato: codice fiscale a 16 caratteri |16|Donazioni Post Mortem|
|dpm/fiduciari|dataNascita| |Identifica la data di nascita del fiduciario|D|OBB|Formato: YYYY-MM-GG|10|Donazioni Post Mortem|
|dpm/fiduciari|indirizzoResidenza| |Indica l’indirizzo di residenza del fiduciario.|AN|OBB|Contiene nell’ordine il qualificatore (via, corso, piazza..) il nome della via e il numero civico.|200|Donazioni Post Mortem|
|dpm/fiduciari|capResidenza| |Indica il CAP di residenza del fiduciario|N|OBB|Testo numerico per indicare il cap di residenza del fiduciario|5|Donazioni Post Mortem|
|dpm/fiduciari|email| |Indica la mail del fiduciario alla quale inviare la mail di comunicazione di inserimento dei contenuti informativi nella banca dati nazionale|AN|OBB|Testo libero in formato indirizzo email valido| |Donazioni Post Mortem|
|dpm/fiduciari\*\*\*|consensoEmail| |Indica il consenso all’invio della e-mail di notifica di avvenuta registrazione dei contenuti informativi nella banca dati nazionale|A|OBB|Valori ammessi:<br>SI: Se si intende ricevere la notifica via mail;<br>NO: Se non si intende ricevere la notifica via mail.|2|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*/luogoNascita|codiceNazione| |Indica lo Stato presso cui è nato il fiduciario|A|OBB|La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|2|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*/luogoNascita|codiceRegione| |Identifica la regione di nascita del fiduciario|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*/luogoNascita|codiceProvincia| |Identifica la provincia di nascita del fiduciario|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*/luogoNascita|codiceComune| |Identifica il comune di nascita del fiduciario|A|OBB|Il codice da utilizzare è quello previsto dalla codifica ISTAT, i cui primi tre caratteri individuano la provincia e i successivi un progressivo all’interno di ciascuna provincia che individua il singolo Comune. Nel caso in cui il disponente è nato all’estero va indicato il codice 999999.|6|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*/luogoResidenza|codiceNazione| |Indica lo Stato presso cui è residente il fiduciario|A|OBB|La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|2|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*/luogoResidenza|codiceRegione| |Identifica la regione di residenza del fiduciario|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*/luogoResidenza|codiceProvincia| |Identifica la provincia di residenza del fiduciario|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|3|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*/luogoResidenza|codiceComune| |Comune nella cui anagrafe (Anagrafe della Popolazione Residente) è iscritto il fiduciario|A|OBB|Il codice da utilizzare è quello previsto dalla codifica ISTAT, i cui primi tre caratteri individuano la provincia e i successivi un progressivo all’interno di ciascuna provincia che individua il singolo Comune. Nel caso in cui il disponente è nato all’estero va indicato il codice 999999.|6|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*|nome| |Identifica il nome del fiduciario|AN|OBB|Testo libero per indicare il nome del fiduciario|100|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*|cognome| |Identifica il cognome del fiduciario|AN|OBB|` `Testo libero per indicare il cognome del fiduciario|100|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*|codiceFiscale| |Codice fiscale del fiduciario|AN|OBB|Formato: codice fiscale a 16 caratteri |16|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*|dataNascita| |Identifica la data di nascita del fiduciario|D|OBB|Formato: YYYY-MM-GG|10|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*|indirizzoResidenza| |Indica l’indirizzo di residenza del fiduciario.|AN|OBB|Contiene nell’ordine il qualificatore (via, corso, piazza..) il nome della via e il numero civico.|200|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*|capResidenza| |Indica il CAP di residenza del fiduciario|N|OBB|Testo numerico per indicare il cap di residenza del fiduciario|5|Donazioni Post Mortem|
|dpm/fiduciari\*\*\*|email| |Indica la mail del fiduciario alla quale inviare la mail di comunicazione di inserimento dei contenuti informativi nella banca dati nazionale|AN|OBB|Testo libero in formato indirizzo email valido| |Donazioni Post Mortem|
|dpm/soggettoAlimentante|regioneSoggettoAlimentante| |Identifica la regione di competenza della struttura|A|OBB|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT.|3|Donazioni Post Mortem|
|dpm/soggettoAlimentante|cap| |Indica il CAP della struttura|N|OBB|Testo numerico per indicare il cap|5|Donazioni Post Mortem|
|dpm/soggettoAlimentante|codiceSoggettoAlimentante| |Codice della struttura sanitaria che trasmette i contenuti informativi|A|OBB|Valori di riferimento:<br>Ciascun codice è composto da 6 caratteri dei quali i primi 3 identificano la regione di appartenenza, i successivi 3 sono costituiti da un progressivo numerico attribuito in ambito regionale.|6|Donazioni Post Mortem|
|dpm/soggettoAlimentante|indirizzo| |Indica l’indirizzo della struttura|AN|OBB|Contiene nell’ordine il qualificatore (via, corso, piazza..) il nome della via e il numero civico.|200|Donazioni Post Mortem|
|dpm|identificativoSoggettoAlimentante|Y|Identificativo univoco che identifica la trasmissione dei contenuti informativi presso il soggetto alimentante|A|OBB|Il valore deve corrispondere a quello indicato nel metadato <be:idSoggettoAlimentante> nella richiesta SOAP|100|Donazioni Post Mortem|
|dpm|dataSottoscrizione| |Data in cui l’atto dispositivo è stato sottoscritto presso una struttura sanitaria |D|OBB|Formato YYYY-MM-DD|10|Donazioni Post Mortem|



Acquisito il dato di input, il Validation Engine di SDK procederà ad implementare i controlli descritti nella tabella riportata di seguito:

|**CAMPO**|**CAMPO PADRE**|**CAMPO FIGLIO**|**CODICE ERRORE**|**DESCRIZIONE ERRORE**|**DESCRIZIONE ALGORITMO**|**TABELLA ANAGRAFICA**|**APPROFONDIMENTO**|**INTEGRAZIONI ALLA REGOLA**|**CAMPI DI COERENZA**|
| :- | :- | :- | :- | :- | :-: | :-: | :-: | :-: | :-: |
|dpm/donatoreMinorenne|dpm|donatoreMinorenne|5001|Codice non presente nel dominio di riferimento|Valori ammessi:<br>SI: Se il donatore è un minore di età;<br>NO: Se il donatore è maggiorenne.| |I valori del campo del tracciato di input dpmdonatoreMinorenne ammessi sono: SI, NO|Se il donatore è minorenne controllare la data di sottoscrizione che siano <= 17 anni<br>Gli altri (disponenti e fiduciari) sempre MAGGIORENNI| |
|dpm/tipoAtto|dpm|tipoAtto|5001|Codice non presente nel dominio di riferimento|Valori diversi da quelli ammessi:<br>MANIFESTAZIONE\_CONSENSO: Se i contenuti informativi trasmessi rappresentano un atto di manifestazione del consenso alla donazione;<br>REVOCA\_CONSENSO: Se i contenuti informativi trasmessi rappresentano un atto di revoca del consenso alla donazione precedentemente trasmesso;| |I valori del campo del tracciato di input dpmtipoAtto ammessi sono: MANIFESTAZIONE\_CONSENSO, REVOCA\_CONSENSO| | |
|dpm/tipoDisponente|dpm|tipoDisponente|5001|Codice non presente nel dominio di riferimento|Valori diversi da quelli ammessi:<br>GENITORE: se si dispone il consenso per conto di un minore di età in qualità di genitore esercente la patria potestà<br>TUTORE\_SOGGETTI\_AFFIDATARI: se si dispone il consenso per conto di un minore di età in qualità di tutore o soggetto affidatario ai sensi della legge 4 maggio 1983, n. 184| |I valori del campo tracciato di input dpmtipoDisponente ammessi sono: GENITORE, TUTORE\_SOGGETTI\_AFFIDATARI| | |
|dataSottoscrizione|dpm|dataSottoscrizione|5000|Tipolo dato errato o formato data errato|Il campo deve essere valorizzato con il formato data AAAA-MM-GG| |Il campo deve essere valorizzato con il formato data AAAA-MM-GG| | |
|dpm/formatoAtto|dpm|formatoAtto|5600|Codice non presente nel dominio di riferimento| | |I valori del campo tracciato di input dpmformatoAtto ammessi sono: FORMA\_SCRITTA, FORMA\_AUDIO\_VIDEO| | |
|dataSottoscrizione|dpm|dataSottoscrizione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/donatoreMinorenne|dpm|donatoreMinorenne|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/idSoggettoAlimentante|dpm|identificativoSoggettoAlimentante|5300|Formato errato|Il codice è di lunghezza superiore a 100 caratteri o contiene caratteri diversi da lettere| |Il campo del tracciato di input dpmidentificativoSoggettoAlimentante deve essere lungo al massimo 100 caratteri alfanumerici| | |
|dpm/idSoggettoAlimentante|dpm|identificativoSoggettoAlimentante|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/tipoAtto|dpm|tipoAtto|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/tipoDisponente|dpm|tipoDisponente|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[2]/capResidenza|dpm/disponenti\*\*|capResidenza|5006|Formato del CAP errato|CAP non valido| |CAP è obbligatorio (presente e diverso da blanks) e deve contenere 5 caratteri numerici| | |
|dpm/disponenti[1]/capResidenza|dpm/disponenti\*\*|capResidenza|5006|Formato del CAP errato|CAP non valido| |CAP è obbligatorio (presente e diverso da blanks) e deve contenere 5 caratteri numerici| | |
|dpm/disponenti[1]/dataNascita|dpm/disponenti\*\*|dataNascita|5000|Tipolo dato errato o formato data errato|Formato: AAAA-MM-GG| |Il formato della data deve corrispondere ad AAAA-MM-GG| | |
|dpm/disponenti[2]/dataNascita|dpm/disponenti\*\*|dataNascita|5000|Tipolo dato errato o formato data errato|Formato: AAAA-MM-GG| |Il formato della data deve corrispondere ad AAAA-MM-GG| | |
|dpm/disponenti[1]/consensoEmail|dpm/disponenti\*\*|consensoEmail|5001|Codice non presente nel dominio di riferimento|Valori diversi da quelli ammessi:<br>SI: Se si intende ricevere la notifica via mail;<br>NO: Se non si intende ricevere la notifica via mail.| |I valori del campo del tracciato di input dpm/disponenti\*\*consensoEmail ammessi sono: SI, NO| | |
|dpm/disponenti[2]/consensoEmail|dpm/disponenti\*\*|consensoEmail|5001|Codice non presente nel dominio di riferimento|Valori diversi da quelli ammessi:<br>SI: Se si intende ricevere la notifica via mail;<br>NO: Se non si intende ricevere la notifica via mail.| |I valori del campo del tracciato di input dpm/disponenti\*\*consensoEmail ammessi sono: SI, NO| | |
|dpm/disponenti[2]/capResidenza|dpm/disponenti\*\*|capResidenza|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/capResidenza|dpm/disponenti\*\*|capResidenza|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/codiceFiscale|dpm/disponenti\*\*|codiceFiscale|5003|Lunghezza diversa da quella aspettata|Formato: codice fiscale a 16 caratteri | |Il codice è di lunghezza superiore o inferiore a 16 caratteri alfanumerici|Controllo che sia coerente con data di nascita e che l'ultimo carattere sia corretto. Ha classe| |
|dpm/disponenti[2]/codiceFiscale|dpm/disponenti\*\*|codiceFiscale|5003|Lunghezza diversa da quella aspettata|Formato: codice fiscale a 16 caratteri | |Il codice è di lunghezza superiore o inferiore a 16 caratteri alfanumerici|Controllo che sia coerente con data di nascita e che l'ultimo carattere sia corretto. Ha classe| |
|dpm/disponenti[1]/codiceFiscale|dpm/disponenti\*\*|codiceFiscale|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)|Controllo che sia coerente con data di nascita e che l'ultimo carattere sia corretto. Ha classe| |
|dpm/disponenti[2]/codiceFiscale|dpm/disponenti\*\*|codiceFiscale|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)|Controllo che sia coerente con data di nascita e che l'ultimo carattere sia corretto. Ha classe| |
|dpm/disponenti[1]/cognome|dpm/disponenti\*\*|cognome|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[2]/cognome|dpm/disponenti\*\*|cognome|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/consensoEmail|dpm/disponenti\*\*|consensoEmail|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[2]/consensoEmail|dpm/disponenti\*\*|consensoEmail|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/dataNascita|dpm/disponenti\*\*|dataNascita|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[2]/dataNascita|dpm/disponenti\*\*|dataNascita|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/email|dpm/disponenti\*\*|email|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[2]/email|dpm/disponenti\*\*|email|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/indirizzoResidenza|dpm/disponenti\*\*|indirizzoResidenza|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[2]/indirizzoResidenza|dpm/disponenti\*\*|indirizzoResidenza|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/nome|dpm/disponenti\*\*|nome|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[2]/nome|dpm/disponenti\*\*|nome|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/luogoNascita/codiceNazione|dpm/disponenti/luogoNascita|codiceNazione|5000|Codice non presente nel dominio di riferimento|Valori di versi da quelli ammessi.<br>La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|Codifica nazioni ISO-Alpha2|Il campo dpm/disponenti/luogoNascitacodiceNazione del tracciato di input deve essere presente nel campo **COD\_NAZ** dell'anagrafica filtrata con dpm/disponentidatadiNascita tra le date at\_ini\_vld, dat\_end\_vld, INTEGRATA con i valori extra "XK", "XX", "ZZ".<br>Il campo dpm/disponenti/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/disponenti[2]/luogoNascita/codiceNazione|dpm/disponenti/luogoNascita|codiceNazione|5000|Codice non presente nel dominio di riferimento|Valori di versi da quelli ammessi.<br>La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|Codifica nazioni ISO-Alpha2|Il campo dpm/disponenti/luogoNascitacodiceNazione del tracciato di input deve essere presente nel campo **COD\_NAZ** dell'anagrafica filtrata con dpm/disponentidatadiNascita tra le date at\_ini\_vld, dat\_end\_vld, INTEGRATA con i valori extra "XK", "XX", "ZZ".<br>Il campo dpm/disponenti/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/disponenti[1]/luogoNascita/codiceRegione|dpm/disponenti/luogoNascita|codiceRegione|5002|Codice regione non presente nell'anagrafica di riferimento|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|Regioni|Il campo del tracciato di input dpm/disponenti/luogoNascitacodiceRegione deve esistere nel campo **COD\_REG** dell'anagrafica Regioni nel caso in cui il dpm/disponenti/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri)<br>Il campo dpm/disponenti/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/disponenti[2]/luogoNascita/codiceRegione|dpm/disponenti/luogoNascita|codiceRegione|5002|Codice regione non presente nell'anagrafica di riferimento|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|Regioni|Il campo del tracciato di input dpm/disponenti/luogoNascitacodiceRegione deve esistere nel campo **COD\_REG** dell'anagrafica Regioni nel caso in cui il dpm/disponenti/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri)<br>Il campo dpm/disponenti/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/disponenti[1]/luogoNascita/codiceProvincia|dpm/disponenti/luogoNascita|codiceProvincia|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[2]/luogoNascita/codiceProvincia|dpm/disponenti/luogoNascita|codiceProvincia|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/luogoNascita/codiceProvincia|dpm/disponenti/luogoNascita|codiceProvincia|5050|Regione di Residenza incoerente con Comune o ASL  di Residenza|Il codice è valorizzaro e diverso da 999 e nel dominio e il codice  Comune di residenza è non nullo e valorizzato con 999999 e/o  la Asl di residenza è non nulla e valorizzata con 999 oppure sono non nulli e valorizzati con valori che non afferiscono alla regione di residenza.|Province|Il campo del tracciato di input dpm/disponenti/luogoNascitacodiceProvincia deve esistere nel campo **COD\_PROV** dell'anagrafica Regioni nel caso in cui il dpm/disponenti/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri).<br>Il campo dpm/disponenti/luogoNascitacodiceRegione deve essere presente nel campo **COD\_REG** dell'anagrafica delle Regioni per la provincia in oggetto.<br>Il campo dpm/disponenti/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| |Codice provincia/residenza|
|dpm/disponenti[2]/luogoNascita/codiceProvincia|dpm/disponenti/luogoNascita|codiceProvincia|5050|Regione di Residenza incoerente con Comune o ASL  di Residenza|Il codice è valorizzaro e diverso da 999 e nel dominio e il codice  Comune di residenza è non nullo e valorizzato con 999999 e/o  la Asl di residenza è non nulla e valorizzata con 999 oppure sono non nulli e valorizzati con valori che non afferiscono alla regione di residenza.|Province|Il campo del tracciato di input dpm/disponenti/luogoNascitacodiceProvincia deve esistere nel campo **COD\_PROV** dell'anagrafica Regioni nel caso in cui il dpm/disponenti/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri).<br>Il campo dpm/disponenti/luogoNascitacodiceRegione deve essere presente nel campo **COD\_REG** dell'anagrafica delle Regioni per la provincia in oggetto.<br>Il campo dpm/disponenti/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| |Codice provincia/residenza|
|dpm/disponenti[1]/luogoNascita/codiceComune|dpm/disponenti/luogoNascita|codiceComune|5010|Codice non presente nel dominio di riferimento| |Comuni|Il campo del tracciato di input dpm/disponenti/luogoNascitacodiceComune deve esistere nel campo **COD\_CMN** dell'anagrafica Comuni nel caso in cui il dpm/disponenti/luogocodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999999" (per gli esteri).<br>Il campo dpm/disponenti/luogoNascitacodiceRegione deve essere presente nel campo **COD\_REG** dell'anagrafica delle Regioni per il comune in oggetto.<br>Il campo dpm/disponenti/luogoNascitacodiceProvincia deve essere presente nel campo **COD\_PRV** dell'anagrafica delle Province per il comune in oggetto.<br>Il campo dpm/disponenti/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/disponenti[2]/luogoNascita/codiceComune|dpm/disponenti/luogoNascita|codiceComune|5010|Codice non presente nel dominio di riferimento| |Comuni|Il campo del tracciato di input dpm/disponenti/luogoNascitacodiceComune deve esistere nel campo **COD\_CMN** dell'anagrafica Comuni nel caso in cui il dpm/disponenti/luogocodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999999" (per gli esteri).<br>Il campo dpm/disponenti/luogoNascitacodiceRegione deve essere presente nel campo **COD\_REG** dell'anagrafica delle Regioni per il comune in oggetto.<br>Il campo dpm/disponenti/luogoNascitacodiceProvincia deve essere presente nel campo **COD\_PRV** dell'anagrafica delle Province per il comune in oggetto.<br>Il campo dpm/disponenti/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/disponenti[1]/luogoNascita/codiceComune|dpm/disponenti/luogoNascita|codiceComune|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[2]/luogoNascita/codiceComune|dpm/disponenti/luogoNascita|codiceComune|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/luogoNascita/codiceNazione|dpm/disponenti/luogoNascita|codiceNazione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[2]/luogoNascita/codiceNazione|dpm/disponenti/luogoNascita|codiceNazione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/luogoNascita/codiceRegione|dpm/disponenti/luogoNascita|codiceRegione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[2]/luogoNascita/codiceRegione|dpm/disponenti/luogoNascita|codiceRegione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/luogoResidenza/codiceNazione|dpm/disponenti/luogoResidenza|codiceNazione|5000|Codice non presente nel dominio di riferimento|Valori di versi da quelli ammessi.<br>La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|Codifica nazioni ISO-Alpha2|Il campo dpm/disponenti/luogoNascitacodiceNazione del tracciato di input deve essere presente nel campo COD\_NAZ dell'anagrafica filtrata con dpm/disponentidatadiSottoscrizione tra le date at\_ini\_vld, dat\_end\_vld, INTEGRATA con i valori extra "XK", "XX", "ZZ".| | |
|dpm/disponenti[2]/luogoResidenza/codiceNazione|dpm/disponenti/luogoResidenza|codiceNazione|5000|Codice non presente nel dominio di riferimento|Valori di versi da quelli ammessi.<br>La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|Codifica nazioni ISO-Alpha2|Il campo dpm/disponenti/luogoNascitacodiceNazione del tracciato di input deve essere presente nel campo COD\_NAZ dell'anagrafica filtrata con dpm/disponentidatadiSottoscrizione tra le date at\_ini\_vld, dat\_end\_vld, INTEGRATA con i valori extra "XK", "XX", "ZZ".| | |
|dpm/disponenti[1]/luogoResidenza/codiceRegione|dpm/disponenti/luogoResidenza|codiceRegione|5002|Codice regione non presente nell'anagrafica di riferimento|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|Regioni|Il campo del tracciato di input dpm/disponenti/luogoNascitacodiceRegione deve esistere nel campo COD\_REG dell'anagrafica Regioni nel caso in cui il dpm/disponenti/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri)<br>Il campo dpm/disponenti/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/disponenti[2]/luogoResidenza/codiceRegione|dpm/disponenti/luogoResidenza|codiceRegione|5002|Codice regione non presente nell'anagrafica di riferimento|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|Regioni|Il campo del tracciato di input dpm/disponenti/luogoNascitacodiceRegione deve esistere nel campo COD\_REG dell'anagrafica Regioni nel caso in cui il dpm/disponenti/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri)<br>Il campo dpm/disponenti/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/disponenti[2]/luogoResidenza/codiceProvincia|dpm/disponenti/luogoResidenza|codiceProvincia|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/luogoResidenza/codiceProvincia|dpm/disponenti/luogoResidenza|codiceProvincia|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/luogoResidenza/codiceProvincia|dpm/disponenti/luogoResidenza|codiceProvincia|5050|Regione di Residenza incoerente con Comune o ASL  di Residenza|Il codice è valorizzaro e diverso da 999 e nel dominio e il codice  Comune di residenza è non nullo e valorizzato con 999999 e/o  la Asl di residenza è non nulla e valorizzata con 999 oppure sono non nulli e valorizzati con valori che non afferiscono alla regione di residenza.|Province|Il campo del tracciato di input dpm/disponenti/luogoNascitacodiceProvincia deve esistere nel campo COD\_PROV dell'anagrafica Regioni nel caso in cui il dpm/disponenti/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri).<br>Il campo dpm/disponenti/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per la provincia in oggetto.<br>Il campo dpm/disponenti/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| |Codice provincia/residenza|
|dpm/disponenti[2]/luogoResidenza/codiceProvincia|dpm/disponenti/luogoResidenza|codiceProvincia|5050|Regione di Residenza incoerente con Comune o ASL  di Residenza|Il codice è valorizzaro e diverso da 999 e nel dominio e il codice  Comune di residenza è non nullo e valorizzato con 999999 e/o  la Asl di residenza è non nulla e valorizzata con 999 oppure sono non nulli e valorizzati con valori che non afferiscono alla regione di residenza.|Province|Il campo del tracciato di input dpm/disponenti/luogoNascitacodiceProvincia deve esistere nel campo COD\_PROV dell'anagrafica Regioni nel caso in cui il dpm/disponenti/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri).<br>Il campo dpm/disponenti/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per la provincia in oggetto.<br>Il campo dpm/disponenti/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| |Codice provincia/residenza|
|dpm/disponenti[1]/luogoResidenza/codiceComune|dpm/disponenti/luogoResidenza|codiceComune|5010|Lunghezza diversa da quella attesa|La lunghezza è diversa da 6 caratteri|Comuni|Il campo del tracciato di input dpm/disponenti/luogoNascitacodiceComune deve esistere nel campo COD\_CMN dell'anagrafica Comuni nel caso in cui il dpm/disponenti/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999999" (per gli esteri).<br>Il campo dpm/disponenti/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per il comune in oggetto.<br>Il campo dpm/disponenti/luogoNascitacodiceProvincia deve essere presente nel campo COD\_PRV dell'anagrafica delle Province per il comune in oggetto.<br>Il campo dpm/disponenti/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/disponenti[2]/luogoResidenza/codiceComune|dpm/disponenti/luogoResidenza|codiceComune|5010|Lunghezza diversa da quella attesa|La lunghezza è diversa da 6 caratteri|Comuni|Il campo del tracciato di input dpm/disponenti/luogoNascitacodiceComune deve esistere nel campo COD\_CMN dell'anagrafica Comuni nel caso in cui il dpm/disponenti/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999999" (per gli esteri).<br>Il campo dpm/disponenti/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per il comune in oggetto.<br>Il campo dpm/disponenti/luogoNascitacodiceProvincia deve essere presente nel campo COD\_PRV dell'anagrafica delle Province per il comune in oggetto.<br>Il campo dpm/disponenti/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/disponenti[1]/luogoResidenza/codiceComune|dpm/disponenti/luogoResidenza|codiceComune|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[2]/luogoResidenza/codiceComune|dpm/disponenti/luogoResidenza|codiceComune|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/luogoResidenza/codiceNazione|dpm/disponenti/luogoResidenza|codiceNazione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/luogoResidenza/codiceNazione|dpm/disponenti/luogoResidenza|codiceNazione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[1]/luogoResidenza/codiceRegione|dpm/disponenti/luogoResidenza|codiceRegione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/disponenti[2]/luogoResidenza/codiceRegione|dpm/disponenti/luogoResidenza|codiceRegione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/donatore/capResidenza|dpm/donatore\*|capResidenza|5006|Formato del CAP errato|CAP non valido| |CAP è obbligatorio (presente e diverso da blanks) e deve contenere 5 caratteri numerici| | |
|dpm/donatore/capResidenza|dpm/donatore\*|capResidenza|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/donatore/codiceFiscale|dpm/donatore\*|codiceFiscale|5003|Lunghezza diversa da quella aspettata|Formato: codice fiscale a 16 caratteri | |Il codice è di lunghezza superiore o inferiore a 16 caratteri alfanumerici|Controllo che sia coerente con data di nascita e che l'ultimo carattere sia corretto. Ha classe| |
|dpm/donatore/codiceFiscale|dpm/donatore\*|codiceFiscale|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)|Controllo che sia coerente con data di nascita e che l'ultimo carattere sia corretto. Ha classe| |
|dpm/donatore/cognome|dpm/donatore\*|cognome|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/donatore/dataNascita|dpm/donatore\*|dataNascita|5000|Tipolo dato errato o formato data errato|Il campo deve essere valorizzato con il formato data AAAA-MM-GG| |Il formato della data deve corrispondere ad AAAA-MM-GG| | |
|dpm/donatore/dataNascita|dpm/donatore\*|dataNascita|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/donatore/indirizzoResidenza|dpm/donatore\*|indirizzoResidenza|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/donatore/nome|dpm/donatore\*|nome|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/donatore/luogoNascita/codiceNazione|dpm/donatore/luogoNascita|codiceNazione|5000|Codice non presente nel dominio di riferimento|Valori di versi da quelli ammessi.<br>La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|Codifica nazioni ISO-Alpha2|Il campo dpm/donatore/luogoNascitacodiceNazione del tracciato di input deve essere presente nel campo COD\_NAZ dell'anagrafica filtrata con dpm/donatoredatadiNascita tra le date at\_ini\_vld, dat\_end\_vld, INTEGRATA con i valori extra "XK", "XX", "ZZ".<br>Il campo dpm/donatore/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/donatore/luogoNascita/codiceRegione|dpm/donatore/luogoNascita|codiceRegione|5002|Codice regione non presente nell'anagrafica di riferimento|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|Regioni|Il campo del tracciato di input dpm/donatore/luogoNascitacodiceRegione deve esistere nel campo COD\_REG dell'anagrafica Regioni nel caso in cui il dpm/donatore/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri)<br>Il campo dpm/donatore/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/donatore/luogoNascita/codiceProvincia|dpm/donatore/luogoNascita|codiceProvincia|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Il campo dpm/donatore/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/donatore/luogoNascita/codiceProvincia|dpm/donatore/luogoNascita|codiceProvincia|5050|Regione di Residenza incoerente con Comune o ASL  di Residenza|Il codice è valorizzaro e diverso da 999 e nel dominio e il codice  Comune di residenza è non nullo e valorizzato con 999999 e/o  la Asl di residenza è non nulla e valorizzata con 999 oppure sono non nulli e valorizzati con valori che non afferiscono alla regione di residenza.|Province|Il campo del tracciato di input dpm/donatore/luogoNascitacodiceProvincia deve esistere nel campo COD\_PROV dell'anagrafica Regioni nel caso in cui il dpm/donatore/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri).<br>Il campo dpm/donatore/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per la provincia in oggetto.<br>Il campo dpm/donatore/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| |Codice provincia/residenza|
|dpm/donatore/luogoNascita/codiceComune|dpm/donatore/luogoNascita|codiceComune|5010|Codice non presente nel dominio di riferimento|La lunghezza è diversa da 6 caratteri|Comuni|Il campo del tracciato di input dpm/donatore/luogoNascitacodiceComune deve esistere nel campo COD\_CMN dell'anagrafica Comuni nel caso in cui il dpm/donatore/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999999" (per gli esteri).<br>Il campo dpm/donatore/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per il comune in oggetto.<br>Il campo dpm/donatore/luogoNascitacodiceProvincia deve essere presente nel campo COD\_PRV dell'anagrafica delle Province per il comune in oggetto.<br>Il campo dpm/donatore/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/donatore/luogoNascita/codiceComune|dpm/donatore/luogoNascita|codiceComune|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/donatore/luogoNascita/codiceNazione|dpm/donatore/luogoNascita|codiceNazione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/donatore/luogoNascita/codiceRegione|dpm/donatore/luogoNascita|codiceRegione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/donatore/luogoResidenza/codiceNazione|dpm/donatore/luogoResidenza|codiceNazione|5000|Codice non presente nel dominio di riferimento|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|Codifica nazioni ISO-Alpha2|Il campo dpm/donatore/luogoNascitacodiceNazione del tracciato di input deve essere presente nel campo COD\_NAZ dell'anagrafica filtrata con dpm/donatoredatadiNascita tra le date at\_ini\_vld, dat\_end\_vld, INTEGRATA con i valori extra "XK", "XX", "ZZ".<br>Il campo dpm/donatore/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/donatore/luogoResidenza/codiceRegione|dpm/donatore/luogoResidenza|codiceRegione|5002|Codice regione non presente nell'anagrafica di riferimento|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT.<br>Nel caso in cui il disponente è residnete all’estero va indicato il codice 999.|Regioni|Il campo del tracciato di input dpm/donatore/luogoNascitacodiceRegione deve esistere nel campo COD\_REG dell'anagrafica Regioni nel caso in cui il dpm/donatore/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri)<br>Il campo dpm/donatore/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/donatore/luogoResidenza/codiceProvincia|dpm/donatore/luogoResidenza|codiceProvincia|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/donatore/luogoResidenza/codiceProvincia|dpm/donatore/luogoResidenza|codiceProvincia|5050|Regione di Residenza incoerente con Comune o ASL  di Residenza|Il codice è valorizzaro e diverso da 999 e nel dominio e il codice  Comune di residenza è non nullo e valorizzato con 999999 e/o  la Asl di residenza è non nulla e valorizzata con 999 oppure sono non nulli e valorizzati con valori che non afferiscono alla regione di residenza.|Province|Il campo del tracciato di input dpm/donatore/luogoNascitacodiceProvincia deve esistere nel campo COD\_PROV dell'anagrafica Regioni nel caso in cui il dpm/donatore/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri).<br>Il campo dpm/donatore/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per la provincia in oggetto.<br>Il campo dpm/donatore/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| |Codice provincia/residenza|
|dpm/donatore/luogoResidenza/codiceComune|dpm/donatore/luogoResidenza|codiceComune|5010|Codice non presente nel dominio di riferimento|La lunghezza è diversa da 6 caratteri|Comuni|Il campo del tracciato di input dpm/donatore/luogoNascitacodiceComune deve esistere nel campo COD\_CMN dell'anagrafica Comuni nel caso in cui il dpm/donatore/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999999" (per gli esteri).<br>Il campo dpm/donatore/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per il comune in oggetto.<br>Il campo dpm/donatore/luogoNascitacodiceProvincia deve essere presente nel campo COD\_PRV dell'anagrafica delle Province per il comune in oggetto.<br>Il campo dpm/donatore/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/donatore/luogoResidenza/codiceComune|dpm/donatore/luogoResidenza|codiceComune|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/donatore/luogoResidenza/codiceNazione|dpm/donatore/luogoResidenza|codiceNazione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/donatore/luogoResidenza/codiceRegione|dpm/donatore/luogoResidenza|codiceRegione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/capResidenza|dpm/fiduciari\*\*\*|capResidenza|5006|Formato del CAP errato|CAP non valido| |CAP è obbligatorio (presente e diverso da blanks) e deve contenere 5 caratteri numerici| | |
|dpm/fiduciari[1]/consensoEmail|dpm/fiduciari\*\*\*|consensoEmail|5001|Codice non presente nel dominio di riferimento|Valori diversi da quelli ammessi:<br>SI: Se si intende ricevere la notifica via mail;<br>NO: Se non si intende ricevere la notifica via mail.| |I valori del campo del tracciato di input dpmdonatoreMinorenne ammessi sono: SI, NO| | |
|dpm/fiduciari[1]/capResidenza|dpm/fiduciari\*\*\*|capResidenza|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/codiceFiscale|dpm/fiduciari\*\*\*|codiceFiscale|5003|Lunghezza diversa da quella aspettata|Formato: codice fiscale a 16 caratteri | |Il codice è di lunghezza superiore o inferiore a 16 caratteri alfanumerici|Controllo che sia coerente con data di nascita e che l'ultimo carattere sia corretto. Ha classe| |
|dpm/fiduciari[1]/codiceFiscale|dpm/fiduciari\*\*\*|codiceFiscale|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)|Controllo che sia coerente con data di nascita e che l'ultimo carattere sia corretto. Ha classe| |
|dpm/fiduciari[1]/cognome|dpm/fiduciari\*\*\*|cognome|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/consensoEmail|dpm/fiduciari\*\*\*|consensoEmail|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/dataNascita|dpm/fiduciari\*\*\*|dataNascita|5000|Tipolo dato errato o formato data errato|Il campo deve essere valorizzato con il formato data AAAA-MM-GG| |Il formato della data deve corrispondere ad AAAA-MM-GG| | |
|dpm/fiduciari[1]/dataNascita|dpm/fiduciari\*\*\*|dataNascita|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/consensoEmail|dpm/fiduciari\*\*\*|email|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/indirizzoResidenza|dpm/fiduciari\*\*\*|indirizzoResidenza|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/nome|dpm/fiduciari\*\*\*|nome|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/luogoNascita/codiceNazione|dpm/fiduciari/luogoNascita|codiceNazione|5000|Codice non presente nel dominio di riferimento|Valori di versi da quelli ammessi.<br>La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|Codifica nazioni ISO-Alpha2|Il campo dpm/fiduciari/luogoNascitacodiceNazione del tracciato di input deve essere presente nel campo COD\_NAZ dell'anagrafica filtrata con dpm/donatoredatadiNascita tra le date at\_ini\_vld, dat\_end\_vld, INTEGRATA con i valori extra "XK", "XX", "ZZ".<br>Il campo dpm/fiduciari/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/fiduciari[1]/luogoNascita/codiceRegione|dpm/fiduciari/luogoNascita|codiceRegione|5002|Codice regione non presente nell'anagrafica di riferimento|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|Regioni|Il campo del tracciato di input dpm/fiduciari/luogoNascitacodiceRegione deve esistere nel campo COD\_REG dell'anagrafica Regioni nel caso in cui il dpm/fiduciari/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri)<br>Il campo dpm/fiduciari/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/fiduciari[1]/luogoNascita/codiceProvincia|dpm/fiduciari/luogoNascita|codiceProvincia|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/luogoNascita/codiceProvincia|dpm/fiduciari/luogoNascita|codiceProvincia|5050|Regione di Residenza incoerente con Comune o ASL  di Residenza|Il codice è valorizzaro e diverso da 999 e nel dominio e il codice  Comune di residenza è non nullo e valorizzato con 999999 e/o  la Asl di residenza è non nulla e valorizzata con 999 oppure sono non nulli e valorizzati con valori che non afferiscono alla regione di residenza.|Province|Il campo del tracciato di input dpm/fiduciari/luogoNascitacodiceProvincia deve esistere nel campo COD\_PROV dell'anagrafica Regioni nel caso in cui il dpm/fiduciari/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri).<br>Il campo dpm/fiduciari/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per la provincia in oggetto.<br>Il campo dpm/fiduciari/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| |Codice provincia/residenza|
|dpm/fiduciari[1]/luogoNascita/codiceComune|dpm/fiduciari/luogoNascita|codiceComune|5010|Codice non presente nel dominio di riferimento|La lunghezza è diversa da 6 caratteri|Comuni|Il campo del tracciato di input dpm/fiduciari/luogoNascitacodiceComune deve esistere nel campo COD\_CMN dell'anagrafica Comuni nel caso in cui il dpm/fiduciari/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999999" (per gli esteri).<br>Il campo dpm/fiduciari/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per il comune in oggetto.<br>Il campo dpm/fiduciari/luogoNascitacodiceProvincia deve essere presente nel campo COD\_PRV dell'anagrafica delle Province per il comune in oggetto.<br>Il campo dpm/fiduciari/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/fiduciari[1]/luogoNascita/codiceComune|dpm/fiduciari/luogoNascita|codiceComune|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/luogoNascita/codiceNazione|dpm/fiduciari/luogoNascita|codiceNazione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/luogoNascita/codiceRegione|dpm/fiduciari/luogoNascita|codiceRegione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/luogoResidenza/codiceNazione|dpm/fiduciari/luogoResidenza|codiceNazione|5000|Codice non presente nel dominio di riferimento|Valori di versi da quelli ammessi.<br>La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|Codifica nazioni ISO-Alpha2|Il campo dpm/fiduciari/luogoNascitacodiceNazione del tracciato di input deve essere presente nel campo COD\_NAZ dell'anagrafica filtrata con dpm/donatoredatadiNascita tra le date at\_ini\_vld, dat\_end\_vld, INTEGRATA con i valori extra "XK", "XX", "ZZ".<br>Il campo dpm/fiduciari/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/fiduciari[1]/luogoResidenza/codiceRegione|dpm/fiduciari/luogoResidenza|codiceRegione|5002|Codice regione non presente nell'anagrafica di riferimento|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|Regioni|Il campo del tracciato di input dpm/fiduciari/luogoNascitacodiceRegione deve esistere nel campo COD\_REG dell'anagrafica Regioni nel caso in cui il dpm/fiduciari/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri)<br>Il campo dpm/fiduciari/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/fiduciari[1]/luogoResidenza/codiceProvincia|dpm/fiduciari/luogoResidenza|codiceProvincia|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/luogoResidenza/codiceProvincia|dpm/fiduciari/luogoResidenza|codiceProvincia|5050|Regione di Residenza incoerente con Comune o ASL  di Residenza|Il codice è valorizzaro e diverso da 999 e nel dominio e il codice  Comune di residenza è non nullo e valorizzato con 999999 e/o  la Asl di residenza è non nulla e valorizzata con 999 oppure sono non nulli e valorizzati con valori che non afferiscono alla regione di residenza.|Province|Il campo del tracciato di input dpm/fiduciari/luogoNascitacodiceProvincia deve esistere nel campo COD\_PROV dell'anagrafica Regioni nel caso in cui il dpm/fiduciari/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri).<br>Il campo dpm/fiduciari/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per la provincia in oggetto.<br>Il campo dpm/fiduciari/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| |Codice provincia/residenza|
|dpm/fiduciari[1]/luogoResidenza/codiceComune|dpm/fiduciari/luogoResidenza|codiceComune|5010|Lunghezza diversa da quella attesa|La lunghezza è diversa da 6 caratteri|Comuni|Il campo del tracciato di input dpm/fiduciari/luogoNascitacodiceComune deve esistere nel campo COD\_CMN dell'anagrafica Comuni nel caso in cui il dpm/fiduciari/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999999" (per gli esteri).<br>Il campo dpm/fiduciari/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per il comune in oggetto.<br>Il campo dpm/fiduciari/luogoNascitacodiceProvincia deve essere presente nel campo COD\_PRV dell'anagrafica delle Province per il comune in oggetto.<br>Il campo dpm/fiduciari/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/fiduciari[1]/luogoResidenza/codiceComune|dpm/fiduciari/luogoResidenza|codiceComune|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/luogoResidenza/codiceNazione|dpm/fiduciari/luogoResidenza|codiceNazione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[1]/luogoResidenza/codiceRegione|dpm/fiduciari/luogoResidenza|codiceRegione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/soggettoAlimentante/cap|dpm/soggettoAlimentante|cap|5006|Formato del CAP errato|Formato errato| |CAP è obbligatorio (presente e diverso da blanks) e deve contenere 5 caratteri numerici| | |
|dpm/soggettoAlimentante/regioneSoggettoAlimentante|dpm/soggettoAlimentante|regioneSoggettoAlimentante|5002|Codice regione non presente nell'anagrafica di riferimento|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT.|Regioni|Il campo del tracciato di input dpm/soggettoAlimentantecodiceRegione deve esistere nel campo **COD\_REG** dell'anagrafica Regioni, integrata con il valore "999" (per gli esteri)| | |
|dpm/soggettoAlimentante/cap|dpm/soggettoAlimentante|cap|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/soggettoAlimentante/codiceSoggettoAlimentante|dpm/soggettoAlimentante|codiceSoggettoAlimentante|5700|Lunghezza diversa da quella aspettata|Il codice è di lunghezza superiore a 6 caratteri| |Il codice è di lunghezza superiore a 6 caratteri alfanumerici| | |
|dpm/soggettoAlimentante/codiceSoggettoAlimentante|dpm/soggettoAlimentante|codiceSoggettoAlimentante|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/soggettoAlimentante/indirizzo|dpm/soggettoAlimentante|indirizzo|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/soggettoAlimentante/regioneSoggettoAlimentante|dpm/soggettoAlimentante|regioneSoggettoAlimentante|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/capResidenza|dpm/fiduciari\*\*\*|capResidenza|5006|Formato del CAP errato|CAP non valido| |CAP è obbligatorio (presente e diverso da blanks) e deve contenere 5 caratteri numerici| | |
|dpm/fiduciari[2]/consensoEmail|dpm/fiduciari\*\*\*|consensoEmail|5001|Codice non presente nel dominio di riferimento|Valori diversi da quelli ammessi:<br>SI: Se si intende ricevere la notifica via mail;<br>NO: Se non si intende ricevere la notifica via mail.| |I valori del campo del tracciato di input dpmdonatoreMinorenne ammessi sono: SI, NO| | |
|dpm/fiduciari[2]/capResidenza|dpm/fiduciari\*\*\*|capResidenza|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/codiceFiscale|dpm/fiduciari\*\*\*|codiceFiscale|5003|Lunghezza diversa da quella aspettata|Formato: codice fiscale a 16 caratteri | |Il codice è di lunghezza superiore o inferiore a 16 caratteri alfanumerici|Controllo che sia coerente con data di nascita e che l'ultimo carattere sia corretto. Ha classe| |
|dpm/fiduciari[2]/codiceFiscale|dpm/fiduciari\*\*\*|codiceFiscale|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)|Controllo che sia coerente con data di nascita e che l'ultimo carattere sia corretto. Ha classe| |
|dpm/fiduciari[2]/cognome|dpm/fiduciari\*\*\*|cognome|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/consensoEmail|dpm/fiduciari\*\*\*|consensoEmail|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/dataNascita|dpm/fiduciari\*\*\*|dataNascita|5000|Tipolo dato errato o formato data errato|Il campo deve essere valorizzato con il formato data AAAA-MM-GG| |Il formato della data deve corrispondere ad AAAA-MM-GG| | |
|dpm/fiduciari[2]/dataNascita|dpm/fiduciari\*\*\*|dataNascita|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/consensoEmail|dpm/fiduciari\*\*\*|email|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/indirizzoResidenza|dpm/fiduciari\*\*\*|indirizzoResidenza|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/nome|dpm/fiduciari\*\*\*|nome|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/luogoNascita/codiceNazione|dpm/fiduciari/luogoNascita|codiceNazione|5000|Codice non presente nel dominio di riferimento|Valori di versi da quelli ammessi.<br>La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|Codifica nazioni ISO-Alpha2|Il campo dpm/fiduciari/luogoNascitacodiceNazione del tracciato di input deve essere presente nel campo COD\_NAZ dell'anagrafica filtrata con dpm/donatoredatadiNascita tra le date at\_ini\_vld, dat\_end\_vld, INTEGRATA con i valori extra "XK", "XX", "ZZ".<br>Il campo dpm/fiduciari/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/fiduciari[2]/luogoNascita/codiceRegione|dpm/fiduciari/luogoNascita|codiceRegione|5002|Codice regione non presente nell'anagrafica di riferimento|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|Regioni|Il campo del tracciato di input dpm/fiduciari/luogoNascitacodiceRegione deve esistere nel campo COD\_REG dell'anagrafica Regioni nel caso in cui il dpm/fiduciari/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri)<br>Il campo dpm/fiduciari/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/fiduciari[2]/luogoNascita/codiceProvincia|dpm/fiduciari/luogoNascita|codiceProvincia|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/luogoNascita/codiceProvincia|dpm/fiduciari/luogoNascita|codiceProvincia|5050|Regione di Residenza incoerente con Comune o ASL  di Residenza|Il codice è valorizzaro e diverso da 999 e nel dominio e il codice  Comune di residenza è non nullo e valorizzato con 999999 e/o  la Asl di residenza è non nulla e valorizzata con 999 oppure sono non nulli e valorizzati con valori che non afferiscono alla regione di residenza.|Province|Il campo del tracciato di input dpm/fiduciari/luogoNascitacodiceProvincia deve esistere nel campo COD\_PROV dell'anagrafica Regioni nel caso in cui il dpm/fiduciari/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri).<br>Il campo dpm/fiduciari/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per la provincia in oggetto.<br>Il campo dpm/fiduciari/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| |Codice provincia/residenza|
|dpm/fiduciari[2]/luogoNascita/codiceComune|dpm/fiduciari/luogoNascita|codiceComune|5010|Codice non presente nel dominio di riferimento|La lunghezza è diversa da 6 caratteri|Comuni|Il campo del tracciato di input dpm/fiduciari/luogoNascitacodiceComune deve esistere nel campo COD\_CMN dell'anagrafica Comuni nel caso in cui il dpm/fiduciari/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999999" (per gli esteri).<br>Il campo dpm/fiduciari/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per il comune in oggetto.<br>Il campo dpm/fiduciari/luogoNascitacodiceProvincia deve essere presente nel campo COD\_PRV dell'anagrafica delle Province per il comune in oggetto.<br>Il campo dpm/fiduciari/datadiNascita deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/fiduciari[2]/luogoNascita/codiceComune|dpm/fiduciari/luogoNascita|codiceComune|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/luogoNascita/codiceNazione|dpm/fiduciari/luogoNascita|codiceNazione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/luogoNascita/codiceRegione|dpm/fiduciari/luogoNascita|codiceRegione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/luogoResidenza/codiceNazione|dpm/fiduciari/luogoResidenza|codiceNazione|5000|Codice non presente nel dominio di riferimento|Valori di versi da quelli ammessi.<br>La codifica da utilizzare è quella Alpha2 (a due lettere) prevista dalla normativa ISO 3166-2.<br>Se compilato con uno stato diverso da IT (Italia), compilare gli altri attributi afferenti alla nascita del disponente nel modo seguente:<br>Comune di nascita = 999999<br>Provincia di nascita = 999<br>Ulteriori valori ammessi:<br>XK = Kosovo<br>XX = Stato nascita sconosciuto;<br>ZZ = Apolidi.|Codifica nazioni ISO-Alpha2|Il campo dpm/fiduciari/luogoNascitacodiceNazione del tracciato di input deve essere presente nel campo COD\_NAZ dell'anagrafica filtrata con dpm/donatoredatadiNascita tra le date at\_ini\_vld, dat\_end\_vld, INTEGRATA con i valori extra "XK", "XX", "ZZ".<br>Il campo dpm/fiduciari/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/fiduciari[2]/luogoResidenza/codiceRegione|dpm/fiduciari/luogoResidenza|codiceRegione|5002|Codice regione non presente nell'anagrafica di riferimento|Il codice da utilizzare è il codice di tre caratteri secondo codifica ISTAT. Nel caso in cui il disponente è nato all’estero va indicato il codice 999.|Regioni|Il campo del tracciato di input dpm/fiduciari/luogoNascitacodiceRegione deve esistere nel campo COD\_REG dell'anagrafica Regioni nel caso in cui il dpm/fiduciari/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri)<br>Il campo dpm/fiduciari/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/fiduciari[2]/luogoResidenza/codiceProvincia|dpm/fiduciari/luogoResidenza|codiceProvincia|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/luogoResidenza/codiceProvincia|dpm/fiduciari/luogoResidenza|codiceProvincia|5050|Regione di Residenza incoerente con Comune o ASL  di Residenza|Il codice è valorizzaro e diverso da 999 e nel dominio e il codice  Comune di residenza è non nullo e valorizzato con 999999 e/o  la Asl di residenza è non nulla e valorizzata con 999 oppure sono non nulli e valorizzati con valori che non afferiscono alla regione di residenza.|Province|Il campo del tracciato di input dpm/fiduciari/luogoNascitacodiceProvincia deve esistere nel campo COD\_PROV dell'anagrafica Regioni nel caso in cui il dpm/fiduciari/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999" (per gli esteri).<br>Il campo dpm/fiduciari/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per la provincia in oggetto.<br>Il campo dpm/fiduciari/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| |Codice provincia/residenza|
|dpm/fiduciari[2]/luogoResidenza/codiceComune|dpm/fiduciari/luogoResidenza|codiceComune|5010|Lunghezza diversa da quella attesa|La lunghezza è diversa da 6 caratteri|Comuni|Il campo del tracciato di input dpm/fiduciari/luogoNascitacodiceComune deve esistere nel campo COD\_CMN dell'anagrafica Comuni nel caso in cui il dpm/fiduciari/luogoNascitacodiceNazione valorizzato con "IT". Altrimenti il campo di input deve valere "999999" (per gli esteri).<br>Il campo dpm/fiduciari/luogoNascitacodiceRegione deve essere presente nel campo COD\_REG dell'anagrafica delle Regioni per il comune in oggetto.<br>Il campo dpm/fiduciari/luogoNascitacodiceProvincia deve essere presente nel campo COD\_PRV dell'anagrafica delle Province per il comune in oggetto.<br>Il campo dpm/fiduciari/datadiSottoscrizione deve essere compresa tra le date at\_ini\_vld, dat\_end\_vld| | |
|dpm/fiduciari[2]/luogoResidenza/codiceComune|dpm/fiduciari/luogoResidenza|codiceComune|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/luogoResidenza/codiceNazione|dpm/fiduciari/luogoResidenza|codiceNazione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/fiduciari[2]/luogoResidenza/codiceRegione|dpm/fiduciari/luogoResidenza|codiceRegione|5555|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Mancata valorizzazione di un campo obbligatorio (non presente o blanks)| | |
|dpm/tipoDisponente|dpm|tipoDisponente|5556|Mancata valorizzazione di un campo obbligatorio|Tag XML non presente o tag XML presente ma non valorizzato.| |Se il campo in inputo dpm/tipoDisponente è valorizzato con GENITORE allora possono essere presenti fino a un MASSIMO di 2 (due) /disponenti.<br>Altrimenti, se è valorizzato con TUTORE\_SOGGETTI\_AFFIDATARI, può esistere 1 (uno) solo /disponeni|Se il campo in inputo dpm/tipoDisponente è valorizzato con GENITORE allora possono essere presenti fino a un MASSIMO di 2 (due) /disponenti.<br>Altrimenti, se è valorizzato con TUTORE\_SOGGETTI\_AFFIDATARI, può esistere 1 (uno) solo /disponeni| |


Al verificarsi anche di un solo errore, tra quelli descritti, il record oggetto di controllo sarà inserito tra gli scarti con il codice di errore riportato nella precedente tabella.


L' SDK provvederà a validare il dataset secondo le regole descritte in questo documento, restituendo in modo sincrono all’applicazione sorgente un esito di validazione della scheda (ok/ko). Quindi:

  - in caso di ko, sarà presente il dettaglio degli errori riscontrati;
  - in caso di ok, sarà presente il path al file Xml generato, da firmare (in carico all'utilizzatore) per poi essere utilizzato con l'API2.

## ***3.3 API 2: invio file verso MdS***

**Nota**: Per i dettagli tecnici su chiamate e risposte (JSON) consulta il manuale d'integrazione.

Per API 2 è previsto in input il file xml generato dall'API 1 e firmato dall'ente (non è prevista naming standard per il file input a SDK), contenente i dati relativi alla singola richiesta (donazione o revoca consenso). Il tracciato del payload xml è identico a quello in input a API 1.

Acquisito il dato di input, il Validation Engine di SDK procederà:

- ad implementare sul payolad gli stessi controlli di API 1 (incluso check con xsd);
- a verificare che la firma sia formalmente corretta (NOTA: la validazione della firma non sarà gestita lato SDK).

Laddove i controlli applicativi vengono superati, il file di input sarà inviato vs MdS. La tabella di seguito riporta le API esposte da MdS (tramite PDI) per l’acquisizione del file.


|**Operation**|**Tipo Request**|**Tipo response**|
| - | - | - |
|**dpmInvioXml**|dpmInvioXmlRequest|dpmInvioXmlResponse|

A chiusura del processo, SDK risponderà all’applicazione chiamante restituendo in modo sincrono un esito di elaborazione/tramissione vs MdS (ok o ko). In caso di ko, il sistema provvederà a restituire anche i dettagli degli errori riscontrati (lista dei codici di errore e relative descrizioni).

## ***3.4 API 3: verifica stato di elaborazione MdS***

**Nota**: Per i dettagli tecnici su chiamate e risposte (JSON) consulta il manuale d'integrazione.

L’applicazione sorgente provvederà a invocare la API 3 per verificare l’effettiva acquisizione della richiesta DPM lato MdS. SDK provvederà a inoltrare la richiesta verso MdS comunicandone l’esito all’applicazione sorgente.

La tabella di seguito riporta le API esposte da MdS (tramite PDI) per il monitoraggio della sottomissione del flusso:

|**Operation**|**Tipo Request**|**Tipo response**|
| - | - | - |
|**dpmVerificaStatoXml**|dpmVerificaStatoXmlRequest|dpmVerificaStatoXmlResponse|


## ***3.5 Informazioni dell’esecuzione***

In una cartella dedicata (es. /sdk/run) verrà creato un file json contenente il dettaglio degli scarti riscontrati ad ogni esecuzione del processo SDK.

Il naming del file sarà:  

{ID\_RUN}.json

Dove:

- ID\_RUN rappresenta l’identificativo univoco dell’elaborazione

Di seguito il tracciato del record da produrre.


|**CAMPO**|**DESCRIZIONE**|
| :- | :- |
|ID RUN (chiave)|Identificativo univoco di ogni esecuzione del SDK|
|ID\_CLIENT|Identificativo Univoco della trasazione sorgente che richiede processamento lato SDK|
|ID UPLOAD (chiave)|Identificativo di caricamento fornito da MdS|
|TIPO ELABORAZIONE|F (full)/R (per singolo record) - Impostato di default a F|
|MODALITA’ OPERATIVA|P (=produzione) /T (=test)|
|DATA INIZIO ESECUZIONE|Timestamp dell’ inizio del processamento|
|DATA FINE ESECUZIONE|Timestamp di completamento del processamento|
|STATO ESECUZIONE |<p>Esito dell’esecuzione dell’ SDK. </p><p>Possibili valori: </p><p>- IN ELABORAZIONE: Sdk in esecuzione;</p><p>- ELABORATA: Esecuzione completata con successo;</p><p>- KO: Esecuzione fallita: </p><p>- KO SPECIFICO: Esecuzione fallita per una fase/componente più rilevante della soluzione (es. ko\_gestione\_file, ko\_gestione\_validazione, ko\_invio\_ministero, etc.); </p><p>- KO GENERICO: un errore generico non controllato.</p>|
|FILE ASSOCIATI RUN|nome del file di input elaborato dall’SDK|
|NOME FLUSSO|valore fisso che identifica lo specifico SDK in termini di categoria e nome flusso|
|NUMERO RECORD |Numero di record del flusso input|
|NUMERO RECORD ACCETTATI|Numero validi|
|NUMERO RECORD SCARTATI|Numero scarti|
|VERSION|Versione del SDK (Access Layer e Validation Engine)|
|TIMESTAMP CREAZIONE|Timestamp creazione della info run|
|API (\*DPM)|Rappresenta L’API utilizzata per il flusso DPM (non valorizzata per gli altri flussi)|
|IDENTIFICATIVO SOGGETTO ALIMENTANTE (\*DPM)|Chiave flusso DPM (non valorizzata per gli altri flussi)|
|TIPO ATTO (\*DPM)|Chiave flusso DPM (non valorizzata per gli altri flussi)|
|NUMERO ATTO (\*DPM)|Chiave flusso DPM (non valorizzata per gli altri flussi)|
|TIPO ESITO MDS (\*DPM)|Esito della response dell’API 2 (non valorizzata per gli altri flussi) |
|DATA RICEVUTA MDS (\*DPM)|Data della response dell’API 3 (non valorizzata per gli altri flussi)|
|CODICE REGIONE|Codice Regione del Mittente|
|ANNO RIFERIMENTO|Anno cui si riferiscono i dati del flusso|
|PERIODO DI RIFERIMENTO|Rappresenta il periodo di riferimento passato in input all’SDK|
|DESCRIZIONE STATO ESECUZIONE |Specifica il messaggio breve dell’errore, maggiori informazioni saranno presenti all’interno del log applicativo|
|NOME FILE OUTPUT MDS|Nome dei file di output inviati verso MdS|
|ESITO ACQUISIZIONE FLUSSO|Codice dell’esito del processo di acquisizione del flusso su MdS. Tale campo riflette la proprietà invioFlussiReturn/listaEsitiUpload/item/esito della response della procedura **invioFlussi**. (Es IF00)|
|CODICE ERRORE INVIO FLUSSI|Codice d’errore della procedura di invio. Tale campo riflette la proprietà InvioFlussiReturn/errorCode della response della procedura **invioFlussi**|
|TESTO ERRORE INVIO FLUSSI|Descrizione codice d’errore della procedura.Tale campo riflette la proprietà InvioFlussiReturn/ errorText della response della procedura **invioFlussi**|


Inoltre, a supporto dell’entità che rappresenta lo stato dell’esecuzione, sotto la cartella “/sdk/log”, saranno presenti anche i file di log applicativi (aggregati giornalmente) non strutturati, nei quali saranno presenti informazioni aggiuntive, ad esempio lo StackTrace (in caso di errori).

Il naming del file, se non modificata la politica di rolling (impostazioni) sarà:

**SDK \_DPM.log**

# **4. Installazione e avvio**

Gli step di installazione dell'SDK possono essere sintetizzati nei seguenti passaggi:
- clonare da git sdk-engine-donazionipostmortem-java
- clonare da git tutti gli artifatti di dipendenze
- buildare e installare le dipendenzer

## ***4.1 Scaricare repository e dipendenze***

Clonare dal git sdk-engine-donazionipostmortem-java e tutti gli artifatti di dipendenze:

- sdk-engine-donazionipostmortem-java
  - git clone https://github.com/ministero-salute/sdk-engine-donazionipostmortem-java.git

- sdk-al-donazionipostmortem-java
  - git clone https://github.com/ministero-salute/sdk-al-donazionipostmortem-java.git

- sdk-lib-al-java
  - git clone https://github.com/ministero-salute/sdk-lib-al-java.git

- sdk-lib-apigateway-java
  - git clone https://github.com/ministero-salute/sdk-lib-apigateway-java.git

- sdk-lib-connettoremds-java
  - https://github.com/ministero-salute/sdk-lib-connettoremds-java.git

- sdk-lib-crypto 
  - git clone https://github.com/ministero-salute/sdk-lib-crypto.git
  
- sdk-lib-downloader-anagrafiche-client
  - git clone https://github.com/ministero-salute/sdk-lib-downloader-anagrafiche-client.git

- sdk-lib-gestoreanagrafiche-java
  - git clone https://github.com/ministero-salute/sdk-lib-gestoreanagrafiche-java.git
  
- sdk-lib-gestoreesiti-java
  - git clone https://github.com/ministero-salute/sdk-lib-gestoreesiti-java.git

- sdk-lib-gestorefile-java
  - git clone https://github.com/ministero-salute/sdk-lib-gestorefile-java.git

- sdk-lib-interconnessione
  - git clone https://github.com/ministero-salute/sdk-lib-interconnessione.git
  
- sdk-lib-regole-java
  - git clone https://github.com/ministero-salute/sdk-lib-regole-java.git
  
## ***4.2 Buildare e installare le dipendenze***

Per poter installare correttamente le dipendenze occorre accedere a ogni cartella precedetemente scaricata ed eseguire una Maven build inserendo clean install nei Goals e utilizzando la jdk 11.

(NOTA: sdk-lib-downloader-anagrafiche-client necessita della jdk 8)

Per poter eseguire la build: 
build system maven,
comando mvn clean package per il microservizio,
comando mvn clean install per le dipendenze

Esempio:
Accedere alla cartella -> cd sdk-engine-donazionipostmortem-java
Build -> mvn clean package

Esempio dipendenze
Accedere alla cartella -> cd sdk-lib-gestoreesiti-java
Build -> mvn clean install

Per eseguire l'installazione è necessario prendere il jar generato dal build system e copiarlo in una cartella, successivamente all'interno della root folder ( / su Linux mentre C:\ su Windows) creare la cartella sdk e tutte le sottocartelle e i file necessari:
- db
- dir
- esiti 
- log
- progressivo
- properties
- regole
- run
- templates_bkp
- xmloutputn

## ***4.3 Avvio***

Per eseguire l'avvio del microservizio:

java -jar <jar prodotto dalla clean package>


## mantainerr:
 Accenture SpA until January 2026