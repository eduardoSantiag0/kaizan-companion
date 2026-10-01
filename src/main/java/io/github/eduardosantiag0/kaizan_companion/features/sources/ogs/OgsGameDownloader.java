package io.github.eduardosantiag0.kaizan_companion.features.sources.ogs;

import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISourceStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos.PaginatedGameList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class OgsGameDownloader implements ISourceStrategy {


    @Value("${ogs.baseurl}")
    private String BASE_URL;
    private final RestTemplate rest = new RestTemplate();

    public OgsGameDownloader() {
    }

    @Override
    public byte[] downloadGameById(String id) {
        String endpoint = BASE_URL + "games/" + id + "/sgf/";

        try {
            return rest.getForObject(endpoint, byte[].class);
        } catch (RestClientException e) {
            throw new RuntimeException(
                    "Error fetching game " + id,
                    e
            );
        }
    }

    @Override
    public byte[] downloadLastGame(Long playerId) {

        String endpoint =
                BASE_URL +
                        "players/" +
                        playerId +
                        "/games/?ordering=ended&page=1&page_size=1";

        try {
            PaginatedGameList response =
                    rest.getForObject(endpoint, PaginatedGameList.class);

            assert response != null;
            String url = response
                    .results()
                    .getFirst()
                    .related();

            String gameId = url.substring(url.lastIndexOf('/') + 1);

            return downloadGameById(gameId);

        } catch (RestClientException e) {
            throw new RuntimeException(
                    "Error fetching last game for player " + playerId,
                    e
            );
        }
    }
}


