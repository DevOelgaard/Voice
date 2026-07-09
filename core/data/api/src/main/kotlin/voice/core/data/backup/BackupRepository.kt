package voice.core.data.backup

public interface BackupRepository {

  /**
   * Exports the current progress and settings into a JSON string.
   */
  public suspend fun exportData(): String

  /**
   * Imports progress and settings from a JSON string.
   */
  public suspend fun importData(jsonString: String)
}
