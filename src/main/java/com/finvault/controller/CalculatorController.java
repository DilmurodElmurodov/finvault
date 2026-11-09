package com.finvault.controller;

import com.finvault.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Calculator Controller - Financial calculations: EMI, Compound Interest, ROI, SIP
 */
@RestController
@RequestMapping("/calculator")
@Tag(name = "Financial Calculator", description = "Finance calculation endpoints")
public class CalculatorController {

    @PostMapping("/emi")
    @Operation(summary = "Calculate EMI (Equated Monthly Installment)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateEmi(
            @RequestParam double principal,
            @RequestParam double annualRatePercent,
            @RequestParam int tenureMonths) {

        double monthlyRate = annualRatePercent / 12 / 100;
        double emi;
        if (monthlyRate == 0) {
            emi = principal / tenureMonths;
        } else {
            double factor = Math.pow(1 + monthlyRate, tenureMonths);
            emi = principal * monthlyRate * factor / (factor - 1);
        }
        double totalPayment = emi * tenureMonths;
        double totalInterest = totalPayment - principal;

        // Generate amortization schedule (first 24 months)
        List<Map<String, Object>> schedule = new ArrayList<>();
        double balance = principal;
        for (int i = 1; i <= Math.min(tenureMonths, 24); i++) {
            double interest = balance * monthlyRate;
            double principalPaid = emi - interest;
            balance -= principalPaid;
            schedule.add(Map.of(
                "month", i,
                "emi", round(emi),
                "principal", round(principalPaid),
                "interest", round(interest),
                "balance", round(Math.max(0, balance))
            ));
        }

        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "monthlyEmi", round(emi),
            "totalPayment", round(totalPayment),
            "totalInterest", round(totalInterest),
            "principal", principal,
            "annualRate", annualRatePercent,
            "tenure", tenureMonths,
            "amortizationSchedule", schedule
        )));
    }

    @PostMapping("/compound-interest")
    @Operation(summary = "Calculate Compound Interest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateCompoundInterest(
            @RequestParam double principal,
            @RequestParam double annualRatePercent,
            @RequestParam int years,
            @RequestParam(defaultValue = "12") int compoundingFrequency) {

        double rate = annualRatePercent / 100;
        double futureValue = principal * Math.pow(1 + rate / compoundingFrequency,
                compoundingFrequency * years);
        double totalInterest = futureValue - principal;

        List<Map<String, Object>> yearlyGrowth = new ArrayList<>();
        for (int y = 0; y <= years; y++) {
            double val = principal * Math.pow(1 + rate / compoundingFrequency,
                    compoundingFrequency * y);
            yearlyGrowth.add(Map.of("year", y, "value", round(val)));
        }

        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "futureValue", round(futureValue),
            "totalInterest", round(totalInterest),
            "returnPercent", round((totalInterest / principal) * 100),
            "principal", principal,
            "yearlyGrowth", yearlyGrowth
        )));
    }

    @PostMapping("/roi")
    @Operation(summary = "Calculate Return on Investment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateRoi(
            @RequestParam double investment,
            @RequestParam double returns,
            @RequestParam int months) {

        double netProfit = returns - investment;
        double roi = (netProfit / investment) * 100;
        double annualizedRoi = (Math.pow(returns / investment, 12.0 / months) - 1) * 100;

        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "roi", round(roi),
            "annualizedRoi", round(annualizedRoi),
            "netProfit", round(netProfit),
            "multiplier", round(returns / investment),
            "investment", investment,
            "returns", returns
        )));
    }

    @PostMapping("/sip")
    @Operation(summary = "Calculate SIP (Systematic Investment Plan)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateSip(
            @RequestParam double monthlyInvestment,
            @RequestParam double annualRatePercent,
            @RequestParam int years) {

        double monthlyRate = annualRatePercent / 12 / 100;
        int months = years * 12;
        double futureValue = monthlyInvestment *
                (Math.pow(1 + monthlyRate, months) - 1) / monthlyRate * (1 + monthlyRate);
        double totalInvested = monthlyInvestment * months;
        double wealthGained = futureValue - totalInvested;

        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "futureValue", round(futureValue),
            "totalInvested", round(totalInvested),
            "wealthGained", round(wealthGained),
            "returnPercent", round((wealthGained / totalInvested) * 100),
            "monthlyInvestment", monthlyInvestment
        )));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
