package com.egov.socialservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Tag(name = "Social Service", description = "Social Service APIs")
@RestController
@RequestMapping("/api/v1")
public class MainRestController {

    private static final Logger log = LoggerFactory.getLogger(MainRestController.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Qualifier("webClient_2")
    WebClient webClient;

    @Autowired
    @Qualifier("webClient_3")
    WebClient webClient_obs;

    @Autowired
    @Qualifier("webClientRationService_1")
    WebClient webClientRationService_1;

    @Autowired
    CredentialRepository credentialRepository;

    @Autowired
    SocialRepository socialRepository;

    @Autowired
    Producer producer;

    @Autowired
    RequestIdExtractor requestIdExtractor;



    @PostMapping("/save")
    public String saveObj(@RequestParam("key") String key, @RequestParam("value") String value)
    {
        redisTemplate.opsForValue().set(key, value);
        return "success";
    }

    @GetMapping("/register")
    public ResponseEntity<Credential> register() throws JsonProcessingException {

           Credential credential = new Credential();
           credential.setId(UUID.randomUUID());
           credential.setPassword(String.valueOf((int)(Math.random()*1000000)));

           credentialRepository.save(credential);
           //redisTemplate.opsForValue().set(credential.getCitizenid().toString(), credential.getPassword());

            producer.pubSocialEvent_1("REGISTER",credential.getId());
            return  ResponseEntity.ok(credential);
    }

    @GetMapping("/login")
     public ResponseEntity<String> login(@RequestParam("citizenid") UUID citizenid, @RequestParam("password") String password)
    {
        String passwordFromRedis = (String) redisTemplate.opsForValue().get(citizenid.toString());
        if(passwordFromRedis.equals(password))
        {
            return ResponseEntity.ok().header("Authorization","VGHJUHGJ543534534564554").body("AUTHENTICATED");
        }
        else
        {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/getdob/{citizenid}")
    public ResponseEntity<String> getdob(@PathVariable UUID citizenid)
    {


        Mono<Date> responseMono = webClient.post()
                .body(citizenid, UUID.class) // Set the request body as UUID
                .retrieve()
                .bodyToMono(Date.class); // ASYNCHRONOUS

        final Date[] finalResponse = {null};

        responseMono.subscribe(
                response -> {
                    log.info(response+" from the social service");
                    finalResponse[0] = response;
                    //redisTemplate.opsForValue().set(credential.getCitizenid().toString(), credential.getPassword());

                },
                error ->
                {
                    log.info("error processing the response "+error);
                });

        return  ResponseEntity.ok("DOB extraction initiated"); // INCORRECT APPROACH

    }

    @CircuitBreaker()
    @RateLimiter(name = "healthService")
    @Retry(name = "healthService")
    @GetMapping("/health")
    public ResponseEntity<String> getHealthStatus(HttpServletRequest request, HttpServletResponse servletResponse) // URI HANDLER
    {

        List<Cookie> cookieList = null;

        //Optional<String> healthStatusCookie = Optional.ofNullable(request.getHeader("health_status_cookie"));
        Cookie[] cookies = request.getCookies();
        if(cookies == null)
        {
            cookieList = new ArrayList<>();
        }
        else
        {
            // REFACTOR TO TAKE NULL VALUES INTO ACCOUNT
            cookieList = List.of(cookies);
        }


        if( cookieList.stream().filter(cookie -> cookie.getName().equals("ss-1")).findAny().isEmpty()) // COOKIE_CHECK
        {
            //STEP 1: DB CHANGES FOR SOCIAL-SERVICE
            Social social = new Social();
            social.setId(String.valueOf((int)(Math.random()*1000000)));
            social.setType("TESTING");
            socialRepository.save(social);

            //STEP 2: ASYNC REQUEST TO HEALTH-SERVICE
            Mono<String> responseMono = webClient.get()
                    .retrieve()
                    .bodyToMono(String.class); // ASYNCHRONOUS

            //STEP 3: COOKIE-GENERATION + INTERIM RESPONSE
            //String cookie =  String.valueOf((int)(Math.random()*1000000));

            String requestid = requestIdExtractor.getRequestId(request);
            Cookie cookie1 = new Cookie("ss-1", "ss-1-"+requestid);
            cookie1.setMaxAge(3600);

            responseMono.subscribe( // ASYNC RESPONSE HANDLER
                    response1 -> { // SUCCESS HANDLER
                        log.info(response1+" from the health service");
                        //finalResponse[0] = response;
                        redisTemplate.opsForValue().set(String.valueOf(cookie1), response1);
                    },
                    error1 ->
                    {
                        // ROLLBACK + FAILURE MESSAGE UPDATION IN CACHE
                        log.info("error processing the response "+error1);
                    });

            // SENDING ANOTHER PARALLEL REQUEST TO OBSERVABLE-SERVICE
            Mono<String> responseMono_2 = webClient_obs.get()
                    .retrieve()
                    .bodyToMono(String.class); // ASYNCHRONOUS

            responseMono_2.subscribe(
                    response2 -> {
                        log.info(response2+" from the observable service");
                        //finalResponse[0] = response2;
                    },
                    error2 ->
                    {
                        log.info("error processing the response "+error2);
                    });

            servletResponse.addCookie(cookie1);
            return  ResponseEntity.ok().body("Social-Service-STEP-1-COMPLETE");
        }
        else
        {
            // TO BE MODIFIED TO CHECK FOR COOKIE AND NOT HEADER
             String cookie = request.getHeader("health_status_cookie");
             String response = (String)redisTemplate.opsForValue().get(cookie);
             if(response == null)
             {
                 return ResponseEntity.notFound().build();
             }
             else
             {
                 return ResponseEntity.ok().body(response);
             }
            //return ResponseEntity.ok().body("Social-Service-STEP-2-IN-PROGRESS");
        }
    }

    @GetMapping("/get/all/citizen/ration/details")
    public ResponseEntity<String> getAllCitizenRationDetails(HttpServletRequest request)
    {
        // Forward the Request to the Ration-Service
        log.info("Request received from POSTMAN for the RATION-DETAILS ENDPOINT");

        //STEP 2: ASYNC REQUEST TO HEALTH-SERVICE
        Mono<String> responseMono = webClientRationService_1.get()
                .retrieve()
                .bodyToMono(String.class); // ASYNCHRONOUS - IF .BLOCK() IS NOT USED | SYNCHRONOUS - THREAD WILL BE BLOCKED AT THIS POINT IF .BLOCK()

        log.info("Request forwarded to the RATION-SERVICE");


        //STEP 3: COOKIE-GENERATION + INTERIM RESPONSE
        //String cookie =  String.valueOf((int)(Math.random()*1000000));

        //String requestid = requestIdExtractor.getRequestId(request);
        //Cookie cookie1 = new Cookie("ss-1", "ss-1-"+requestid);
        //cookie1.setMaxAge(3600);

        responseMono.subscribe(
                // ASYNC RESPONSE HANDLER
                response1 -> { // SUCCESS HANDLER
                    log.info(response1+" from the ration service");
                    //finalResponse[0] = response;
                   // redisTemplate.opsForValue().set(String.valueOf(cookie1), response1);
                },
                error1 ->
                { // ERROR HANDLER
                    // ROLLBACK + FAILURE MESSAGE UPDATION IN CACHE
                    log.info("error processing the response from ration service"+error1);
                }

                );

        log.info("Async Request Handlers for handling response appended");

        return ResponseEntity.ok("social service has forwarded the response to the ration-service");
    }







}
