package org.example.iws_websitesneaker.repository;

import org.example.iws_websitesneaker.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoSanPham extends JpaRepository<SanPham, Integer>, JpaSpecificationExecutor<SanPham> {
    @Modifying
    @Transactional
    @Query("UPDATE SanPham sp SET sp.thuongHieu = NULL WHERE sp.thuongHieu.id = :id")
    void removeThuongHieuReference(@Param("id") int id);
    // Xóa  danh mục - - sửa id danh mục trong bảng ctsp về null
    @Modifying
    @Transactional
    @Query("UPDATE SanPham sp SET sp.danhMuc = NULL WHERE sp.danhMuc.id = :id")
    void removeDanhMucReference(@Param("id") int id);

    // Xóa Chất liêu - - sửa id cl trong bảng ctsp về null
    @Modifying
    @Transactional
    @Query("UPDATE SanPham sp SET sp.chatLieu = NULL WHERE sp.chatLieu.id = :id")
    void removeChatLieuReference(@Param("id") int id);

    // Xóa Đế giày - - sửa id de giày trong bảng ctsp về null
    @Modifying
    @Transactional
    @Query("UPDATE SanPham sp SET sp.deGiay = NULL WHERE sp.deGiay.id = :id")
    void removeDeGiayReference(@Param("id") int id);
}
