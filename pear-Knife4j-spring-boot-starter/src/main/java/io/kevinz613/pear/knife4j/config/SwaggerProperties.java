package io.kevinz613.pear.knife4j.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Swagger 属性
 *
 * @author kevinz613
 */

@Data
@ConfigurationProperties("swagger.config")
public class SwaggerProperties {

    /**
     * 开启swagger
     */
    private Boolean enabled;

    /**
     * 标题
     */
    private String title = "";

    /**
     * 版本
     */
    private String version = "";

    /**
     * 描述
     */
    private String description = "";

    /**
     * 许可证
     */
    private License license = new License();

    /**
     * 联系
     */
    private Contact contact = new Contact();

    private String summary = "";

    private String termsOfService = "";

    @Data
    @NoArgsConstructor
    public static class Contact {
        private String name;

        private String url;

        private String email;
    }

    @Data
    @NoArgsConstructor
    public static class License {
        private String name = "";
        private String url = "";
        private String identifier = "";
    }


}
