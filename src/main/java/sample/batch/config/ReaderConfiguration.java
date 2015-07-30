package sample.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import sample.domain.model.Person;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@Slf4j
public class ReaderConfiguration {
    private final static String FILE_NAME = "sample-data.csv";

    @Bean
    public ItemReader<Person> csvItemReader() {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(FILE_NAME));
//        reader.setResource(new FileSystemResource(FILE_NAME)); // read system file.
//        reader.setLinesToSkip(1); // skip header.
        reader.setLineMapper(new DefaultLineMapper<Person>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
//                setDelimiter(DelimitedLineTokenizer.DELIMITER_TAB); // read tvs.
                setNames(new String[]{"firstName", "lastName", "mail"});
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }});
        }});

        return reader;
    }

    @Bean(destroyMethod = "") // WARNING: org.springframework.batch.item.ItemStreamException: Error while closing item reader
    public ItemReader<Person> jpaItemReader(EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<Person> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("select emp from Person emp");

        return reader;
    }

    @Bean
    public ItemReader<Person> jdbcPagingItemReader(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean bean = new SqlPagingQueryProviderFactoryBean();
        bean.setDataSource(dataSource);
        bean.setSelectClause("*");
        bean.setFromClause("PERSONS");
        bean.setSortKey("ID");

        JdbcPagingItemReader<Person> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        try {
            reader.setQueryProvider(bean.getObject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        reader.setRowMapper((rs, rowNum) ->
                        Person.builder()
                                .firstName(rs.getString("FIRST_NAME"))
                                .lastName(rs.getString("LAST_NAME"))
                                .mail(rs.getString("MAIL"))
                                .build()
        );

        return reader;
    }
}
