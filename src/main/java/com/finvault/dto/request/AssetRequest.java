package com.finvault.dto.request;

import com.finvault.entity.Asset;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AssetRequest {
    @NotBlank(message = "Symbol is required")
    @Size(max = 20)
    private String symbol;

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    private String name;

    private Asset.AssetType assetType;

    @NotNull
    @DecimalMin("0.00000001")
    private BigDecimal quantity;

    @NotNull
    @DecimalMin("0.000001")
    private BigDecimal averageBuyPrice;

    private BigDecimal currentPrice;
}
