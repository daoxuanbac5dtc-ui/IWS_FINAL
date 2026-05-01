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
    // Method để tìm chi tiết khuyến mãi theo ID khuyến mãi
    List<KhuyenMaiChiTiet> findByKhuyenMaiId(Integer khuyenMaiId);

    // Hoặc sử dụng query tùy chỉnh
    @Query("SELECT k FROM KhuyenMaiChiTiet k WHERE k.khuyenMai.id = :khuyenMaiId")
    List<KhuyenMaiChiTiet> findByKhuyenMaiIdCustom(@Param("khuyenMaiId") Integer khuyenMaiId);

    // Xóa Khuyến mãi - sửa id khuyên mãi trong bảng ctsp về null
    @Modifying
    @Transactional
    @Query("UPDATE KhuyenMaiChiTiet kmct SET kmct.khuyenMai = NULL WHERE kmct.khuyenMai.id = :id")
    void removeKhuyenMaiReference(@Param("id") int id);
}
