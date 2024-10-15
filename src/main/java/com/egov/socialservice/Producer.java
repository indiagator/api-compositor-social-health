package com.egov.socialservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class Producer
{
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    private static final String TOPIC = "social-events";

    @Autowired //DEPENDENCY INJECTION PROMISE FULFILLED AT RUNTIME
    private KafkaTemplate<String, String> kafkaTemplate ;

    @Autowired
    SocialEvent1 socialEvent1;


    public void pubSocialEvent_1(String type, UUID userid) throws JsonProcessingException // LOGIN | REGISTER
    {
        //SocialEvent1 socialEvent1 = new SocialEvent1();
        socialEvent1.setType(type);
        socialEvent1.setUsername(userid);
        socialEvent1.setService_name("social-service");

        // convert to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String datum =  objectMapper.writeValueAsString(socialEvent1);

        logger.info(String.format("#### -> Producing message -> %s", datum));
        this.kafkaTemplate.send(TOPIC,"social-key-1", datum);
    }

}
