package org.example.iws_websitesneaker.specification;

import org.example.iws_websitesneaker.Dto.BanHang.SanPhamChiTietFilterRequest;
import org.example.iws_websitesneaker.entity.ChiTietSanPham;
import org.example.iws_websitesneaker.entity.SanPham;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietSanPhamSpecification {

    public static Specification<ChiTietSanPham> withFilter(SanPhamChiTietFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join với bảng sản phẩm
            Join<ChiTietSanPham, SanPham> sanPhamJoin = root.join("sanPham", JoinType.LEFT);

            // Filter theo keyword
            if (StringUtils.hasText(filter.getKeyword())) {
                String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                Predicate keywordPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(sanPhamJoin.get("tenSanPham")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("maChiTiet")), keyword)
                );
                predicates.add(keywordPredicate);
            }

            // Filter theo danh mục
            if (filter.getDanhMucId() != null) {
                predicates.add(criteriaBuilder.equal(sanPhamJoin.get("danhMuc").get("id"), filter.getDanhMucId()));
            }

            // Filter theo thương hiệu
            if (filter.getThuongHieuId() != null) {
                predicates.add(criteriaBuilder.equal(sanPhamJoin.get("thuongHieu").get("id"), filter.getThuongHieuId()));
            }

            // Filter theo màu sắc
            if (filter.getMauSacId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("mauSac").get("id"), filter.getMauSacId()));
            }

            // Filter theo kích cỡ
            if (filter.getKichCoId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("kichCo").get("id"), filter.getKichCoId()));
            }

            // Filter theo chất liệu
            if (filter.getChatLieuId() != null) {
                predicates.add(criteriaBuilder.equal(sanPhamJoin.get("chatLieu").get("id"), filter.getChatLieuId()));
            }

            // Filter theo đế giày
            if (filter.getDeGiayId() != null) {
                predicates.add(criteriaBuilder.equal(sanPhamJoin.get("deGiay").get("id"), filter.getDeGiayId()));
            }

            // Filter theo giá
            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("giaBan"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("giaBan"), filter.getMaxPrice()));
            }

            // Chỉ lấy sản phẩm active
            predicates.add(criteriaBuilder.equal(root.get("trangThai"), 1));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
