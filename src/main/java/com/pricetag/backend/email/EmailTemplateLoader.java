package com.pricetag.backend.email;

import com.pricetag.backend.exception.EmailTemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class EmailTemplateLoader {

    private final ResourceLoader resourceLoader;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public RenderedEmail render(String templateName, Map<String, String> vars) {
        String htmlTemplate = loadTemplate(templateName + ".html");
        String textTemplate = loadTemplate(templateName + ".txt");
        return new RenderedEmail(
                substitute(htmlTemplate, vars),
                substitute(textTemplate, vars)
        );
    }

    private String loadTemplate(String fileName) {
        return cache.computeIfAbsent(fileName, this::loadFromClasspath);
    }

    private String loadFromClasspath(String fileName) {

        String path = "classpath:templates/emails/" + fileName;
        Resource resource = resourceLoader.getResource(path);

        if (!resource.exists()) {
            throw new EmailTemplateException("Email template not found: " + fileName);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new EmailTemplateException("Failed to read email template: " + fileName, e);
        }
    }

    private String substitute(String template, Map<String, String> vars) {
        String result = template;
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            result = result.replace(
                    "${" + entry.getKey() + "}",
                    entry.getValue() == null ? "" : entry.getValue()
            );
        }
        if (result.contains("${")) {
            throw new EmailTemplateException("Unsubstituted variables in template: " + result);
        }
        return result;
    }
}
