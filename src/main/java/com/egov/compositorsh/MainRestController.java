package com.egov.compositorsh;

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
import java.util.UUID;

@Tag(name = "Compositorsh Service", description = "Compositorsh Service APIs")
@RestController
@RequestMapping("/api/v1")
public class MainRestController {

    Logger logger = LoggerFactory.getLogger(MainRestController.class);

    @Autowired
    @Qualifier("webClient")
    WebClient webClient_ss;

    @Autowired
    @Qualifier("webClient_2")
    WebClient webClient_hs;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @GetMapping("/citizen/btype/social/events/{citizenid}")
    public ResponseEntity<String> getCitizenBtypeSocialEvents(@PathVariable("citizenid") UUID citizenid,HttpServletRequest request, HttpServletResponse servletResponse)
    {

        //STEP 0A: EXTRACT THE COOKIES FROM THE INCOMING REQUEST
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

        //STEP 0B: CREATE A TEMPORARY COOKIE
        Cookie cookie1 = new Cookie("sh-1", citizenid.toString());
        cookie1.setMaxAge(60);

        if( redisTemplate.opsForValue().get(cookie1.getName()+citizenid) == null )
        {
            //STEP 0C: LOOK FOR THE APPROPRIATE COOKIES
            if( cookieList.stream().filter(cookie -> cookie.getName().equals("sh-1")).findAny().isEmpty()) // COOKIE_CHECK
            {
                Citizen citizen = new Citizen();
                citizen.setCitizenid(citizenid);



                // STEP 1A: GET DATA FROM SOCIAL-SERVICE
                logger.info("Request received from POSTMAN for the blood-type | social events data");

                Mono<Socialevent> responseMonoSS = webClient_ss.post().body(Mono.just(citizen), Citizen.class)
                        .retrieve()
                        .bodyToMono(Socialevent.class); // ASYNCHRONOUS - IF .BLOCK() IS NOT USED | SYNCHRONOUS - THREAD WILL BE BLOCKED AT THIS POINT IF .BLOCK()

                logger.info("Request forwarded to the SOCIAL-SERVICE");

                // STEP 1B: CODING THE HANDLER FOR THE RESPONSE FROM SOCIAL-SERVICE
                responseMonoSS.subscribe(
                        responsess -> {
                            logger.info(responsess+" from the social service");
                            //citizenBtypeSocialView.setSocialevent(responsess);
                            redisTemplate.opsForValue().set(String.valueOf(cookie1.getName()+citizenid+"-social-service"), responsess);
                        },
                        error ->
                        {
                            logger.info("error processing the response "+error);
                        });

                // STEP 2A: GET DATA FROM HEALTH-SERVICE
                Mono<Btypedetail> responseMonoHS = webClient_hs.post().body(Mono.just(citizen), Citizen.class)
                        .retrieve()
                        .bodyToMono(Btypedetail.class); // ASYNCHRONOUS - IF .BLOCK() IS NOT USED | SYNCHRONOUS - THREAD WILL BE BLOCKED AT THIS POINT IF .BLOCK()

                logger.info("Request forwarded to the HEALTH-SERVICE");

                // STEP 2B: CODING THE HANDLER FOR THE RESPONSE FROM HEALTH-SERVICE
                responseMonoHS.subscribe(
                        responsehs -> {
                            logger.info(responsehs+" from the health service");
                            //citizenBtypeSocialView.setBtype(responsehs.getBtype());
                            redisTemplate.opsForValue().set(String.valueOf(cookie1.getName()+citizenid+"-health-service"), responsehs);

                        },
                        error ->
                        {
                            logger.info("error processing the response "+error);
                        });


                // STEP 3: SEND BACK AN INTERIM RESPONSE | WITH A COOKIE
                servletResponse.addCookie(cookie1);
                return ResponseEntity.ok("Blood Type | Social Events Query is being processed");
            }
            else
            {
                // CHECK WHETHER THE RESPONSES HAVE COME BACK FROM THE OTHER SERVICE
                String cacheKeySS = String.valueOf(cookie1.getName()+citizenid+"-social-service");
                String cacheKeyHS = String.valueOf(cookie1.getName()+citizenid+"-health-service");
                Socialevent cacheValueSS =  (Socialevent) redisTemplate.opsForValue().get(cacheKeySS);
                Btypedetail cacheValueHS =  (Btypedetail) redisTemplate.opsForValue().get(cacheKeyHS);

                if(redisTemplate.opsForValue().get(cacheKeySS) != null)
                {
                    if(redisTemplate.opsForValue().get(cacheKeyHS) != null)
                    {
                        CitizenBtypeSocialView citizenBtypeSocialView = new CitizenBtypeSocialView();
                        citizenBtypeSocialView.setCitizenid(citizenid);
                        citizenBtypeSocialView.setBtype(cacheValueHS.getBtype());
                        citizenBtypeSocialView.setSocialevent(cacheValueSS);
                        logger.info(citizenBtypeSocialView.toString());

                        redisTemplate.opsForValue().set(String.valueOf(cookie1.getName()+citizenid), citizenBtypeSocialView);

                        return ResponseEntity.ok(citizenBtypeSocialView.toString());
                    }
                    else
                    {
                        return ResponseEntity.ok("Blood Type | Social Events Query is still being processed");
                    }

                }
                else
                {
                    return ResponseEntity.ok("Blood Type | Social Events Query is still being processed");
                }


            }
        }
        else
        {
            return ResponseEntity.ok(redisTemplate.opsForValue().get(cookie1.getName()+citizenid).toString());
        }



    }


}
