package ar.edu.utn.dds.k3003.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrResponse {

    @JsonProperty("ParsedResults")
    public List<ParsedResult> parsedResults;

    public static class ParsedResult {
        @JsonProperty("ParsedText")
        public String parsedText;
    }
}
