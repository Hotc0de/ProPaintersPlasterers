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

## Data safety on updates

- `fallbackToDestructiveMigration()` is disabled to prevent silent data wipes.
- All Room migrations are registered through `AppDatabase.ALL_MIGRATIONS`.
- Room schema export is enabled (`app/schemas`) so schema changes are tracked.
- Android backup/restore is enabled for app database and shared preferences.

### Release checklist for DB changes

1. Increase `version` in `AppDatabase`.
2. Add a forward migration object (for example `MIGRATION_23_24`).
3. Add the migration to `AppDatabase.ALL_MIGRATIONS`.
4. Build and run migration validation:

```bash
./gradlew :app:kspDebugKotlin
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

5. Run instrumentation migration tests on a device/emulator:

```bash
./gradlew :app:connectedDebugAndroidTest
```

