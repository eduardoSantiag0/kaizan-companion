package io.github.eduardosantiag0.kaizan_companion.services;

import io.github.eduardosantiag0.kaizan_companion.interfaces.ICommands;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Service
public class CommandHandler implements ICommands {

    private final AnalysisService analysisService;

    public CommandHandler(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    public void execute(Update update) {
        String[] fullMessage = new String[]{update.getMessage().getText()};
        String command = fullMessage[0];

        switch (command) {
            case "/study" -> {
                String url = fullMessage[2];
                study(url);
            }


        }

    }


    public static boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void study(String url) {
        // Prepara o commando
        isValidURL(url);
        analysisService.study(url);

    }

    @Override
    public void collection() {

    }

    @Override
    public void addEmail() {

    }

    @Override
    public void addGoodleDrive() {

    }

    @Override
    public void addMyProfile() {

    }

    @Override
    public void latest() {

    }
}
