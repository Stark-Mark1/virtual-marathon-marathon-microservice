package com.virtualmarathon.marathon.controller;

import com.sun.istack.NotNull;
import com.virtualmarathon.marathon.entity.Lap;
import com.virtualmarathon.marathon.entity.Marathon;
import com.virtualmarathon.marathon.repository.MarathonRepository;
import com.virtualmarathon.marathon.service.MarathonService;
import com.virtualmarathon.marathon.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/marathon")
public class MarathonController {

    @Autowired
    MarathonService marathonService;

    @Autowired
    UserService userService;

    @Autowired
    MarathonRepository marathonRepository;

    Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/get-all-marathons")
    public List<Marathon> getAllMarathon(){
        return marathonRepository.getAll();
    }

    @GetMapping("/get-all-current-marathons")
    public List<Marathon> getAllCurrentMarathons(){
        return marathonRepository.getAllCurrentMarathons();
    }

    @PostMapping("/register-marathon")
    public Marathon addMarathon(@RequestHeader("username") String email, @RequestBody Marathon marathon){
        marathon.setId(null);
        return marathonRepository.addMarathon(marathonService.createMarathon(email,marathon));
    }

    @PutMapping("/update-marathon")
    public Marathon updateMarathon(@RequestHeader("username") String email, @RequestBody Marathon marathon){
        return marathonRepository.updateMarathon(marathonService.createMarathon(email,marathon));
    }

    @GetMapping("/search")
    public List<Marathon> getAllMarathonsInBetween(@RequestParam("startDate") @DateTimeFormat(pattern = "dd-MM-yyyy") @Nullable Date startDate, @RequestParam("endDate") @DateTimeFormat(pattern = "dd-MM-yyyy") @Nullable Date endDate, @RequestParam("name") @Nullable String name){
        System.out.println("here");
        return marathonRepository.findMarathons(startDate,endDate,name);
    }

    @GetMapping("/marathon-details/{marathon-id}")
    public  Marathon getMarathonDetails(@PathVariable("marathon-id") Long marathonId){
        return marathonRepository.findMarathonById(marathonId);
    }

    @PostMapping("/join-marathon")
    public Lap joinMarathon(@RequestHeader("username") String email, @RequestParam("marathonId") Long marathonId){
        return marathonService.joinMarathon(email,marathonId);
    }

    @PutMapping("/marathon-submission/{activityId}")
    public Lap marathonSubmission(@PathVariable("activityId") long activityId, @RequestBody Lap lap, @RequestHeader("username") String email){
        return marathonService.marathonSubmission(lap, email, activityId);
    }

    @GetMapping("/is-virtual-marathon-authorized")
    public boolean isStravaAuthorized(@RequestHeader("username") String email){
        return marathonService.isVirtualMarathonAuthorized(email);
    }

    @PostMapping("/authorize-virtual-marathon/")
    public String authorizeVirtualMarathon(@RequestHeader("username") String email,@RequestParam("userCode") String userCode){
        return marathonService.authorizeVirtualMarathon(email, userCode);
    }

    @GetMapping("/get-paginated-marathon-leaderboard")
    public Page<Lap> getMarathonLeaderBoardPage(@RequestParam Long marathonId, @RequestParam int pageNumber, @RequestParam int pageLength){
        return marathonService.getResultPage(marathonId,pageNumber,pageLength);
    }

    @GetMapping("/get-marathon-leaderboard")
    public List<Lap> getMarathonLeaderBoard(@RequestParam Long marathonId){
        return marathonService.generateResult(marathonId);
    }

    @GetMapping("/get-lap-details/")
    public Lap getLapDetails(@NotNull @RequestHeader("username") String email, @NotNull @RequestParam long marathonId){
        return marathonService.getLap(email,marathonId);
    }
}
