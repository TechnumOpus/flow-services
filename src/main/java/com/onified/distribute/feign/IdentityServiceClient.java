package com.onified.distribute.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * @author srini
 */
@FeignClient(name = "IDENTITY-SERVICE")
public interface IdentityServiceClient {

    @GetMapping("/api/v1/auth/extract-username")
    Map<String, String> extractUsername(@RequestHeader("Authorization") String token);


}
