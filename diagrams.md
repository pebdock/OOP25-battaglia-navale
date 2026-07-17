# Guida agli schemi UML del progetto

Questo file stabilisce come realizzare gli schemi UML del progetto in modo coerente con le slide del corso e con il modello della relazione. Gli esempi usano PlantUML.

## 1. Regola fondamentale

Ogni simbolo deve descrivere una relazione reale del progetto. La notazione non va scelta per ottenere un effetto grafico.

- Il diagramma deve avere uno scopo preciso.
- Devono comparire soltanto gli elementi necessari a quello scopo.
- Ogni elemento inserito deve essere collegato al resto dello schema.
- Il diagramma deve essere spiegato nel testo della relazione.
- I dettagli devono aumentare gradualmente passando dall'analisi al design.
- Un pattern va indicato soltanto se Ã¨ realmente implementato e motivato.

La relazione tipo richiede pochi UML mirati e leggibili, non un unico schema contenente tutto il progetto.

## 2. Tipi di diagramma nella relazione

### 2.1 Modello del dominio - sezione 1.2

Scopo: descrivere il problema, le entitÃ  principali e i loro rapporti, senza mostrare come il software Ã¨ implementato.

Inserire:

- concetti del dominio;
- operazioni pubbliche essenziali;
- associazioni, aggregazioni e composizioni significative;
- eventuali generalizzazioni proprie del dominio.

Non inserire:

- classi `Impl`;
- Controller, View o librerie grafiche;
- campi e metodi privati;
- strutture dati interne;
- eccezioni Java;
- pattern non ancora implementati;
- dettagli di Gradle, JUnit o Java.

Nel progetto Battleship questo Ã¨ il diagramma generale con concetti come `Game`, `Player`, `Board`, `Ship`, `Shot`, `Sonar`, `Coordinate` e i risultati delle azioni.

### 2.2 Schema architetturale - sezione 2.1

Scopo: mostrare le parti principali del software e il modo in cui comunicano.

Quando l'architettura sarÃ  implementata, mostrare soltanto:

- punti d'ingresso principali di Model, View e Controller;
- interfacce che separano i componenti;
- dipendenze fra i componenti;
- direzione delle comunicazioni rilevanti.

Evitare classi secondarie, campi interni e metodi che non servono a comprendere l'architettura.

### 2.3 Design dettagliato - sezione 2.2

Scopo: spiegare la soluzione adottata per un problema progettuale specifico.

Ogni sottosezione deve contenere:

1. il problema;
2. la soluzione e le alternative considerate;
3. vantaggi e costi della scelta;
4. un piccolo UML focalizzato;
5. il pattern utilizzato, soltanto se esiste realmente nel codice.

Qui possono comparire classi concrete, record, enum, eccezioni e implementazioni di interfacce, ma soltanto se pertinenti alla soluzione descritta.

## 3. Elementi dei riquadri

Le slide sui class diagram prevedono un riquadro diviso, quando necessario, in nome, campi e operazioni.

### VisibilitÃ 

| Simbolo | Significato |
| --- | --- |
| `+` | pubblico |
| `-` | privato |
| `#` | protetto |
| `~` | visibilitÃ  di package |

Firma di un metodo:

```text
+nome(argomento: Tipo): TipoRestituito
```

Esempio:

```plantuml
class Coordinate <<record>> {
    +Coordinate(row: int, column: int)
    +isInside(boardSize: int): boolean
}
```

Nel modello del dominio mostrare normalmente soltanto la parte pubblica essenziale. I campi privati sono ammessi solo in un UML di design quando sono indispensabili a capire la soluzione.

### Tipi principali

```plantuml
interface Board {
    +shoot(target: Coordinate): ShotResult
}

class BoardImpl

abstract class AbstractShip

enum ShotOutcome {
    MISS
    HIT
    SUNK
}

class ShotResult <<record>> {
    +ShotResult(target: Coordinate, outcome: ShotOutcome)
    +isHit(): boolean
}
```

## 4. Relazioni UML

### 4.1 Associazione - "usa" o "conosce"

Una classe Ã¨ collegata a un'altra, senza una relazione forte parte-tutto.

```plantuml
ShotResult -- Coordinate
GameRuleException -- RuleViolation
```

Usare una freccia soltanto quando Ã¨ utile indicare la navigabilitÃ  o la direzione della conoscenza:

```plantuml
Controller --> Game
```

Non usare una freccia direzionale soltanto perchÃ© sembra graficamente piÃ¹ chiara.

### 4.2 Aggregazione - relazione parte-tutto debole

Il rombo vuoto si colloca dalla parte del tutto. La parte puÃ² avere una vita propria indipendente dal contenitore.

```plantuml
Game o-- Player
```

Si legge: `Game` aggrega `Player`.

### 4.3 Composizione - relazione parte-tutto forte

Il rombo nero si colloca dalla parte del tutto. La parte appartiene al tutto ed Ã¨ concettualmente legata al suo ciclo di vita.

