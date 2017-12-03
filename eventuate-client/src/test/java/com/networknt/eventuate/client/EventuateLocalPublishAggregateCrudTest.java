package com.networknt.eventuate.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.eventuate.cdc.common.PublishedEvent;
import com.networknt.eventuate.common.impl.EventIdTypeAndData;
import com.networknt.eventuate.jdbc.IdGeneratorImpl;
import org.apache.kafka.clients.producer.MockProducer;
import org.junit.BeforeClass;
import org.junit.Test;


public class EventuateLocalPublishAggregateCrudTest {

    /*
    static CdcKafkaProducer cdcKafkaProducer;
    static PublishedEvent event;

    @BeforeClass
    public static void setUp() {


        String aggregateType = "EventuateLocalPublishAggregateCrudTest.topic";
        String aggregateId = "EventuateLocalPublishAggregateCrudTest.topic.id";
        EventIdTypeAndData eventIdAndData = new EventIdTypeAndData ( new IdGeneratorImpl().genId(), "EventuateLocalPublishAggregateCrudTest.event", "{\"todo\":{\"title\":\" this is the URL todo\",\"completed\":false,\"order\":0}}");
        event = toPublishedEvent(aggregateType, aggregateId, eventIdAndData);
        cdcKafkaProducer = new CdcKafkaProducer();
        cdcKafkaProducer.setProducer( new MockProducer(true, null , null, null) );
       }


    @Test
    public void testSend() {
        cdcKafkaProducer.send(event.getEntityType(), event.getId(), toJson(event));
     }

    @Test(expected=IllegalArgumentException.class)
    public void testSendWithoutTopic() {
         cdcKafkaProducer.send(null, event.getId(), toJson(event));
    }


    private String toJson(PublishedEvent eventInfo) {
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(eventInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PublishedEvent toPublishedEvent( String aggregateType, String aggregateId, EventIdTypeAndData event) {
        return new PublishedEvent(event.getId().toString(), aggregateId, aggregateType, event.getEventData(), event.getEventType());
    }
    */
}
