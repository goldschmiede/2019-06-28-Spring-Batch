package com.anderscore.goldschmiede.springbatch.samples.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.core.io.ClassPathResource;

public class FlatFileItemReaderTest {

    @Test
    void testReadSampleData() throws Exception {
        ItemStreamReader<Person> reader = createPersonReader();
        List<Person> persons = readPersons(reader);
        assertThat(persons).hasSize(3)
                .contains(new Person("Anna", "Gramm"),
                        new Person("Franz", "Brandwein"),
                        new Person("Izmir", "Egal"));
    }

    // tag::createPersonReader[]
    private FlatFileItemReader<Person> createPersonReader() {
        FlatFileItemReader<Person> reader = new FlatFileItemReaderBuilder<Person>()
                .name("personReader") // alternativ: .saveState(false)
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited().delimiter(";")
                .names(new String[] { "firstName", "lastName" }) // LineTokenizer
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {
                    {
                        setTargetType(Person.class);
                    }
                })
                .linesToSkip(1)
                .build();
        return reader;
    }
    // end::createPersonReader[]

    // tag::readPersons[]
    private List<Person> readPersons(ItemStreamReader<Person> reader) throws Exception {
        List<Person> persons = new ArrayList<>();
        Person person;
        ExecutionContext ec = new ExecutionContext(); // Damit speichert der Reader seinen Fortschritt
        reader.open(ec);
        while ((person = reader.read()) != null) {
            persons.add(person);
        }
        reader.close();
        return persons;
    }
    // end::readPersons[]
}
