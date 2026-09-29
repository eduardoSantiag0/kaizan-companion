package io.github.eduardosantiag0.kaizan_companion.features.telegram;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.AnalysisService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class CommandHandler implements ICommands {

    private final AnalysisService analysisService;

    public CommandHandler(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    public void execute(Update update) {

        String[] fullMessage = update.getMessage().getText().split("\\s+");;
//        String[] fullMessage = new String[]{update.getMessage().getText()};
        String command = fullMessage[0];

        switch (command) {
            case "/study" -> {
                String url = fullMessage[1];
                study(url);
            }
        }
    }




    @Override
    public void study(String url) {
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
