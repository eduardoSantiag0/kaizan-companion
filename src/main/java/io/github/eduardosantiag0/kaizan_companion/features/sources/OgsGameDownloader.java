package io.github.eduardosantiag0.kaizan_companion.features.sources;

import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISource;
import io.github.eduardosantiag0.sgf.parser.SgfParseException;
import io.github.eduardosantiag0.sgf.parser.SgfParser;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class OgsGameDownloader implements ISource {

    private final String BASE_URL = "https://online-go.com/api/v1/games/";
    private final RestTemplate rest = new RestTemplate();
    private final SgfParser sgfParser = new SgfParser();

    public OgsGameDownloader() {
    }

    private String buildEndpoint (String id) {
        return BASE_URL + id + "/sgf/";
    }
    @Override
    public byte[] downloadGame(String url) throws SgfParseException {
        String id = url.substring(url.indexOf("/game/") + "/game/".length());

        String endpoint = buildEndpoint(id);

        System.out.println(endpoint);

        try {

            return rest.getForObject(endpoint, byte[].class);

        } catch (RestClientException e) {
            System.out.println("Error fetching data: " + e.getMessage());
        }


        return null;
    }
}


