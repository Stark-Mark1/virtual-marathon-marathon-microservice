package com.virtualmarathon.marathon.repository;

import com.virtualmarathon.marathon.Constants;
import com.virtualmarathon.marathon.entity.Marathon;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Repository
@Transactional
public class MarathonRepository {

    @PersistenceContext
    EntityManager entityManager;

    public Marathon findMarathonById(Long id){
        return entityManager.find(Marathon.class,id);
    }

    public List<Marathon> getAll(){
        return entityManager.createQuery("Select marathon from Marathon marathon",Marathon.class).getResultList();
    }

    public List<Marathon> getAllCurrentMarathons(){
        return entityManager.createQuery("Select marathon from Marathon marathon where marathon.endTime>=:currentTime and marathon.startTime<=:currentTime",Marathon.class)
                .setParameter("currentTime", Constants.getCurrentTime())
                .getResultList();
    }

    public Marathon addMarathon(Marathon marathon){
        entityManager.persist(marathon);
        return marathon;
    }

    public Marathon updateMarathon(Marathon marathon){
        if(findMarathonById(marathon.getId())!=null){
            entityManager.merge(marathon);
            return marathon;
        }
        return null;
    }

    public List<Marathon> findMarathons(Date startTime, Date endTime, String name){
        TypedQuery<Marathon> marathonTypedQuery=entityManager.createQuery("Select marathon from Marathon marathon where (:endTime is null or marathon.startTime<:endTime) and (:startTime is null or marathon.endTime>:startTime) and (:name is null or marathon.name like :name)",Marathon.class);
        marathonTypedQuery.setParameter("endTime",endTime==null?null:LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime.getTime()), ZoneId.systemDefault()));
        marathonTypedQuery.setParameter("startTime",startTime==null?null:LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime.getTime()), ZoneId.systemDefault()));
        marathonTypedQuery.setParameter("name",String.format("%%%s%%",name));
        return marathonTypedQuery.getResultList();
    }
}
