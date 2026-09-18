package rs.diplomski.change_management;

/**
 * Preslikava kodove prioriteta, koji se cuvaju u procesu i bazi, u oznake
 * na srpskom jeziku koje se prikazuju u formama.
 */
final class ChangePriorityLabels {

    private ChangePriorityLabels() {
    }

    static String of(String priority) {
        return switch (priority) {
            case "high" -> "visok";
            case "medium" -> "srednji";
            case "low" -> "nizak";
            default -> priority;
        };
    }
}
