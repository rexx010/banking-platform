package com.bankplatform.shared.kafka;

import com.bankplatform.shared.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.slf4j.MDC;

import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public final class KafkaMdcPropagation {
    private KafkaMdcPropagation(){}

    static final String TRACE_HEADER = "X-Trace_Id";
    static final String MDC_TRACE = "traceId";

    /**PRODUCER SIDE
     Registered in application.yml under:
     spring.kafka.producer.properties.interceptor.classes*/
    @Slf4j
    public static class MdcProducerInterceptor<K, V> implements ProducerInterceptor<K, V>{

        @Override
        public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
            String traceId = MDC.get(MDC_TRACE);
            if (traceId != null && !traceId.isBlank()) {
                record.headers().add(
                        TRACE_HEADER,
                        traceId.getBytes(StandardCharsets.UTF_8)
                );
            }
            return record;
        }

        @Override
        public void onAcknowledgement(
                RecordMetadata metadata, Exception exception) {}

        @Override public void close() {}

        @Override public void configure(Map<String, ?> configs) {}
    }

    /**CONSUMER SIDE
     Registered as a Spring bean and wired into the
     Kafka listener container factory in the auto-config*/
    @Slf4j
    @Component
    public static class MdcConsumerInterceptor<K, V> implements RecordInterceptor<K, V> {

        @Override
        public ConsumerRecord<K, V> intercept(
                @NonNull ConsumerRecord<K, V> record,
                @NonNull Consumer<K, V> consumer
        ) {
            String traceId = extractHeader(record.headers(), TRACE_HEADER)
                    .orElseGet(IdGenerator::generateCompact);

            MDC.put(MDC_TRACE, traceId);
            MDC.put("kafkaTopic", record.topic());
            MDC.put("kafkaPartition", String.valueOf(record.partition()));
            MDC.put("kafkaOffset", String.valueOf(record.offset()));

            return record;
        }

        @Override
        public void afterRecord(
                @NonNull ConsumerRecord<K, V> record,
                @NonNull Consumer<K, V> consumer
        ) {
            MDC.remove(MDC_TRACE);
            MDC.remove("kafkaTopic");
            MDC.remove("kafkaPartition");
            MDC.remove("kafkaOffset");
        }

        private Optional<String> extractHeader(Headers headers, String key) {
            var header = headers.lastHeader(key);
            if (header == null || header.value() == null) {
                return Optional.empty();
            }
            return Optional.of(
                    new String(header.value(), StandardCharsets.UTF_8)
            );
        }
    }
}
