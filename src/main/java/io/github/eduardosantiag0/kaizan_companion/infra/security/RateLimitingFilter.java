package io.github.eduardosantiag0.kaizan_companion.infra.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimitingFilter implements Filter {

    private static final Duration IDLE_BUCKET_TIMEOUT = Duration.ofMinutes(10);

    private final ConcurrentMap<String, BucketEntry> buckets = new ConcurrentHashMap<>();

    private Bucket getBucket(String clientId) {
        BucketEntry entry = buckets.computeIfAbsent(clientId, key ->
                new BucketEntry(Bucket.builder()
                        .addLimit(Bandwidth.classic(10, Refill.intervally(1, Duration.ofSeconds(1))))
                        .build()));
        entry.lastAccess = Instant.now();
        return entry.bucket;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = resolveClientIp(httpRequest);
        Bucket bucket = getBucket(clientIp);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("text/plain");
            PrintWriter writer = httpResponse.getWriter();
            writer.write("Too Many Requests");
            writer.flush();
        }
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    void evictIdleBuckets() {
        Instant cutoff = Instant.now().minus(IDLE_BUCKET_TIMEOUT);
        buckets.values().removeIf(entry -> entry.lastAccess.isBefore(cutoff));
    }

    private static final class BucketEntry {
        private final Bucket bucket;
        private volatile Instant lastAccess;

        private BucketEntry(Bucket bucket) {
            this.bucket = bucket;
            this.lastAccess = Instant.now();
        }
    }
}
