package com.github.storytime.config;

import com.github.storytime.model.pb.jaxb.statement.response.error.Response;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringWriter;

import static javax.xml.bind.JAXBContext.newInstance;

@Configuration
public class JAXBConfig {

    @Bean
    public StringWriter stringWriter() {
        return new StringWriter();
    }

    @Bean
    public Marshaller jaxbMarshaller() throws JAXBException {
        final JAXBContext jaxbContext = newInstance(com.github.storytime.model.pb.jaxb.statement.response.ok.Response.class);
        return jaxbContext.createMarshaller();
    }

    @Bean
    public Unmarshaller jaxbStatementErrorUnmarshaller() throws JAXBException {
        final JAXBContext jaxbContext = newInstance(Response.class);
        return jaxbContext.createUnmarshaller();
    }

    @Bean
    public Unmarshaller jaxbStatementOkUnmarshaller() throws JAXBException {
        final JAXBContext jaxbContext = newInstance(com.github.storytime.model.pb.jaxb.statement.response.ok.Response.class);
        return jaxbContext.createUnmarshaller();
    }

    @Bean
    public Unmarshaller jaxbAccountOkUnmarshaller() throws JAXBException {
        final JAXBContext jaxbContext = newInstance(com.github.storytime.model.pb.jaxb.account.response.Response.class);
        return jaxbContext.createUnmarshaller();
    }
}
