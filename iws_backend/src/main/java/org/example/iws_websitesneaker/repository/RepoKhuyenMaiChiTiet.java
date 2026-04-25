package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.KhuyenMaiChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RepoKhuyenMaiChiTiet extends JpaRepository<KhuyenMaiChiTiet, Integer> {
    // Method Ä‘á»ƒ tÃ¬m chi tiáº¿t khuyáº¿n mÃ£i theo ID khuyáº¿n mÃ£i
    List<KhuyenMaiChiTiet> findByKhuyenMaiId(Integer khuyenMaiId);

    // Hoáº·c sá»­ dá»¥ng query tÃ¹y chá»‰nh
    @Query("SELECT k FROM KhuyenMaiChiTiet k WHERE k.khuyenMai.id = :khuyenMaiId")
    List<KhuyenMaiChiTiet> findByKhuyenMaiIdCustom(@Param("khuyenMaiId") Integer khuyenMaiId);

    // XÃ³a Khuyáº¿n mÃ£i - sá»­a id khuyÃªn mÃ£i trong báº£ng ctsp vá» null
    @Modifying
    @Transactional
    @Query("UPDATE KhuyenMaiChiTiet kmct SET kmct.khuyenMai = NULL WHERE kmct.khuyenMai.id = :id")
    void removeKhuyenMaiReference(@Param("id") int id);
}
