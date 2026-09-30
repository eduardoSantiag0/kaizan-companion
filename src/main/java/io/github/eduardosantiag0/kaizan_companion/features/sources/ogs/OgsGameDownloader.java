package io.github.eduardosantiag0.kaizan_companion.features.sources.ogs;

import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISource;
import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import io.github.eduardosantiag0.sgf.parser.SgfParseException;
import io.github.eduardosantiag0.sgf.parser.SgfParser;
import io.github.eduardosantiag0.sgf.parser.SgfParserOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class OgsGameDownloader implements ISource {


    @Value("${ogs.baseurl}")
    private String BASE_URL;
    private final SgfParser parser = new SgfParser();
    private final RestTemplate rest = new RestTemplate();

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

            var data = rest.getForObject(endpoint, byte[].class);
            if (data != null) {
                SgfCollection collection = new SgfParser().parse(data);
                SgfGameTree game = collection.game(0);
                SgfNode root = game.root();

                String black = root.value("PB").orElse("desconhecido");
                String white = root.value("PW").orElse("desconhecido");
                log.debug("Jogadores: {} vs {}", black, white);
                return data;
            }


        } catch (RestClientException e) {
            System.out.println("Error fetching data: " + e.getMessage());
        }


        return null;
    }
}