```plantuml
Player *-- Board
Board *-- Ship
TurnResult *-- ShotResult
```

Si legge, per esempio: `TurnResult` Ã¨ composto da `ShotResult`.

Non usare la composizione automaticamente per ogni campo Java. Deve esistere una vera relazione "Ã¨ composto da" nel modello considerato.

### 4.4 EreditarietÃ  o generalizzazione - relazione "Ã¨ un tipo di"

La linea Ã¨ continua e il triangolo vuoto punta verso la classe piÃ¹ generale.

```plantuml
RuntimeException <|-- GameRuleException
```

Si legge: `GameRuleException` Ã¨ una sottoclasse di `RuntimeException`.

Il triangolo di generalizzazione deve rimanere vuoto. In alcune slide la punta appare nera per lo stile grafico usato, ma PlantUML applica la notazione UML standard con il triangolo vuoto.

### 4.5 Realizzazione di un'interfaccia - `implements`

La linea Ã¨ tratteggiata e il triangolo vuoto punta verso l'interfaccia.

```plantuml
Board <|.. BoardImpl
```

Si legge: `BoardImpl` implementa `Board`.

Non confondere:

```plantuml
' extends: linea continua
RuntimeException <|-- GameRuleException

' implements: linea tratteggiata
Board <|.. BoardImpl
```

### 4.6 Dipendenza temporanea

Una classe usa un'altra, per esempio come parametro o servizio, ma non la conserva necessariamente come parte del proprio stato.

```plantuml
GameController ..> Game
```

La dipendenza usa una linea tratteggiata con freccia aperta.

### 4.7 MolteplicitÃ 

Le molteplicitÃ  si aggiungono soltanto quando sono certe e aiutano a comprendere il modello.

```plantuml
Game "1" o-- "2" Player
Board "1" *-- "8" Ship
TurnResult "1" *-- "1..2" ShotResult
```

Valori comuni:

| Notazione | Significato |
| --- | --- |
| `1` | esattamente uno |
| `0..1` | zero oppure uno |
| `*` o `0..*` | zero o piÃ¹ |
| `1..*` | uno o piÃ¹ |
| `2` | esattamente due |

Se la molteplicitÃ  rende il diagramma piÃ¹ pesante senza aggiungere informazioni utili, puÃ² essere omessa.

## 5. Come scegliere la relazione

Usare queste domande nell'ordine:

1. `B` Ã¨ un tipo particolare di `A`? Usare generalizzazione.
2. Una classe concreta implementa un'interfaccia? Usare realizzazione.
3. `B` Ã¨ una parte essenziale e posseduta da `A`? Usare composizione.
4. `A` raggruppa `B`, ma `B` puÃ² vivere autonomamente? Usare aggregazione.
5. `A` conosce o usa stabilmente `B`? Usare associazione.
6. `A` usa `B` solo temporaneamente? Usare dipendenza.

Esempi del progetto:

| Relazione | Scelta | Motivo |
| --- | --- | --- |
| `GameRuleException` - `RuntimeException` | ereditarietÃ  | la prima Ã¨ un tipo della seconda |
| `BoardImpl` - `Board` | realizzazione | la classe implementa l'interfaccia |
| `TurnResult` - `ShotResult` | composizione | il risultato del turno Ã¨ formato dagli esiti dei bersagli |
| `GameRuleException` - `RuleViolation` | associazione | l'eccezione conserva la causa semantica, ma l'enum non dipende dal suo ciclo di vita |
| `ShotResult` - `Coordinate` | associazione | la coordinata Ã¨ un valore autonomo riutilizzabile |

## 6. UML corretti fino al punto 1.6 della guida

Fino a questa fase non Ã¨ ancora stato applicato alcun pattern GoF. I due schemi di design devono quindi descrivere value object, risultati ed eccezioni senza dichiarare Strategy, Template Method, Decorator o Observer.

### 6.1 Risultato di un singolo bersaglio

```plantuml
@startuml ShotResultDesign

skinparam monochrome true
skinparam shadowing true
skinparam classAttributeIconSize 0
hide circle
hide empty fields

class Coordinate <<record>> {
    +Coordinate(row: int, column: int)
    +isInside(boardSize: int): boolean
}

enum ShotOutcome {
    MISS
    HIT
    SUNK
}

class ShotResult <<record>> {
    +ShotResult(target: Coordinate, outcome: ShotOutcome)
    +isHit(): boolean
}

ShotResult -- Coordinate
ShotResult -- ShotOutcome

@enduml
```

Le relazioni sono associazioni: `Coordinate` e `ShotOutcome` sono valori autonomi, non parti il cui ciclo di vita dipende da `ShotResult`.

### 6.2 Violazioni delle regole

```plantuml
@startuml DomainViolationDesign

skinparam monochrome true
skinparam shadowing true
skinparam classAttributeIconSize 0
hide circle
hide empty fields

class RuntimeException

class GameRuleException {
    +GameRuleException(violation: RuleViolation, message: String)
    +violation(): RuleViolation
}

enum RuleViolation

RuntimeException <|-- GameRuleException
GameRuleException -- RuleViolation

@enduml
```

