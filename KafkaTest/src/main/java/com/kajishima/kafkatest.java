package com.kajishima;

import java.util.Properties;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class kafkatest {
    
    private static final CountCallback SEND_CALLBACK = new CountCallback();
    
    private static class CountCallback implements Callback {

        @Override
        public void onCompletion(RecordMetadata rm, Exception excptn) {
            if (excptn != null) {
                System.err.println(excptn.getMessage());
		System.exit(1);
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("group.id","example");
        
        try ( // Kafkaへデータを送る
                KafkaProducer<String,String> kafkaProducer = new KafkaProducer<>(props)) {
            ProducerRecord<String,String> record = new ProducerRecord<>("kafka-test","test-title","content");
            kafkaProducer.send(record, SEND_CALLBACK);
        }
    }
}
