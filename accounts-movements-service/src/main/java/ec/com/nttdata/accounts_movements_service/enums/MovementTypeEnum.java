package ec.com.nttdata.accounts_movements_service.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum MovementTypeEnum {

    DEPOSIT("DEPOSITO"),
    WITHDRAWAL("RETIRO");

    private final String displayName;

    MovementTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Busca un MovementTypeEnum a partir de su nombre de visualización.
     *
     * @param displayName El nombre de visualización del tipo de movimiento.
     * @return El MovementTypeEnum correspondiente.
     * @throws IllegalArgumentException si el displayName no coincide con ningún enum.
     */
    public static MovementTypeEnum fromDisplayName(String displayName) {
        return Arrays.stream(MovementTypeEnum.values())
                .filter(type -> type.getDisplayName().equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró una constante de enum con el nombre: " + displayName));
    }
}
