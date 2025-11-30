package bg.sit_varna.sit.si.repository;

import bg.sit_varna.sit.si.entity.TemplateRecord;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TemplateRepository implements PanacheRepositoryBase<TemplateRecord, UUID> {

    public Optional<TemplateRecord> findByNameAndLocale(String templateName, String locale) {
        return find("templateName = ?1 and locale = ?2", templateName, locale).firstResultOptional();
    }

    public boolean exists(String templateName, String locale) {
        return count("templateName = ?1 and locale = ?2", templateName, locale) > 0;
    }
}
