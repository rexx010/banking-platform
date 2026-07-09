package com.bankplatform.shared.config;

import com.bankplatform.shared.exceptions.GlobalExceeptionHandler;
import com.bankplatform.shared.kafka.KafkaMdcPropagation;
import com.bankplatform.shared.logging.MdcRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@AutoConfiguration
public class SharedKernelAutoConfiguration {

    @Bean
    public MdcRequestFilter mdcRequestFilter(){
        return new MdcRequestFilter();
    }

    @Bean
    public GlobalExceeptionHandler globalExceeptionHandler(){
        return new GlobalExceeptionHandler();
    }

    @Bean
    public KafkaMdcPropagation.MdcConsumerInterceptor<Object, Object> mdcConsumerInterceptor(){
        return new KafkaMdcPropagation.MdcConsumerInterceptor<>();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaMdcPropagation.MdcConsumerInterceptor<Object, Object> interceptor
    ){
        var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.setRecordInterceptor(interceptor);
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener
                        .ContainerProperties.AckMode.MANUAL_IMMEDIATE
        );
        return factory;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }
}
