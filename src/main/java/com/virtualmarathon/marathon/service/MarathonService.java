package com.virtualmarathon.marathon.service;

import com.virtualmarathon.marathon.Constants;
import com.virtualmarathon.marathon.customerror.MarathonClassException;
import com.virtualmarathon.marathon.entity.Lap;
import com.virtualmarathon.marathon.entity.Marathon;
import com.virtualmarathon.marathon.entity.User;
import com.virtualmarathon.marathon.repository.LapRepository;
import com.virtualmarathon.marathon.repository.MarathonRepository;
import com.virtualmarathon.marathon.repository.UserRepository;

import java.math.BigDecimal;

import java.time.format.DateTimeFormatter;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MarathonService {
    Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MarathonRepository marathonRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LapRepository lapRepository;

    @Autowired
    StravaService stravaService;

    public Marathon createMarathon(String email, Marathon marathon){
        if(userService.getValidSubmissionCount(email)>=10){
            if(marathon.getName()==null || marathon.getDistance()==null ||
                    marathon.getStartTime()==null || marathon.getEndTime()==null){
                throw new MarathonClassException("Necessary details not provided", HttpStatus.UNPROCESSABLE_ENTITY);
            }else if((Duration.between(Constants.getCurrentTime(),marathon.getStartTime()).toDays()<1 && !email.equals("Admin")) ||
                    marathon.getStartTime().isBefore(Constants.getCurrentTime()) ||
                    marathon.getEndTime().isBefore(marathon.getStartTime()) ||
                    marathon.getEndTime().isEqual(marathon.getStartTime()) ||
                    marathon.getMaxParticipantCount()<=0){
                throw new MarathonClassException("Incorrect marathon data given", HttpStatus.UNPROCESSABLE_ENTITY);
            }else{
                Marathon newMarathon;
                if(marathon.isPrivate()){
                    HashSet<String> splitParticipantList = new HashSet<>(List.of(marathon.getPrivateParticipantSet().split(",")));
                    marathon.setPrivateParticipantSet(String.join(",",splitParticipantList));

                    newMarathon = new Marathon(marathon.getName(),marathon.getOrganizer(),marathon.getDistance(),marathon.getDescription(),marathon.getStartTime(),marathon.getEndTime(),marathon.getMaxParticipantCount(),marathon.getPrivateParticipantSet());
                }
                else{
                    newMarathon = new Marathon(marathon.getName(),marathon.getOrganizer(),marathon.getDistance(),marathon.getDescription(),marathon.getStartTime(),marathon.getEndTime(),marathon.getMaxParticipantCount());
                }
                newMarathon.setOrganizer(userRepository.findUserByEmail(email));
                newMarathon.setId(marathon.getId());
                LOGGER.info(newMarathon.toString());
                if(userService.updateMarathonsOrganized(false,newMarathon)){
                    return newMarathon;
                }else{
                    throw new MarathonClassException("Exceeds the limit for organizing marathons",HttpStatus.BAD_REQUEST);
                }

            }
        }else{
            throw new MarathonClassException("User not eligible to organize marathon",HttpStatus.PRECONDITION_FAILED);
        }
    }

    public Lap joinMarathon(String email, Long marathonId){
        if (userRepository.findUserByEmail(email)!=null && marathonRepository.findMarathonById(marathonId)!=null){
            Marathon marathon=marathonRepository.findMarathonById(marathonId);
            User user=userRepository.findUserByEmail(email);
            if(marathon.isPrivate()) {
                Set participantSet = Stream.of(marathon.getPrivateParticipantSet().trim().split("\\s*,\\s*")).collect(Collectors.toSet());
                if (!participantSet.contains(email)) {
                    throw new MarathonClassException("Joining this marathon is not allowed", HttpStatus.BAD_REQUEST);
                }
            }

            if(lapRepository.findLapByUserAndMarathon(email, marathonId) == null &&
                    Constants.getCurrentTime().isBefore(marathon.getEndTime()) &&
                    marathon.getCurrentParticipantCount() < marathon.getMaxParticipantCount()){
                Lap lap=new Lap(user,marathon,Constants.getCurrentTime());
                lap.setMarathon(marathon);
                lap.setAthlete(user);
                lap.setDistance(null);
                lap.setLapCompletedInSeconds(null);
                marathon.addParticipantSubmission(lap);
                marathon.setCurrentParticipantCount(marathon.getCurrentParticipantCount()+1);
                lapRepository.addLap(lap);
                marathonRepository.updateMarathon(marathon);
                userService.addLap(email,lap);

                return lap;
            }else {
                if(lapRepository.findLapByUserAndMarathon(email, marathonId) != null)
                    throw new MarathonClassException("User has already joined this marathon",HttpStatus.BAD_REQUEST);
                else if(Constants.getCurrentTime().isAfter(marathon.getEndTime())){
                    throw new MarathonClassException("Join period of marathon has ended",HttpStatus.BAD_REQUEST);
                }else if(marathon.getCurrentParticipantCount() < marathon.getMaxParticipantCount()){
                    throw new MarathonClassException("Max participant count reached",HttpStatus.BAD_REQUEST);
                }else{
                    throw new MarathonClassException("Joining this marathon is not allowed",HttpStatus.BAD_REQUEST);
                }


            }
        }else{
            throw new MarathonClassException("Invalid marathon id",HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public Lap getLap(Lap lap){
        if(lap.getMarathon()!=null && lap.getAthlete()!=null){
            LOGGER.info("marathon_id={} email={}",lap.getMarathon().getId(),lap.getAthlete().getEmail());
            return lapRepository.findLapByUserAndMarathon(lap.getAthlete().getEmail(),lap.getMarathon().getId());
        }
        else {
            throw new MarathonClassException("You have not joined this marathon",HttpStatus.BAD_REQUEST);
        }
    }

    public Lap getLap(String email, Long marathonId){
        return lapRepository.findLapByUserAndMarathon(email,marathonId);
    }

    public boolean isVirtualMarathonAuthorized(String email){
        return stravaService.isVirtualMarathonAuthorized(email);
    }

    public String authorizeVirtualMarathon(String email, String userCode){
        LOGGER.info("Call to strava service started");
        return stravaService.authorizeVirtualMarathon(email, userCode,"");
    }

    public Lap marathonSubmission(Lap lap, String email, long activityId){
        Lap oldLap=getLap(lap);

        Map<String,Object> activityInfo=stravaService.getLapDetails(email,activityId);
        lap.setDistance(new BigDecimal(activityInfo.get("distance").toString()));
        lap.setLapCompletedInSeconds(Long.parseLong(activityInfo.get("lapCompletedInSeconds").toString()));
        LocalDateTime activityCreationTime=LocalDateTime.parse(activityInfo.get("activityStartDate").toString(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        LOGGER.info("Distance:{},time:{},date:{}",lap.getDistance(),lap.getLapCompletedInSeconds(), activityCreationTime);

        if(oldLap!=null &&
                oldLap.getAthlete().getEmail().equals(email) &&
                oldLap.getLapCompletedInSeconds() == null &&
                oldLap.getDistance() == null &&
                lap.getDistance() != null &&
                lap.getDistance().compareTo(oldLap.getMarathon().getDistance()) > -1 &&
                lap.getLapCompletedInSeconds() != null &&
                lap.getLapCompletedInSeconds() > 0 &&
                Constants.getCurrentTime().isAfter(oldLap.getMarathon().getStartTime()) &&
                Constants.getCurrentTime().isBefore(oldLap.getMarathon().getEndTime()) &&
                activityCreationTime.isAfter(oldLap.getMarathon().getStartTime())
        ){
            oldLap.setDistance(lap.getDistance());
            oldLap.setLapCompletedInSeconds(lap.getLapCompletedInSeconds());
            LOGGER.info(oldLap.toString());
            userService.updateMarathonCompleted(lap.getAthlete().getEmail(),"");
            return lapRepository.updateLap(oldLap);
        }
        else{
            throw new MarathonClassException("lap details incorrect",HttpStatus.BAD_REQUEST);
        }
    }

    public Page<Lap> getResultPage(Long marathonId, int pageNumber, int pageLength) {
        Marathon marathon = marathonRepository.findMarathonById(marathonId);
        if (marathon == null || marathon.getEndTime().isAfter(Constants.getCurrentTime()))
            return null;
        if (pageLength<1 || pageLength > 100 || pageNumber<0 || pageNumber > marathon.getCurrentParticipantCount() / pageLength) {
            throw new MarathonClassException("Page cannot be rendered", HttpStatus.NOT_ACCEPTABLE);
        }
        if (!marathon.isResultDeclared()) {
            generateResult(marathonId);
        }
        return lapRepository.findPaginatedLapsByUserAndMarathon(marathonId,pageNumber,pageLength);

    }

    public List<Lap> generateResult(Long marathonId){
        Marathon marathon;
        if(marathonRepository.findMarathonById(marathonId)==null){
            throw new MarathonClassException("Marathon id doesn't exist",HttpStatus.BAD_REQUEST);
        }else {
            marathon=marathonRepository.findMarathonById(marathonId);
        }
        if (marathon.getEndTime().isAfter(Constants.getCurrentTime()))
            return null;
        List<Lap> lapList=marathon.getParticipantSubmissions();
        Collections.sort(lapList);
        if (!marathon.isResultDeclared()) {
            marathon.setResultDeclared(true);
            long lastSubmission = 0L, nextRank = 1L;
            for (int i=0;i<lapList.size();i++) {
                Lap lap=lapList.get(i);
                if (Objects.equals(lap.getLapCompletedInSeconds()==null?Long.MAX_VALUE:lap.getLapCompletedInSeconds(), lastSubmission))
                    lap.setLeaderboardPosition(nextRank - 1);
                else {
                    lap.setLeaderboardPosition(nextRank);
                    nextRank++;
                    lastSubmission = lap.getLapCompletedInSeconds()==null?Long.MAX_VALUE:lap.getLapCompletedInSeconds();
                }
            }
            nextRank--;
            for (Lap lap : lapList) {
                if (lap.getLapCompletedInSeconds() == null) {
                    lap.setPoints(0L);
                } else if (lap.getLeaderboardPosition() <= (int) ((1D / 100) * nextRank)) {
                    lap.setPoints(5L);
                } else if (lap.getLeaderboardPosition() <= (int) ((10D / 100) * nextRank)) {
                    lap.setPoints(4L);
                } else if (lap.getLeaderboardPosition() <= (int) ((25D / 100) * nextRank)) {
                    lap.setPoints(3L);
                } else if (lap.getLeaderboardPosition() <= (int) ((50D / 100) * nextRank)) {
                    lap.setPoints(2L);
                } else {
                    lap.setPoints(1L);
                }
                lapRepository.updateLap(lap);
            }
            userService.updateUserPoints(lapList);
            marathonRepository.updateMarathon(marathon);
        }
        return lapList;
    }
}
