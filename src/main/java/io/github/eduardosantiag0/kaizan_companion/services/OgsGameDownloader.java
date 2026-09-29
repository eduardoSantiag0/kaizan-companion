package io.github.eduardosantiag0.kaizan_companion.services;

import io.github.eduardosantiag0.kaizan_companion.interfaces.ISource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Component
public class OgsGameDownloader implements ISource {

    private final String BASE_URL = "https://online-go.com/";
    private final RestTemplate rest = new RestTemplate();

    private String buildEndpoint (String id) {
        return "https://online-go.com/api/v1/games/" + id + "sgf/.";
    }

    @Override
    public void downloadGame(String url) {
        String id = url.substring(url.indexOf("/game/"));

        String endpoint = buildEndpoint(id);

//        https://online-go.com/api/v1/games/91140771/sgf/.
//        "api/v1/games/{id}/sgf/."
        try {

            var file = rest.getForObject(endpoint, MultipartFile.class);
            System.out.println(file.getName());

        } catch (RestClientException e) {
            System.out.println("Error fetching data: " + e.getMessage());
        }


    }
}
