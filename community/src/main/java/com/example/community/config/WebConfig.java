package com.example.community.config;

import com.example.community.common.web.CurrentUserIdArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;
    private final String uploadRootDir;

    public WebConfig(CurrentUserIdArgumentResolver currentUserIdArgumentResolver,
                     @Value("${app.upload.root-dir:uploads}") String uploadRootDir) {
        this.currentUserIdArgumentResolver = currentUserIdArgumentResolver;
        this.uploadRootDir = uploadRootDir;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdArgumentResolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 실제 파일 시스템 경로 (예: file:/.../uploads/)
        Path uploadPath = Paths.get(uploadRootDir).toAbsolutePath().normalize();
        String location = "file:" + uploadPath.toString() + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
