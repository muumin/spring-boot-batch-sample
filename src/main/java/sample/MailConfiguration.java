package sample;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.DataSourceResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MailConfiguration {
    @Bean
    public ResourceLoader templateLoader(DataSource dataSource) {
        DataSourceResourceLoader loader = new DataSourceResourceLoader();
        loader.setDataSource(dataSource);
        return loader;
    }

    @Bean
    public VelocityEngine velocityDBEngine(ResourceLoader templateLoader) throws IOException {
        VelocityEngineFactoryBean velocityEngineFactoryBean = new VelocityEngineFactoryBean();
        velocityEngineFactoryBean.setPreferFileSystemAccess(false);

        Map<String, Object> map = new HashMap<>();
        map.put("resource.loader", "ds");
        map.put("ds.resource.loader.instance", templateLoader);
        map.put("ds.resource.loader.resource.table", "velocity_template");
        map.put("ds.resource.loader.resource.keycolumn", "id");
        map.put("ds.resource.loader.resource.templatecolumn", "def");
        map.put("ds.resource.loader.resource.timestampcolumn", "last_modified");

        velocityEngineFactoryBean.setVelocityPropertiesMap(map);

        return velocityEngineFactoryBean.createVelocityEngine();
    }
}
