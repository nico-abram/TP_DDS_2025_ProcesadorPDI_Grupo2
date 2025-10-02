package ar.edu.utn.dds.k3003.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OcrResponse {

    @JsonProperty("ParsedResults")
    public List<ParsedResult> parsedResults;

    public static class ParsedResult {
        @JsonProperty("ParsedText")
        public String parsedText;
    }
}
