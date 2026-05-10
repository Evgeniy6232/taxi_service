package com.taxi.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class ProxyController {

    private final RestClient client = RestClient.create();

    @RequestMapping("/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request,
                                        @RequestBody(required = false) byte[] body) {
        String path = request.getRequestURI();
        String backend = resolveBackend(path);
        String query = request.getQueryString();
        String url = backend + path + (query != null ? "?" + query : "");
        String method = request.getMethod();

        RestClient.RequestHeadersSpec<?> spec;

        if ("POST".equals(method)) {
            var s = client.post().uri(url);
            if (body != null && body.length > 0) s.body(body);
            spec = s;
        } else if ("PATCH".equals(method)) {
            var s = client.patch().uri(url);
            if (body != null && body.length > 0) s.body(body);
            spec = s;
        } else if ("PUT".equals(method)) {
            var s = client.put().uri(url);
            if (body != null && body.length > 0) s.body(body);
            spec = s;
        } else {
            spec = client.get().uri(url);
        }

        // Forward relevant headers
        var headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String name = headers.nextElement();
            if (!"host".equalsIgnoreCase(name) && !"content-length".equalsIgnoreCase(name)) {
                spec.header(name, request.getHeader(name));
            }
        }

        try {
            var res = spec.retrieve().toEntity(byte[].class);
            return ResponseEntity.status(res.getStatusCode())
                    .headers(res.getHeaders())
                    .body(res.getBody() != null ? res.getBody() : new byte[0]);
        } catch (Exception e) {
            return ResponseEntity.status(502)
                    .body(("Proxy error: " + e.getMessage()).getBytes());
        }
    }

    private String resolveBackend(String path) {
        if (path.startsWith("/trips") || path.startsWith("/stats")) {
            return "http://localhost:8082";
        }
        if (path.startsWith("/notifications")) {
            return "http://localhost:8083";
        }
        return "http://localhost:8081";
    }
}
