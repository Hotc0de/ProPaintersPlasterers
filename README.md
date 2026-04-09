# ProPaintersPlasterersPayment

Android app (Kotlin + Jetpack Compose + MVVM + Room) for private painting and plastering business workflows.

## Phase 1 (current)

- Clean layered package structure started
- Room entities and relationships added
- DAOs and repository layer scaffolding added
- Application container added for database and repositories

## PDF export (Timesheet + Invoice)

- Native Android PDF generation using `android.graphics.pdf.PdfDocument`
- Shared models under `core/pdf/PdfExportModels.kt`
- PDF rendering logic in `core/pdf/PdfExportService.kt`
- Save/share/open helper in `core/pdf/PdfFileHelper.kt`
- `FileProvider` configured in `AndroidManifest.xml` with `res/xml/file_paths.xml`
- Export actions wired into:
  - `feature/timesheet/ui/TimesheetScreen.kt`
  - `feature/invoice/ui/InvoiceScreen.kt`

Generated files are saved inside app-specific storage under:

```text
Android/data/<your.package.name>/files/Documents/exports/
```

## Proposed package structure

```text
app/src/main/java/com/example/propaintersplastererspayment/
  app/
    navigation/
  core/
    util/
    ui/
  data/
    local/
      dao/
      entity/
      model/
      AppDatabase.kt
    repository/
    AppContainer.kt
    AppDataContainer.kt
  domain/
    model/
    repository/
    usecase/
  feature/
    home/
      ui/
      vm/
    job/
      ui/
      vm/
    timesheet/
      ui/
      vm/
    materials/
      ui/
      vm/
    invoice/
      ui/
      vm/
    settings/
      ui/
      vm/
```

## Build and test

```bash
./gradlew test
./gradlew testDebugUnitTest
```

