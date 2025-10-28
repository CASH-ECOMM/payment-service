package com.paymentservice.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    @ApiResponse(
            responseCode = "200",
            description = "Health check",
            content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(example = "✅ Payment service is up and running!")
            )
    )
    public String healthCheck() {
        return "✅ Payment service is up and running!";
    }
}
