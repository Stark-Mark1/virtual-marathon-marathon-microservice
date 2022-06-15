package com.virtualmarathon.marathon.service;

import com.virtualmarathon.marathon.entity.Lap;
import com.virtualmarathon.marathon.entity.Marathon;
import com.virtualmarathon.marathon.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="USER-SERVICE/user/",url = "https://marathon-user-service-urtjok3rza-wl.a.run.app/user/")
public interface UserService {
    @GetMapping("get-valid-submissions-count")
    Long getValidSubmissionCount(@RequestHeader("username") String username);

    @GetMapping("get-user")
    User getUser(@RequestHeader("username") String email);

    @PutMapping("/new-submission/{username}")
    public User addLap(@PathVariable("username") String email,@RequestBody Lap lap);

    @PutMapping("/update-marathon-completed/{username}")
    public User updateMarathonCompleted(@PathVariable("username") String email, @RequestBody String emptyBody);

    @PutMapping("/update-user-points")
    public void updateUserPoints(@RequestBody List<Lap> lapList);

    @PutMapping("/update-organizer-count/{is-complete}")
    public boolean updateMarathonsOrganized(@PathVariable("is-complete") boolean isComplete,@RequestBody Marathon marathon);
}
