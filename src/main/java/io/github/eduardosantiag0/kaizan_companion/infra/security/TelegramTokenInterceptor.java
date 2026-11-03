package io.github.eduardosantiag0.kaizan_companion.infra.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TelegramTokenInterceptor implements HandlerInterceptor  {
    @Value("${telegram.bot.token}")
    private String secretToken;
    private final String TELEGRAM_HEADER="X-Telegram-Bot-Api-Secret-Token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String tokenRecebido = request.getHeader(TELEGRAM_HEADER);

        if (tokenRecebido == null || !tokenRecebido.equals(secretToken)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return false;
        }

        return true;
    }
}
