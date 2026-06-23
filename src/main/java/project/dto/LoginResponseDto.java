package project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponseDto(@JsonProperty("access_token") String accessToken,
                               @JsonProperty("token_type") String tokenType) {
}
