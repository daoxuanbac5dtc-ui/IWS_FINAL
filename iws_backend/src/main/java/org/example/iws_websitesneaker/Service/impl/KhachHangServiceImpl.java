package org.example.iws_websitesneaker.Service.impl;

import org.example.iws_websitesneaker.Dto.KhachHangDto;
import org.example.iws_websitesneaker.Service.KhachHangService;
import org.example.iws_websitesneaker.Service.DiaChiService;
import org.example.iws_websitesneaker.entity.KhachHang;
import org.example.iws_websitesneaker.entity.DiaChi;
import org.example.iws_websitesneaker.repository.RepoKhachHang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Triá»ƒn khai service quáº£n lÃ½ KhÃ¡ch hÃ ng - ÄÃƒ Sá»¬A Lá»–I LazyInitializationException
 * Cung cáº¥p cÃ¡c chá»©c nÄƒng CRUD vÃ  tÃ¬m kiáº¿m cho khÃ¡ch hÃ ng
 */
@Service
@Transactional
public class KhachHangServiceImpl implements KhachHangService {

    @Autowired
    private RepoKhachHang repoKhachHang;

    @Autowired
    private DiaChiService diaChiService;

    // ================== CÃC THAO TÃC CRUD CÆ  Báº¢N ==================

    @Override
    @Transactional(readOnly = true)
    public List<KhachHang> getAllKhachHang() {
        try {
            List<KhachHang> result = repoKhachHang.findAll();
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error getting all customers: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhachHang> getAllWithCompleteInfo() {
        try {
            return repoKhachHang.findAllWithTaiKhoan();
        } catch (Exception e) {
            System.err.println("Error getting all customers with complete info: " + e.getMessage());
            throw new RuntimeException("Lá»—i khi láº¥y danh sÃ¡ch khÃ¡ch hÃ ng: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<KhachHang> getKhachHangById(Integer id) {
        try {
            if (id == null || id <= 0) {
                return Optional.empty();
            }
            return repoKhachHang.findById(id);
        } catch (Exception e) {
            System.err.println("Error getting customer by ID " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<KhachHang> findByIdWithEagerLoading(Integer id) {
        try {
            if (id == null || id <= 0) {
                return Optional.empty();
            }
            return repoKhachHang.findByIdWithTaiKhoan(id);
        } catch (Exception e) {
            System.err.println("Error getting customer by ID with eager loading " + id + ": " + e.getMessage());
            throw new RuntimeException("Lá»—i khi láº¥y thÃ´ng tin khÃ¡ch hÃ ng: " + e.getMessage(), e);
        }
    }

    @Override
    public void addKhachHang(KhachHang khachHang) {
        try {
            if (khachHang == null) {
                throw new IllegalArgumentException("ThÃ´ng tin khÃ¡ch hÃ ng khÃ´ng Ä‘Æ°á»£c null");
            }

            // Thiáº¿t láº­p ngÃ y táº¡o náº¿u chÆ°a cÃ³
            if (khachHang.getNgayTao() == null) {
                khachHang.setNgayTao(new Date());
            }
            khachHang.setNgayCapNhat(new Date());

            // Táº¡o mÃ£ khÃ¡ch hÃ ng náº¿u chÆ°a cÃ³
            if (khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().isEmpty()) {
                khachHang.setMaKhachHang(generateMaKhachHang());
            }

            // Thiáº¿t láº­p tráº¡ng thÃ¡i máº·c Ä‘á»‹nh
            if (khachHang.getTrangThai() == null) {
                khachHang.setTrangThai(1);
            }

            // Kiá»ƒm tra trÃ¹ng láº·p
            if (existsBySdt(khachHang.getSdt())) {
                throw new IllegalArgumentException("Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ tá»“n táº¡i: " + khachHang.getSdt());
            }

            validateKhachHangData(khachHang);
            repoKhachHang.save(khachHang);
            System.out.println("Created customer: " + khachHang.getHoTen() + " (ID: " + khachHang.getId() + ")");

        } catch (Exception e) {
            System.err.println("Error adding customer: " + e.getMessage());
            throw new RuntimeException("Lá»—i khi thÃªm khÃ¡ch hÃ ng: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateKhachHang(KhachHang khachHang) {
        try {
            if (khachHang == null || khachHang.getId() == null) {
                throw new IllegalArgumentException("ThÃ´ng tin khÃ¡ch hÃ ng hoáº·c ID khÃ´ng Ä‘Æ°á»£c null");
            }

            Optional<KhachHang> existing = getKhachHangById(khachHang.getId());
            if (existing.isEmpty()) {
                throw new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng vá»›i ID: " + khachHang.getId());
            }

            validateKhachHangData(khachHang);

            KhachHang existingKH = existing.get();
            if (!khachHang.getSdt().equals(existingKH.getSdt())) {
                if (existsBySdt(khachHang.getSdt())) {
                    throw new IllegalArgumentException("Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng bá»Ÿi khÃ¡ch hÃ ng khÃ¡c");
                }
            }

            khachHang.setNgayTao(existingKH.getNgayTao());
            if (khachHang.getTaiKhoan() == null) {
                khachHang.setTaiKhoan(existingKH.getTaiKhoan());
            }
            khachHang.setNgayCapNhat(new Date());

            repoKhachHang.save(khachHang);
            System.out.println("Updated customer: " + khachHang.getHoTen() + " (ID: " + khachHang.getId() + ")");

        } catch (Exception e) {
            System.err.println("Error updating customer: " + e.getMessage());
            throw new RuntimeException("Lá»—i khi cáº­p nháº­t khÃ¡ch hÃ ng: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteKhachHang(Integer id) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID khÃ¡ch hÃ ng khÃ´ng há»£p lá»‡");
            }

            Optional<KhachHang> khachHang = getKhachHangById(id);
            if (khachHang.isEmpty()) {
                throw new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng vá»›i ID: " + id);
            }

            KhachHang kh = khachHang.get();
            if (!canDeleteKhachHang(id)) {
                throw new IllegalStateException("KhÃ´ng thá»ƒ xÃ³a khÃ¡ch hÃ ng nÃ y do cÃ²n dá»¯ liá»‡u liÃªn quan");
            }

            // Soft delete
            kh.setTrangThai(0);
            kh.setNgayCapNhat(new Date());
            repoKhachHang.save(kh);

            System.out.println("Soft deleted customer: " + kh.getHoTen() + " (ID: " + id + ")");

        } catch (Exception e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            throw new RuntimeException("Lá»—i khi xÃ³a khÃ¡ch hÃ ng: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public KhachHang findByTaiKhoanId(Integer taiKhoanId) {
        try {
            System.out.println("ðŸ” DEBUG KhachHangService - TÃ¬m khÃ¡ch hÃ ng theo TaiKhoan ID: " + taiKhoanId);

            // TÃ¬m theo tai_khoan_id trong báº£ng khach_hang
            Optional<KhachHang> result = repoKhachHang.findByTaiKhoanId(taiKhoanId);

            if (result.isPresent()) {
                KhachHang khachHang = result.get();
                System.out.println("âœ… DEBUG KhachHangService - TÃ¬m tháº¥y: " +
                        "KH.ID=" + khachHang.getId() +
                        ", TÃªn=" + khachHang.getHoTen() +
                        ", TaiKhoan.ID=" + (khachHang.getTaiKhoan() != null ? khachHang.getTaiKhoan().getId() : "null"));
                return khachHang;
            } else {
                System.out.println("âŒ DEBUG KhachHangService - KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng cho TaiKhoan ID: " + taiKhoanId);
                return null;
            }
        } catch (Exception e) {
            System.out.println("ðŸ’¥ DEBUG KhachHangService - Lá»—i: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional
    public void deleteByTaiKhoanId(Integer taiKhoanId) {
        try {
            if (taiKhoanId == null || taiKhoanId <= 0) {
                System.out.println("Invalid taiKhoanId: " + taiKhoanId);
                return;
            }

            System.out.println("Deleting customer with taiKhoanId: " + taiKhoanId);
            Optional<KhachHang> khachHang = findByTaiKhoanIdOptional(taiKhoanId);
            if (khachHang.isPresent()) {
                KhachHang kh = khachHang.get();
                System.out.println("Found customer: " + kh.getHoTen() + " (ID: " + kh.getId() + ")");
                repoKhachHang.deleteById(kh.getId());
                System.out.println("Customer hard deleted successfully");
            } else {
                System.out.println("No customer found with taiKhoanId: " + taiKhoanId);
            }
        } catch (Exception e) {
            System.err.println("Error deleting customer by taiKhoanId: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("KhÃ´ng thá»ƒ xÃ³a khÃ¡ch hÃ ng: " + e.getMessage(), e);
        }
    }

    // ================== CÃC PHÆ¯Æ NG THá»¨C TÃŒM KIáº¾M ==================

    @Override
    @Transactional(readOnly = true)
    public Optional<KhachHang> findByTaiKhoanIdOptional(Integer taiKhoanId) {
        try {
            if (taiKhoanId == null || taiKhoanId <= 0) {
                return Optional.empty();
            }
            return repoKhachHang.findByTaiKhoan_Id(taiKhoanId);
        } catch (Exception e) {
            System.err.println("Error finding customer by taiKhoanId: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean existsBySdt(String sdt) {
        try {
            if (sdt == null || sdt.trim().isEmpty()) {
                return false;
            }
            return repoKhachHang.existsBySdt(sdt.trim());
        } catch (Exception e) {
            System.err.println("Error checking phone existence: " + e.getMessage());
            return false;
        }
    }

    @Override
    public KhachHang findByHoTenAndSdt(String hoTen, String sdt) {
        try {
            if (hoTen == null || sdt == null) {
                return null;
            }
            return repoKhachHang.findByHoTenAndSdt(hoTen.trim(), sdt.trim());
        } catch (Exception e) {
            System.err.println("Error finding customer by name and phone: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean existsByMaKhachHang(String maKhachHang) {
        try {
            if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
                return false;
            }
            return findByMaKhachHang(maKhachHang.trim()).isPresent();
        } catch (Exception e) {
            System.err.println("Error checking customer code existence: " + e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<KhachHang> findByMaKhachHang(String maKhachHang) {
        try {
            if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
                return Optional.empty();
            }
            return repoKhachHang.findByMaKhachHang(maKhachHang.trim());
        } catch (Exception e) {
            System.err.println("Error finding customer by code: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean isPhoneNumberUsed(String sdt, Integer excludeId) {
        try {
            if (sdt == null || sdt.trim().isEmpty()) {
                return false;
            }

            return getAllKhachHang().stream()
                    .anyMatch(kh -> {
                        boolean samePhone = sdt.equals(kh.getSdt());
                        boolean differentCustomer = excludeId == null || !excludeId.equals(kh.getId());
                        return samePhone && differentCustomer;
                    });
        } catch (Exception e) {
            System.err.println("Error checking phone number usage: " + e.getMessage());
            return false;
        }
    }

    // ================== CÃC PHÆ¯Æ NG THá»¨C TÃŒM KIáº¾M CÆ  Báº¢N ==================

    @Override
    @Transactional(readOnly = true)
    public List<KhachHang> searchByKeyword(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllWithCompleteInfo();
            }
            return repoKhachHang.searchWithTaiKhoan(keyword.trim(), null);
        } catch (Exception e) {
            System.err.println("Error searching customers by keyword: " + e.getMessage());
            return getAllWithCompleteInfo();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhachHang> searchAdvanced(String hoTen, String email, String sdt, String maKhachHang,
                                          Integer trangThai, String gioiTinh, Date startDate, Date endDate) {
        try {
            List<KhachHang> baseResults = repoKhachHang.searchAdvancedWithTaiKhoan(hoTen, email, sdt, maKhachHang, trangThai);

            return baseResults.stream()
                    .filter(kh -> {
                        // Filter theo khoáº£ng thá»i gian
                        if (startDate != null && kh.getNgayTao() != null && kh.getNgayTao().before(startDate)) {
                            return false;
                        }
                        if (endDate != null && kh.getNgayTao() != null && kh.getNgayTao().after(endDate)) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error in advanced search: " + e.getMessage());
            throw new RuntimeException("TÃ¬m kiáº¿m nÃ¢ng cao tháº¥t báº¡i: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhachHang> searchAdvancedWithAllCriteria(String hoTen, String email, String sdt,
                                                         String maKhachHang, String diaChi, Integer trangThai,
                                                         Date startDate, Date endDate) {
        try {
            System.out.println("Advanced search with all criteria for customers");

            List<KhachHang> baseResults = repoKhachHang.searchAdvancedWithTaiKhoan(hoTen, email, sdt, maKhachHang, trangThai);

            return baseResults.stream()
                    .filter(kh -> {
                        // Filter theo Ä‘á»‹a chá»‰
                        if (diaChi != null && !diaChi.trim().isEmpty()) {
                            if (!searchInAddress(kh, diaChi)) {
                                return false;
                            }
                        }

                        // Filter theo khoáº£ng thá»i gian
                        if (startDate != null && kh.getNgayTao() != null && kh.getNgayTao().before(startDate)) {
                            return false;
                        }
                        if (endDate != null && kh.getNgayTao() != null && kh.getNgayTao().after(endDate)) {
                            return false;
                        }

                        return true;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error in advanced search with all criteria: " + e.getMessage());
            throw new RuntimeException("TÃ¬m kiáº¿m nÃ¢ng cao khÃ¡ch hÃ ng tháº¥t báº¡i: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhachHang> searchByAddressDetails(String provinceCode, String districtCode, String addressDetail) {
        try {
            return getAllKhachHang().stream()
                    .filter(kh -> {
                        if (kh.getTaiKhoan() == null) return false;

                        List<DiaChi> diaChiList = diaChiService.findByTaiKhoanId(kh.getTaiKhoan().getId());
                        return diaChiList.stream().anyMatch(dc -> {
                            boolean match = true;

                            if (provinceCode != null && !provinceCode.trim().isEmpty()) {
                                match = match && provinceCode.equals(dc.getMaTinh());
                            }

                            // districtCode deprecated - khÃ´ng sá»­ dá»¥ng ná»¯a

                            if (addressDetail != null && !addressDetail.trim().isEmpty()) {
                                match = match && dc.getDiaChiChiTiet() != null &&
                                        dc.getDiaChiChiTiet().toLowerCase().contains(addressDetail.toLowerCase());
                            }

                            return match;
                        });
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error searching by address details: " + e.getMessage());
            return getAllKhachHang();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhachHang> findByPhonePattern(String phonePattern) {
        try {
            if (phonePattern == null || phonePattern.trim().isEmpty()) {
                return getAllKhachHang();
            }
            return getAllKhachHang().stream()
                    .filter(kh -> kh.getSdt() != null && kh.getSdt().contains(phonePattern))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error finding by phone pattern: " + e.getMessage());
            return getAllKhachHang();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhachHang> findByEmailPattern(String emailPattern) {
        try {
            if (emailPattern == null || emailPattern.trim().isEmpty()) {
                return getAllKhachHang();
            }
            return getAllKhachHang().stream()
                    .filter(kh -> kh.getTaiKhoan() != null &&
                            kh.getTaiKhoan().getEmail() != null &&
                            kh.getTaiKhoan().getEmail().toLowerCase().contains(emailPattern.toLowerCase()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error finding by email pattern: " + e.getMessage());
            return getAllKhachHang();
        }
    }

    // ================== CÃC PHÆ¯Æ NG THá»¨C VALIDATION ==================

    @Override
    public boolean isValidKhachHangSearchParams(String hoTen, String email, String sdt) {
        try {
            if (hoTen != null && !hoTen.trim().isEmpty() && !isValidName(hoTen)) {
                return false;
            }

            if (email != null && !email.trim().isEmpty() && !isValidEmailFormat(email)) {
                return false;
            }

            if (sdt != null && !sdt.trim().isEmpty() && !isValidPhoneNumber(sdt)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error validating search params: " + e.getMessage());
            return false;
        }
    }

    // ================== CÃC PHÆ¯Æ NG THá»¨C THá»NG KÃŠ ==================

    @Override
    @Transactional(readOnly = true)
    public long countByStatusAndDateRange(Integer trangThai, Date startDate, Date endDate) {
        try {
            return getAllKhachHang().stream()
                    .filter(kh -> {
                        boolean statusMatch = trangThai == null || kh.getTrangThai().equals(trangThai);
                        boolean dateInRange = true;

                        if (startDate != null && kh.getNgayTao() != null) {
                            dateInRange = !kh.getNgayTao().before(startDate);
                        }
                        if (endDate != null && kh.getNgayTao() != null) {
                            dateInRange = dateInRange && !kh.getNgayTao().after(endDate);
                        }

                        return statusMatch && dateInRange;
                    })
                    .count();
        } catch (Exception e) {
            System.err.println("Error counting by status and date range: " + e.getMessage());
            return 0;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        try {
            List<KhachHang> allCustomers = getAllWithCompleteInfo();

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", (long) allCustomers.size());
            stats.put("active", allCustomers.stream().filter(kh -> kh.getTrangThai() == 1).count());
            stats.put("inactive", allCustomers.stream().filter(kh -> kh.getTrangThai() == 0).count());

            // KhÃ¡ch hÃ ng má»›i (trong 30 ngÃ y qua)
            Date thirtyDaysAgo = new Date(System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L));
            stats.put("recent", allCustomers.stream()
                    .filter(kh -> kh.getNgayTao() != null && kh.getNgayTao().after(thirtyDaysAgo))
                    .count());

            stats.put("profileCompleted", allCustomers.stream()
                    .filter(KhachHang::isProfileCompleted)
                    .count());

            // KhÃ¡ch hÃ ng hÃ´m nay
            Date today = new Date();
            Date startOfDay = new Date(today.getTime() - (today.getTime() % (24 * 60 * 60 * 1000)));
            Date endOfDay = new Date(startOfDay.getTime() + (24 * 60 * 60 * 1000) - 1);

            stats.put("newToday", allCustomers.stream()
                    .filter(kh -> kh.getNgayTao() != null &&
                            kh.getNgayTao().after(startOfDay) &&
                            kh.getNgayTao().before(endOfDay))
                    .count());

            return stats;
        } catch (Exception e) {
            System.err.println("Error getting statistics: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // ================== CÃC PHÆ¯Æ NG THá»¨C NGHIá»†P Vá»¤ ==================

    @Override
    @Transactional
    public KhachHang completeProfile(Integer id, KhachHangDto profileData) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID khÃ¡ch hÃ ng khÃ´ng há»£p lá»‡");
            }

            if (profileData == null) {
                throw new IllegalArgumentException("Dá»¯ liá»‡u profile khÃ´ng Ä‘Æ°á»£c null");
            }

            Optional<KhachHang> customerOpt = findByIdWithEagerLoading(id);
            if (customerOpt.isEmpty()) {
                throw new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng vá»›i ID: " + id);
            }

            KhachHang customer = customerOpt.get();

            List<String> validationErrors = profileData.getProfileCompletionErrors();
            if (!validationErrors.isEmpty()) {
                throw new IllegalArgumentException("Dá»¯ liá»‡u khÃ´ng há»£p lá»‡: " + String.join(", ", validationErrors));
            }

            if (!profileData.getSdt().equals(customer.getSdt())) {
                if (isPhoneNumberUsed(profileData.getSdt(), id)) {
                    throw new IllegalArgumentException("Sá»‘ Ä‘iá»‡n thoáº¡i Ä‘Ã£ Ä‘Æ°á»£c sá»­ dá»¥ng bá»Ÿi khÃ¡ch hÃ ng khÃ¡c");
                }
            }

            customer.updateBasicInfo(profileData.getHoTen(), profileData.getSdt());
            updateKhachHang(customer);

            System.out.println("Profile completed for customer: " + customer.getHoTen() + " (ID: " + id + ")");
            return customer;

        } catch (Exception e) {
            System.err.println("Error completing customer profile: " + e.getMessage());
            throw new RuntimeException("Lá»—i khi hoÃ n thiá»‡n profile: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void updateStatus(Integer id, Integer trangThai) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID khÃ¡ch hÃ ng khÃ´ng há»£p lá»‡");
            }

            if (trangThai == null || (trangThai != 0 && trangThai != 1)) {
                throw new IllegalArgumentException("Tráº¡ng thÃ¡i khÃ´ng há»£p lá»‡ (chá»‰ nháº­n 0 hoáº·c 1)");
            }

            Optional<KhachHang> customerOpt = findByIdWithEagerLoading(id);
            if (customerOpt.isEmpty()) {
                throw new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng vá»›i ID: " + id);
            }

            KhachHang customer = customerOpt.get();

            if (customer.getTrangThai().equals(trangThai)) {
                System.out.println("Customer " + id + " already has status " + trangThai);
                return;
            }

            customer.setTrangThai(trangThai);
            customer.setNgayCapNhat(new Date());

            updateKhachHang(customer);

            String statusText = trangThai == 1 ? "activated" : "deactivated";
            System.out.println("Customer " + statusText + ": " + customer.getHoTen() + " (ID: " + id + ")");

        } catch (Exception e) {
            System.err.println("Error updating customer status: " + e.getMessage());
            throw new RuntimeException("Lá»—i khi cáº­p nháº­t tráº¡ng thÃ¡i: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public KhachHangDto convertToDto(KhachHang entity) {
        try {
            if (entity == null) {
                System.err.println("Warning: Trying to convert null KhachHang entity");
                return null;
            }

            KhachHangDto dto = new KhachHangDto();
            dto.setId(entity.getId());
            dto.setMaKhachHang(entity.getMaKhachHang());
            dto.setHoTen(entity.getHoTen());
            dto.setSdt(entity.getSdt());
            dto.setTrangThai(entity.getTrangThai());
            dto.setNgayTao(entity.getNgayTao());
            dto.setNgayCapNhat(entity.getNgayCapNhat());

            if (entity.getTaiKhoan() != null) {
                dto.setEmail(entity.getTaiKhoan().getEmail());
                dto.setIdTaiKhoan(entity.getTaiKhoan().getId());

                try {
                    List<DiaChi> diaChiList = diaChiService.findByTaiKhoanId(entity.getTaiKhoan().getId());

                    if (diaChiList != null && !diaChiList.isEmpty()) {
                        List<KhachHangDto.DiaChiInfo> diaChiInfoList = diaChiList.stream()
                                .filter(Objects::nonNull)
                                .map(this::convertDiaChiToInfo)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());

                        dto.setDanhSachDiaChi(diaChiInfoList);

                        KhachHangDto.DiaChiInfo defaultAddress = diaChiInfoList.stream()
                                .filter(dc -> dc.getIsDefault() != null && dc.getIsDefault())
                                .findFirst()
                                .orElse(diaChiInfoList.isEmpty() ? null : diaChiInfoList.get(0));

                        dto.setDiaChiMacDinh(defaultAddress);
                    } else {
                        dto.setDanhSachDiaChi(new ArrayList<>());
                        dto.setDiaChiMacDinh(null);
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Could not load addresses for customer " + entity.getId() + ": " + e.getMessage());
                    dto.setDanhSachDiaChi(new ArrayList<>());
                    dto.setDiaChiMacDinh(null);
                }
            } else {
                dto.setEmail(null);
                dto.setIdTaiKhoan(null);
                dto.setDanhSachDiaChi(new ArrayList<>());
                dto.setDiaChiMacDinh(null);
            }

            return dto;
        } catch (Exception e) {
            System.err.println("Error converting customer to DTO: " + e.getMessage());
            e.printStackTrace();

            if (entity != null) {
                KhachHangDto fallbackDto = new KhachHangDto();
                fallbackDto.setId(entity.getId());
                fallbackDto.setMaKhachHang(entity.getMaKhachHang());
                fallbackDto.setHoTen(entity.getHoTen());
                fallbackDto.setSdt(entity.getSdt());
                fallbackDto.setTrangThai(entity.getTrangThai());
                fallbackDto.setNgayTao(entity.getNgayTao());
                fallbackDto.setNgayCapNhat(entity.getNgayCapNhat());
                fallbackDto.setDanhSachDiaChi(new ArrayList<>());
                return fallbackDto;
            }

            return null;
        }
    }

    /**
     * Convert DiaChi entity to DiaChiInfo DTO
     */
    private KhachHangDto.DiaChiInfo convertDiaChiToInfo(DiaChi diaChi) {
        try {
            if (diaChi == null) {
                return null;
            }

            KhachHangDto.DiaChiInfo info = new KhachHangDto.DiaChiInfo();
            info.setId(diaChi.getId());
            info.setMaTinh(diaChi.getMaTinh());
            info.setMaPhuong(diaChi.getMaPhuong());
            info.setTenTinh(diaChi.getTenTinh());
            info.setTenPhuong(diaChi.getTenPhuong());
            info.setDiaChiChiTiet(diaChi.getDiaChiChiTiet());
            info.setIsDefault(diaChi.getIsDefault());
            info.setTrangThai(diaChi.getTrangThai());

            // GhÃ©p Ä‘á»‹a chá»‰ Ä‘áº§y Ä‘á»§ (2-level addressing)
            List<String> parts = new ArrayList<>();
            if (diaChi.getDiaChiChiTiet() != null && !diaChi.getDiaChiChiTiet().trim().isEmpty()) {
                parts.add(diaChi.getDiaChiChiTiet().trim());
            }
            if (diaChi.getTenPhuong() != null && !diaChi.getTenPhuong().trim().isEmpty()) {
                parts.add(diaChi.getTenPhuong().trim());
            }
            if (diaChi.getTenTinh() != null && !diaChi.getTenTinh().trim().isEmpty()) {
                parts.add(diaChi.getTenTinh().trim());
            }

            String diaChiDayDu = parts.isEmpty() ? "ChÆ°a cÃ³ Ä‘á»‹a chá»‰" : String.join(", ", parts);
            info.setDiaChiDayDu(diaChiDayDu);

            return info;
        } catch (Exception e) {
            System.err.println("Warning: Error converting DiaChi to DTO: " + e.getMessage());
            return null;
        }
    }

    // ================== CÃC PHÆ¯Æ NG THá»¨C Bá»” SUNG ==================

    @Override
    @Transactional(readOnly = true)
    public List<KhachHang> getActiveKhachHang() {
        try {
            return getAllWithCompleteInfo().stream()
                    .filter(kh -> kh.getTrangThai() == 1)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting active customers: " + e.getMessage());
            return getAllWithCompleteInfo();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<KhachHang> getKhachHangByDateRange(Date startDate, Date endDate) {
        try {
            return getAllWithCompleteInfo().stream()
                    .filter(kh -> {
                        Date creationDate = kh.getNgayTao();
                        if (creationDate == null) return false;

                        boolean afterStart = startDate == null || !creationDate.before(startDate);
                        boolean beforeEnd = endDate == null || !creationDate.after(endDate);

                        return afterStart && beforeEnd;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting customers by date range: " + e.getMessage());
            return getAllWithCompleteInfo();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getKhachHangStatistics() {
        try {
            List<KhachHang> allCustomers = getAllWithCompleteInfo();

            Map<String, Long> stats = new HashMap<>();
            stats.put("total", (long) allCustomers.size());
            stats.put("active", allCustomers.stream().filter(kh -> kh.getTrangThai() == 1).count());
            stats.put("inactive", allCustomers.stream().filter(kh -> kh.getTrangThai() == 0).count());

            Date thirtyDaysAgo = new Date(System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L));
            stats.put("recent", allCustomers.stream()
                    .filter(kh -> kh.getNgayTao() != null && kh.getNgayTao().after(thirtyDaysAgo))
                    .count());

            return stats;
        } catch (Exception e) {
            System.err.println("Error getting customer statistics: " + e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public boolean canDeleteKhachHang(Integer id) {
        try {
            Optional<KhachHang> khachHang = getKhachHangById(id);
            if (khachHang.isEmpty()) {
                return false;
            }
            // TODO: Implement business rules for deletion
            return true;
        } catch (Exception e) {
            System.err.println("Error checking if customer can be deleted: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void toggleTrangThai(Integer id) {
        try {
            Optional<KhachHang> optional = getKhachHangById(id);
            if (optional.isPresent()) {
                KhachHang kh = optional.get();
                Integer newStatus = kh.getTrangThai() == 1 ? 0 : 1;
                kh.setTrangThai(newStatus);
                kh.setNgayCapNhat(new Date());
                repoKhachHang.save(kh);

                String statusText = newStatus == 1 ? "activated" : "deactivated";
                System.out.println("Customer " + statusText + ": " + kh.getHoTen() + " (ID: " + id + ")");
            } else {
                throw new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y khÃ¡ch hÃ ng vá»›i ID: " + id);
            }
        } catch (Exception e) {
            System.err.println("Error toggling customer status: " + e.getMessage());
            throw new RuntimeException("Lá»—i khi thay Ä‘á»•i tráº¡ng thÃ¡i khÃ¡ch hÃ ng: " + e.getMessage(), e);
        }
    }

    // ================== UTILITY METHODS ==================

    /**
     * TÃ¬m kiáº¿m trong Ä‘á»‹a chá»‰ cá»§a khÃ¡ch hÃ ng
     */
    private boolean searchInAddress(KhachHang kh, String diaChi) {
        if (kh.getTaiKhoan() == null) return false;

        try {
            List<DiaChi> diaChiList = diaChiService.findByTaiKhoanId(kh.getTaiKhoan().getId());
            String searchLower = diaChi.toLowerCase();

            return diaChiList.stream().anyMatch(dc ->
                    (dc.getTenTinh() != null && dc.getTenTinh().toLowerCase().contains(searchLower)) ||
                            (dc.getTenPhuong() != null && dc.getTenPhuong().toLowerCase().contains(searchLower)) ||
                            (dc.getDiaChiChiTiet() != null && dc.getDiaChiChiTiet().toLowerCase().contains(searchLower)) ||
                            (dc.getMaTinh() != null && dc.getMaTinh().toLowerCase().contains(searchLower)) ||
                            (dc.getMaPhuong() != null && dc.getMaPhuong().toLowerCase().contains(searchLower))
            );
        } catch (Exception e) {
            System.err.println("Warning: Could not search addresses for customer " + kh.getId() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Táº¡o mÃ£ khÃ¡ch hÃ ng duy nháº¥t
     */
    private String generateMaKhachHang() {
        try {
            String prefix = "KH";
            long timestamp = System.currentTimeMillis();
            int random = (int) (Math.random() * 1000);

            String maKhachHang;
            int attempts = 0;
            do {
                maKhachHang = prefix + String.format("%d%03d", (timestamp + attempts) % 100000, random);
                attempts++;
            } while (findByMaKhachHang(maKhachHang).isPresent() && attempts < 10);

            if (attempts >= 10) {
                maKhachHang = prefix + timestamp + String.format("%03d", random);
            }

            System.out.println("Generated customer code: " + maKhachHang);
            return maKhachHang;
        } catch (Exception e) {
            System.err.println("Error generating customer code: " + e.getMessage());
            return "KH" + System.currentTimeMillis();
        }
    }

    /**
     * Validate dá»¯ liá»‡u cÆ¡ báº£n cá»§a khÃ¡ch hÃ ng
     */
    private void validateKhachHangData(KhachHang khachHang) {
        if (khachHang == null) {
            throw new IllegalArgumentException("ThÃ´ng tin khÃ¡ch hÃ ng khÃ´ng Ä‘Æ°á»£c null");
        }

        if (khachHang.getHoTen() == null || khachHang.getHoTen().trim().isEmpty()) {
            throw new IllegalArgumentException("Há» tÃªn khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }
        if (khachHang.getHoTen().length() > 255) {
            throw new IllegalArgumentException("Há» tÃªn khÃ´ng Ä‘Æ°á»£c quÃ¡ 255 kÃ½ tá»±");
        }

        if (khachHang.getSdt() == null || khachHang.getSdt().trim().isEmpty()) {
            throw new IllegalArgumentException("Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }
        if (!khachHang.getSdt().matches("^0\\d{9}$")) {
            throw new IllegalArgumentException("Sá»‘ Ä‘iá»‡n thoáº¡i khÃ´ng há»£p lá»‡ (pháº£i cÃ³ 10 sá»‘ vÃ  báº¯t Ä‘áº§u báº±ng 0)");
        }

        if (khachHang.getTrangThai() == null || (khachHang.getTrangThai() != 0 && khachHang.getTrangThai() != 1)) {
            throw new IllegalArgumentException("Tráº¡ng thÃ¡i khÃ´ng há»£p lá»‡ (chá»‰ nháº­n 0 hoáº·c 1)");
        }
    }

    /**
     * Validate tÃªn (chá»‰ chá»¯ cÃ¡i vÃ  khoáº£ng tráº¯ng, há»— trá»£ tiáº¿ng Viá»‡t)
     */
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return name.matches("^[a-zA-ZÃ€ÃÃ‚ÃƒÃˆÃ‰ÃŠÃŒÃÃ’Ã“Ã”Ã•Ã™ÃšÄ‚ÄÄ¨Å¨Æ Ã Ã¡Ã¢Ã£Ã¨Ã©ÃªÃ¬Ã­Ã²Ã³Ã´ÃµÃ¹ÃºÄƒÄ‘Ä©Å©Æ¡Æ¯Ä‚áº áº¢áº¤áº¦áº¨áºªáº¬áº®áº°áº²áº´áº¶áº¸áººáº¼á»€á»€á»‚Æ°Äƒáº¡áº£áº¥áº§áº©áº«áº­áº¯áº±áº³áºµáº·áº¹áº»áº½á»áº¿á»ƒá»„á»†á»ˆá»Šá»Œá»Žá»á»’á»”á»–á»˜á»šá»œá»žá» á»¢á»¤á»¦á»¨á»ªá»…á»‡á»‰á»‹á»á»á»‘á»“á»•á»—á»™á»›á»á»Ÿá»¡á»£á»¥á»§á»©á»«á»¬á»®á»°á»²á»´Ãá»¶á»¸á»­á»¯á»±á»³á»µÃ½á»·á»¹\\s]+$");
    }

    /**
     * Validate Ä‘á»‹nh dáº¡ng email
     */
    private boolean isValidEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    /**
     * Validate sá»‘ Ä‘iá»‡n thoáº¡i (10-11 sá»‘, báº¯t Ä‘áº§u báº±ng 0)
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        return phone.matches("^0\\d{9,10}$");
    }
}