Il triangolo vuoto rappresenta l'ereditarietÃ  da `RuntimeException`; la linea semplice verso `RuleViolation` rappresenta l'associazione con la causa della violazione.

## 7. Pattern da aggiungere soltanto nelle fasi successive

La guida del progetto introduce in seguito:

- Strategy per `ShotStrategy`, `NormalShotStrategy` e `DoubleShotStrategy`;
- un piccolo Template Method nel comportamento comune di `ShotStrategy`;
- Decorator per la policy che rende il sottomarino non rilevabile dal sonar;
- Observer per la comunicazione delle intenzioni dalla View al Controller;
- MVC come pattern architetturale, che non fa parte dei 23 pattern GoF.

Questi elementi vanno inseriti nella relazione e negli UML solo dopo essere stati realmente implementati. Per ogni pattern occorre indicare il problema, i ruoli delle classi del progetto, la soluzione e le conseguenze positive e negative.

## 8. Regole di leggibilitÃ  in PlantUML

Configurazione di base consigliata:

```plantuml
@startuml NomeDiagramma

!pragma layout smetana

skinparam monochrome true
skinparam shadowing true
skinparam classAttributeIconSize 0
hide circle
hide empty fields

' elementi e relazioni

@enduml
```

Per guidare la disposizione senza cambiare il significato UML:

```plantuml
Shot -left- Game
Game -right- Sonar
Shot -[hidden]right- Game
```

Le indicazioni `left`, `right`, `up`, `down` e le relazioni `hidden` servono soltanto al layout. Non trasformano un'associazione in ereditarietÃ , aggregazione o composizione.

Evitare `skinparam linetype ortho` e `skinparam linetype polyline` quando si desiderano collegamenti continui e il piÃ¹ possibile rettilinei.

## 9. Errori da evitare

- Usare un rombo o un triangolo soltanto per imitare l'aspetto di un altro diagramma.
- Disegnare una freccia di ereditarietÃ  al contrario: il triangolo punta sempre al tipo piÃ¹ generale.
- Usare una linea continua per `implements`: deve essere tratteggiata.
- Confondere composizione con ereditarietÃ .
- Inserire una classe senza collegamenti con il resto dello schema.
- Mostrare tutte le classi, tutti i campi e tutti i metodi in ogni UML.
- Inserire elementi implementativi nel modello del dominio.
- Dichiarare un pattern che non esiste nel codice.
- Usare Singleton soltanto perchÃ© nel gioco esiste una singola partita corrente.
- Aggiungere relazioni invisibili e attribuire loro un significato UML.
- Usare nomi o firme diversi da quelli realmente presenti nel progetto senza spiegare il diverso livello di astrazione.

## 10. Checklist prima di inserire un UML nella relazione

- [ ] Lo scopo del diagramma Ã¨ dichiarato nel testo.
- [ ] Il livello Ã¨ corretto: dominio, architettura oppure design dettagliato.
- [ ] Ogni riquadro Ã¨ pertinente allo scopo.
- [ ] Non ci sono elementi isolati.
- [ ] Il triangolo punta alla superclasse o all'interfaccia.
- [ ] `extends` usa una linea continua.
- [ ] `implements` usa una linea tratteggiata.
- [ ] Il rombo Ã¨ collocato dalla parte del tutto.
- [ ] Composizione e aggregazione sono motivate dal dominio.
- [ ] Le molteplicitÃ  inserite sono corrette.
- [ ] Le firme mostrate corrispondono al livello del diagramma.
- [ ] I dettagli privati non necessari sono stati omessi.
- [ ] Gli eventuali pattern citati esistono realmente nel codice.
- [ ] Il diagramma Ã¨ leggibile in bianco e nero.
- [ ] La figura ha numero, didascalia ed Ã¨ richiamata nel testo.

## 11. Riferimenti teorici usati

- Slide 1: composizione ed ereditarietÃ  come forme di riuso.
- Slide 6, in particolare le parti su composizione, class diagram e interfacce: associazione, composizione, generalizzazione, molteplicitÃ  e realizzazione di `implements`.
- Slide 8: polimorfismo e uso corretto delle gerarchie.
- Slide 12: gerarchia delle eccezioni Java e sottoclassi di `RuntimeException`.
- Slide 15: separazione MVC e comunicazione fra View e Controller.
- Slide 20: descrizione e applicazione dei design pattern.
- Relazione tipo: distinzione fra modello del dominio, schema architetturale e piccoli UML di design dettagliato.
- [Documentazione PlantUML sui class diagram](https://plantuml.com/class-diagram): sintassi delle relazioni, molteplicitÃ , direzioni e collegamenti nascosti.
- [Documentazione PlantUML sui motori di layout](https://plantuml.com/layout-engines): uso di Smetana mediante `!pragma layout smetana`.

Questi riferimenti definiscono il significato delle relazioni. Lo stile grafico puÃ² variare fra le slide, ma non deve cambiare la semantica UML.
