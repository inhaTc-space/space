package com.space.sgg.repository;

import com.space.sgg.dto.SggResponseDto;
import com.space.sgg.entity.Sgg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SggRepository extends JpaRepository<Sgg, String>{
    /**
     * sido 구하는 메소드
     * @return
     */
    @Query("select u from Sgg u where u.sggCd = u.upprCd and u.sggNm != '전국'")
    List<Sgg> getSidoList();

    /**
     * sgg 구하는 메소드
     */
    @Query("select u from Sgg u where u.upprCd = :sido and u.sggCd != :sido")
    List<Sgg> getSggList(@Param("sido") String sido);

    /**
     * 회원 sgg 코드를 이용해여 sido코드 값 리턴하는 함수
     * @param memberSgg
     * @return sido에 대한 sgg코드
     */
    SggResponseDto findBySggCd(String memberSgg);
}
