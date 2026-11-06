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
            \n
            ⚔ THE MATCH

            ● %s
                 VS
            ○ %s
            """.formatted(black, white);

        return formatCompleted(match);
    }

    public static String formatStart(String firstName) {
        return """
            \n
            ╭─── ⛩ KAIZEN COMPANION ⛩ ───╮

              Welcome, %s!

              I analyse your Go (Baduk) games with
              KataGo and send back a review split
              into opening, middlegame and endgame.

            ╰───────────────────────────────╯

            ◉ GET STARTED

              1. Link an account (optional, lets
                 you use /random and /last_game):
                 /add_account_ogs <username>
                 /add_account_dragon <url>

              2. Or just send a game right away:
                 /study <url>
                 /collection <url1> <url2> ...

              3. Track and manage:
                 /queue             pending analyses
                 /resign            cancel a running one
                 /my_profile        your linked accounts
                 /add_email         also receive by email
                 /add_google_drive  store results in Drive
                 /unlink            remove a link

            ● ━━━━━━━ ◉ ━━━━━━━ ○
            """.formatted(firstName);
    }

    public static String formatMyProfile(UserInfoEntity userInfo) {
        return """
            \n
            ╭─── ⛩ KAIZEN ⛩ ───╮

              ◉ MY PROFILE

            %s
            ╰───────────────────╯
            """.formatted(userInfo.toString());
    }

    private static String formatCompleted(String details) {
        return """
            \n
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
            \n
            ⏳ ANALYSIS QUEUED

            Your game is waiting to be analyzed.

            ◉ Job ID: %s

            📊 Track your progress with /queue

            ● ─── ◌ ─── ○
            """.formatted(correlationId);
    }


    public static String requestFailed() {
        return """
            \n
            ┌─ ✕ REQUEST FAILED ✕  ─────────┐

              We were not able to process your request.

              ↻ Try again?

            └───────────────────────┘
            """;

    }


    public static String unknownMessage() {
        return """
            \n
            ┌─ ✕ I DIDN'T UNDERSTAND ✕ ───────┐

              Send a game link (OGS or Dragon),
              or use /start to see what I can do.

            └─────────────────────────────────┘
            """;
    }

    public static String systemOffline() {
        return """
        \n
        ┌─ ✕ SYSTEM OFFLINE ✕ ───────────┐

          Our service is temporarily unavailable.

          ↻ Please try again later.

        └───────────────────────────────┘
        """;
    }


    public static String formatAccountLinked(String identifier, String platform) {
        return """
            \n
            ╭─── ⛩ KAIZEN ⛩ ───╮

              ✓ ACCOUNT LINKED!

              ◉ Player: %s
              ◉ Platform: %s

            ╰───────────────────╯
            """.formatted(identifier, platform);
    }

    public static String formatUnlinked(String target) {
        return """
            \n
            ⛩ KAIZEN COMPANION
            ━━━━━━━━━━━━━━━━━━━

            ✓ UNLINKED!

            ◉ %s is no longer linked to your profile

            ● ─── ◉ ─── ○
            """.formatted(target);
    }

    public static String formatEmailLinked(String email) {

        return """
            \n
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
                \n
                ✓ QUEUE IS EMPTY

                No pending analyses.

                ● ─── ◉ ─── ○
                """;
        }

        StringBuilder builder = new StringBuilder(
            """
            \n
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

    public static String formatLinkGoogle(String url) {
        return "Click on the following link to connect: \n" + url;
    }

    }
