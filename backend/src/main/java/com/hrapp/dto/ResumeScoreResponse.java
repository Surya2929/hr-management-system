package com.hrapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeScoreResponse {
    private int score;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private String summary;
}
