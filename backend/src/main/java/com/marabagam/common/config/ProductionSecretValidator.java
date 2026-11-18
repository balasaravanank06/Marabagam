package com.marabagam.common.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class ProductionSecretValidator {

    private static final Logger log = LoggerFactory.getLogger(ProductionSecretValidator.class);

    private static final String DEV_JWT_DEFAULT = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final String DEV_CERT_DEFAULT = "marabagam_development_certificate_secret_key_2026";
    private static final String DEV_DB_DEFAULT = "marabagam_dev_password";

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${marabagam.certificate.secret:}")
    private String certificateSecret;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @PostConstruct
    public void validateProductionSecrets() {
        log.info("Validating mandatory production secrets...");

        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.equals(DEV_JWT_DEFAULT) || jwtSecret.length() < 32) {
            log.error("PRODUCTION SECURITY ERROR: Insecure or default JWT_SECRET detected!");
            throw new IllegalStateException("Production startup aborted: Mandatory JWT_SECRET must be configured with a secure, non-default 512-bit key.");
        }

        if (certificateSecret == null || certificateSecret.isBlank() || certificateSecret.equals(DEV_CERT_DEFAULT) || certificateSecret.length() < 16) {
            log.error("PRODUCTION SECURITY ERROR: Insecure or default MARABAGAM_CERTIFICATE_SECRET detected!");
            throw new IllegalStateException("Production startup aborted: Mandatory MARABAGAM_CERTIFICATE_SECRET must be configured with a secure, non-default key.");
        }

        if (dbPassword == null || dbPassword.isBlank() || dbPassword.equals(DEV_DB_DEFAULT)) {
            log.error("PRODUCTION SECURITY ERROR: Insecure or default DB_PASSWORD detected!");
            throw new IllegalStateException("Production startup aborted: Mandatory DB_PASSWORD must be configured with a non-default production password.");
        }

        log.info("Production secrets validation passed successfully.");
    }
}
