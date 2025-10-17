package cr.una.reservas_municipales.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SpaceRateDto {
    private Long rateId;
    private java.util.UUID spaceId;
    private String name;
    private String unit;
    private Integer blockMinutes;
    private BigDecimal price;
    private String currency;
    private LocalDate appliesFrom;
    private LocalDate appliesTo;
    private boolean active;
}
