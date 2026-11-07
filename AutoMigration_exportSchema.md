# Room Database Migrations & Schema Export

This document explains why schema exporting is critical and provides a complete guide on implementing both **Automated** and **Manual** migrations in Android.

---

## 1. Why `exportSchema = true` is Important

Setting `exportSchema = true` in your `@Database` annotation is a professional standard for several reasons:

### Mandatory for Automated Migrations (`@AutoMigration`)
Room needs to compare the schema of the "old" version with the "new" version to automatically generate migration code. It does this by reading the JSON files generated during the build process. If `exportSchema` is `false`, automated migrations will fail to compile.

### Essential for Testing Migrations
To use `MigrationTestHelper` (the best practice for testing), you must have these JSON files. The helper uses them to "time travel"—recreating the database exactly as it was in version 1, version 2, etc.—to verify that your migration scripts work correctly without crashing.

### Version Control & History
It generates a `schemas/` folder in your project. Checking these JSON files into Git provides a permanent historical record of your database's evolution.

---

## 2. Implementing Automated Migration

Automated migrations handle basic changes (like adding columns or new tables) with almost zero manual SQL.

### Steps to Implement:
1.  **Update the Entity**: Add the new field to your `NoteEntity.kt`.
2.  **Increment Version**: Increase the `version` number in your `@Database` annotation.
3.  **Declare Migration**: Add the `AutoMigration` property.

```kotlin
@Database(
    entities = [NoteEntity::class],
    version = 2, // Increment from 1 to 2
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)
abstract class NoteDatabase : RoomDatabase() { ... }
```

### Handling Ambiguous Changes (`AutoMigrationSpec`):
For actions like **renaming columns** or **deleting tables**, Room needs a "hint" to know your intent and preserve data. Without a spec, Room doesn't know if you want to rename a column or delete it and create a new one.

#### Step 1: Define the Spec Class
Create a class (usually inside your database class) that implements `AutoMigrationSpec` and annotate it with the specific change.
```kotlin
@RenameColumn(
    tableName = "notes", 
    fromColumnName = "oldTitle", 
    toColumnName = "newTitle"
)
class MyAutoMigration : AutoMigrationSpec
```

#### Step 2: Link it in `@AutoMigration`
Pass the class to the `spec` parameter in your `@Database` header.
```kotlin
@Database(
    version = 2,
    autoMigrations = [
        AutoMigration(
            from = 1, 
            to = 2, 
            spec = NoteDatabase.MyAutoMigration::class
        )
    ],
    exportSchema = true
)
abstract class NoteDatabase : RoomDatabase() { ... }
```

#### Full Implementation Example:
If you are renaming `oldTitle` to `newTitle` in the `notes` table, your `NoteDatabase.kt` should look like this:

```kotlin
@Database(
    entities = [NoteEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(
            from = 1, 
            to = 2, 
            spec = NoteDatabase.MyAutoMigration::class 
        )
    ],
    exportSchema = true
)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDAO

    @RenameColumn(
        tableName = "notes", 
        fromColumnName = "oldTitle", 
        toColumnName = "newTitle"
    )
    class MyAutoMigration : AutoMigrationSpec
}
```

#### Available Spec Annotations:
*   `@RenameColumn(tableName, fromColumnName, toColumnName)`
*   `@RenameTable(fromTableName, toTableName)`
*   `@DeleteColumn(tableName, columnName)`
*   `@DeleteTable(tableName)`

---

## 3. Implementing Manual Migration

Manual migrations are used for complex refactoring (e.g., splitting one table into two) or when you need absolute control over the SQL execution.

### Steps to Implement:
1.  **Define the Migration Object**: Create an object that implements the `Migration` class and overrides `migrate`.
2.  **Register in the Builder**: Tell `Room.databaseBuilder` about your migration.
3.  **Increment Version**: Update the version number in the `@Database` annotation.

```kotlin
// 1. Define the SQL logic
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Raw SQL to modify the table
        db.execSQL("ALTER TABLE notes ADD COLUMN category TEXT NOT NULL DEFAULT ''")
    }
}

// 2. Add it to your Database Builder (e.g., in AppModule.kt or Companion Object)
Room.databaseBuilder(context, NoteDatabase::class.java, "note_database")
    .addMigrations(MIGRATION_1_2) // Register it here!
    .build()
```

---

## Summary Comparison

| Feature | Automated Migration | Manual Migration |
| :--- | :--- | :--- |
| **Effort** | Low (Room writes the SQL) | High (You write the SQL) |
| **Control** | Limited | Absolute |
| **Testing** | Requires `exportSchema = true` | Requires `exportSchema = true` |
| **Best For** | Adding/Deleting columns/tables | Complex data restructuring |

**Best Practice**: Always enable `exportSchema = true` from Day 1. Use **Automated Migrations** for 90% of your changes, and only switch to **Manual Migrations** for complex structural refactoring.

---

## 4. Complex Scenarios: Errors vs. Exceptions vs. Wrong Results

When applying migrations (especially automated ones) in complex scenarios, Room behaves differently depending on the nature of the change:

### 1. Compile-Time Errors (Ambiguous Changes)
If you perform a change that is technically ambiguous, the app **will not compile**. Room detects that something has changed but doesn't know your intent.
*   **Scenarios**: Renaming a column/table or deleting a column.
*   **Result**: Room throws a compile-time error and demands an `AutoMigrationSpec` (e.g., `@RenameColumn`) to protect against accidental data loss.

### 2. Runtime Exceptions (`IllegalStateException`)
If the database version is incremented on the device but no valid migration path (Auto or Manual) is provided in the code, the app will crash at startup.
*   **Error**: `IllegalStateException: A migration from X to Y was required but not found.`
*   **Result**: The user is completely blocked from using the app until a fix is deployed.

### 3. Wrong Results (Logic Errors)
This is the most dangerous case where the migration succeeds technically but the **data is corrupted or lost**.
*   **Scenario**: A manual SQL script has a typo, or an automated migration correctly updates the schema but cannot "guess" how to move data between complex table splits.
*   **Result**: The app runs without crashing, but user data is missing or incorrect.

### Safety Summary Table

| Problem Type | Result | Prevention / Fix |
| :--- | :--- | :--- |
| **Ambiguity** | **Compile Error** | Provide an `AutoMigrationSpec`. |
| **Missing Path** | **Runtime Crash** | Ensure version matches a migration path. |
| **Complex Logic** | **Wrong Data** | **Test** with `MigrationTestHelper` to verify data. |
