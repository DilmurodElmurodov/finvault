package com.finvault.dto.request;

import com.finvault.entity.Portfolio;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PortfolioRequest {
    @NotBlank(message = "Portfolio name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @DecimalMin(value = "0.0")
    private BigDecimal initialInvestment;

    private Portfolio.PortfolioType type;
    private Boolean isDefault;
}
