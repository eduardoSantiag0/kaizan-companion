package io.github.eduardosantiag0.kaizan_companion.utils;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.AnalysisJobEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.UserInfoEntity;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos.MinimalPlayer;
import io.github.eduardosantiag0.sgf.model.SgfCollection;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import io.github.eduardosantiag0.sgf.model.SgfNode;
import io.github.eduardosantiag0.sgf.parser.SgfParser;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class MessageFormatter {

    public static String requestCompleted(String fileName) {
        return formatCompleted("📄 Game: " + fileName);
    }

    public static String requestCompleted(byte[] file) {

        SgfCollection collection = new SgfParser().parse(file);
        SgfGameTree game = collection.game(0);
        SgfNode root = game.root();

        String black = root.value("PB").orElse("Unknown");
        String white = root.value("PW").orElse("Unknown");

        String match = """
            ⚔ THE MATCH

            ● %s
                 VS
            ○ %s
            """.formatted(black, white);

        return formatCompleted(match);
    }

    public static String formatMyProfile(UserInfoEntity userInfo) {
        return """
            ╭─── ⛩ KAIZEN ⛩ ───╮

              ◉ MY PROFILE

            %s
            ╰───────────────────╯
            """.formatted(userInfo.toString());
    }

    private static String formatCompleted(String details) {
        return """
            ╭─── ⛩ KAIZEN COMPANION ⛩ ───╮

                   ● ─┬─ ○
                   │  │  │
                   ├─ ◉ ─┤
                   │  │  │
                   ○ ─┴─ ●

              ◉ ANALYSIS COMPLETE!

            ╰─────────────────────────────╯

            %s

            ● ━━━━━━━ ◉ ━━━━━━━ ○
            """.formatted(details);
    }


    public static String requestsIsInQueue(String correlationId) {
        return """
            ⏳ ANALYSIS QUEUED

            Your game is waiting to be analyzed.

            ◉ Job ID: %s

            📊 Track your progress with /queue

            ● ─── ◌ ─── ○
            """.formatted(correlationId);
    }


    public static String requestFailed() {
        return """
            ┌─ ✕ REQUEST FAILED ✕  ─────────┐

              We were not able to process your request.

              ↻ Try again?

            └───────────────────────┘
            """;

    }

    public static String formatAccountLinked(String identifier, String platform) {
        return """
            ╭─── ⛩ KAIZEN ⛩ ───╮

              ✓ ACCOUNT LINKED!

              ◉ Player: %s
              ◉ Platform: %s

            ╰───────────────────╯
            """.formatted(identifier, platform);
    }

    public static String formatUnlinked(String target) {
        return """
            ⛩ KAIZEN COMPANION
            ━━━━━━━━━━━━━━━━━━━

            ✓ UNLINKED!

            ◉ %s is no longer linked to your profile

            ● ─── ◉ ─── ○
            """.formatted(target);
    }

    public static String formatEmailLinked(String email) {

        return """
            ⛩ KAIZEN COMPANION
            ━━━━━━━━━━━━━━━━━━━

            ✓ EMAIL LINKED!

            ✉ %s

            ● ─── ◉ ─── ○
            """.formatted(email);
    }

    public static String formatQueue(List<AnalysisJobEntity> jobs) {
        if (jobs.isEmpty()) {
            return """
                ✓ QUEUE IS EMPTY

                No pending analyses.

                ● ─── ◉ ─── ○
                """;
        }

        StringBuilder builder = new StringBuilder("""
            ◉ KAIZEN QUEUE
            ━━━━━━━━━━━━━━━━━━━

            ⏳ Pending analyses: %d

            """.formatted(jobs.size()));

        for (int index = 0; index < jobs.size(); index++) {
            AnalysisJobEntity job = jobs.get(index);

            String status = switch (job.getJobStatus()) {
                case ANALYSIS_REQUESTED -> "◌ Requested";
                case IN_QUEUE -> "⏳ In queue";
                case COMPLETED -> "✓ Completed";
                default -> "◉ " + job.getJobStatus();
            };

            builder.append("""
                %d. %s
                   ID: %s

                """.formatted(
                    index + 1,
                    status,
                    job.getCorrelationId()
            ));
        }

        return builder.toString();
    }

}
