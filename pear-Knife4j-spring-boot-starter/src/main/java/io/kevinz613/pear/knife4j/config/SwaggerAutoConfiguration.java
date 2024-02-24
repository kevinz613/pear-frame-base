package io.kevinz613.pear.knife4j.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 自动配置
 *
 * @author kevinz613
 */
@Configuration
@ConditionalOnProperty(name = "swagger.config.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({SwaggerProperties.class})
public class SwaggerAutoConfiguration {

    @Bean
    public OpenAPI OpenApi(SwaggerProperties properties) {

        return new OpenAPI().info(new Info()
                // 标题
                .title(properties.getTitle())
                // 描述
                .description(properties.getDescription())
                // 版本
                .version(properties.getVersion())
                // 许可证
                .license(myLicense(properties.getLicense()))
                // 联系人信息
                .contact(myContact(properties.getContact()))
                // summary
                .summary(properties.getSummary())
                // termsOfService
                .termsOfService(properties.getTermsOfService())
        );
    }

    private License myLicense(SwaggerProperties.License license) {
        License myLicense = new License();
        myLicense.setName(license.getName());
        myLicense.setUrl(license.getUrl());
        myLicense.setIdentifier(license.getIdentifier());
        return myLicense;
    }

    private Contact myContact(SwaggerProperties.Contact contact) {
        Contact myContact = new Contact();
        myContact.setName(contact.getName());
        myContact.setUrl(contact.getUrl());
        myContact.setEmail(contact.getEmail());
        return myContact;
    }

}
