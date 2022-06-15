package com.virtualmarathon.marathon.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name="foo-client",url = "https://strava-service-urtjok3rza-wl.a.run.app/strava")
public interface StravaService {
    @GetMapping("/is-virtual-marathon-authorized")
    public boolean isVirtualMarathonAuthorized(@RequestParam String email);

    @PostMapping(value = "/authorize-virtual-marathon")
    public String authorizeVirtualMarathon(@RequestParam String email, @RequestParam String userCode, @RequestBody String emptyBody);

    @GetMapping("/get-lap-details")
    public Map<String,Object> getLapDetails(@RequestParam String email, @RequestParam long activityId);
}
