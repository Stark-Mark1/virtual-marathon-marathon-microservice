package com.virtualmarathon.marathon.repository;

import com.virtualmarathon.marathon.entity.Lap;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class LapRepository {

    @PersistenceContext
    EntityManager entityManager;

    public Lap addLap(Lap lap){
        entityManager.persist(lap);
        return lap;
    }

    public Lap findLapByUserAndMarathon(String email, Long marathonId){
        System.out.println(email);
        TypedQuery<Lap> query=entityManager.createQuery("Select lap from Lap lap where lap.marathon.id = :marathonId and lap.athlete.email = :email order by lap.leaderboardPosition",Lap.class)
                .setParameter("email",email)
                .setParameter("marathonId",marathonId);
        if(query.getResultList().size()>0){
            System.out.println(query.getResultList().get(0));
            return query.getResultList().get(0);
        }
        return null;
    }

    public Page<Lap> findPaginatedLapsByUserAndMarathon(Long marathonId, int pageNumber, int pageLength){
        Pageable pageable= PageRequest.of(pageNumber,pageLength,Sort.by("leaderboardPosition"));
        List<Lap> allLaps= getMarathonSubmissions(marathonId);
        List<Lap> lapPage= allLaps.subList((int)pageable.getOffset(),
                Math.min((int)pageable.getOffset()+pageable.getPageSize(), allLaps.size()));
        return new PageImpl<Lap>(lapPage, pageable, allLaps.size());
    }

    public List<Lap> getMarathonSubmissions(Long marathonId){
        TypedQuery<Lap> query=entityManager.createQuery("Select lap from Lap lap where lap.marathon.id = :marathonId",Lap.class)
                .setParameter("marathonId",marathonId);
        if(query.getResultList().size()>0){
            return query.getResultList();
        }
        return null;
    }

    public Lap updateLap(Lap lap){
        return entityManager.merge(lap);
    }
}
