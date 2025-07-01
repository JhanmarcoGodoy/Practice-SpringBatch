// Ubicación: src/main/java/com/mysql/demo/Batch/CsvImportBatchConfig.java

package com.batch.demo.Batch;

import com.batch.demo.Model.User;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class CsvImportBatchConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public FlatFileItemReader<User> csvUserReader() {
        return new FlatFileItemReaderBuilder<User>()
                .name("csvUserReader")
                .resource(new ClassPathResource("data/users.csv"))
                .delimited()
                .names("firstName", "lastName", "email")
                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(User.class);
                }})
                .build();
    }

    @Bean
    public ItemProcessor<User, User> csvUserProcessor() {
        return user -> {
            if (user.getEmail() != null) {
                user.setEmail(user.getEmail().toLowerCase());
            }
            return user;
        };
    }

    @Bean
    public ItemWriter<User> jdbcUserWriter() {
        return new JdbcBatchItemWriterBuilder<User>()
                .dataSource(dataSource)
                .sql("INSERT INTO users (first_name, last_name, email) VALUES (:firstName, :lastName, :email)")
                .beanMapped()
                .build();
    }

    @Bean
    public Step importCsvStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              @Qualifier("csvUserReader") ItemReader<User> reader,
                              @Qualifier("csvUserProcessor") ItemProcessor<User, User> processor,
                              @Qualifier("jdbcUserWriter") ItemWriter<User> writer) {
        return new StepBuilder("importCsvStep", jobRepository)
                .<User, User>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository, @Qualifier("importCsvStep") Step importCsvStep) {
        return new JobBuilder("importUserJob", jobRepository)
                .flow(importCsvStep)
                .end()
                .build();
    }
}