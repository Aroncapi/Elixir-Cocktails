package com.barpro.core.repository;

import com.barpro.core.entity.Client;
import com.barpro.core.entity.Request;
import com.barpro.core.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Optional<Request> findByPublicToken(String publicToken);

    List<Request> findByClientAndStatusOrderByEventDateDesc(Client client, RequestStatus status);

    @Query(value = """
            select r from Request r
            where (:status is null or r.status = :status)
              and (:needle is null
                   or lower(r.folio) like :needle
                   or lower(r.clientName) like :needle
                   or lower(r.clientEmail) like :needle
                   or lower(r.location) like :needle
                   or lower(r.eventType.name) like :needle)
            order by r.createdAt desc
            """, countQuery = """
            select count(r) from Request r
            where (:status is null or r.status = :status)
              and (:needle is null
                   or lower(r.folio) like :needle
                   or lower(r.clientName) like :needle
                   or lower(r.clientEmail) like :needle
                   or lower(r.location) like :needle
                   or lower(r.eventType.name) like :needle)
            """)
    Page<Request> search(@Param("status") RequestStatus status,
                         @Param("needle") String needle,
                         Pageable pageable);

    @Query("""
            select r from Request r
            where r.eventDate between :desde and :hasta
            order by r.eventDate asc, r.eventTime asc
            """)
    List<Request> findByEventDateBetween(@Param("desde") LocalDate desde,
                                         @Param("hasta") LocalDate hasta);

    long countByStatus(RequestStatus status);

    @Query("select coalesce(sum(r.totalAmount), 0) from Request r where r.status <> :excluido")
    BigDecimal sumTotalAmountExcluding(@Param("excluido") RequestStatus excluido);

    @Query("""
            select count(r) from Request r
            where r.status <> :excluido and r.eventDate between :desde and :hasta
            """)
    long countByStatusExcludingAndEventDateBetween(@Param("excluido") RequestStatus excluido,
                                                   @Param("desde") LocalDate desde,
                                                   @Param("hasta") LocalDate hasta);
}
