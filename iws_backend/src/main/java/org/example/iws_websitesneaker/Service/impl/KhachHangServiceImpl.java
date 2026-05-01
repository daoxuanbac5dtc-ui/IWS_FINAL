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
 * Triển khai service quản lý Khách hàng - ĐÃ SỬA LỖI LazyInitializationException
 * Cung cấp các chức năng CRUD và tìm kiếm cho khách hàng
 */
@Service
@Transactional
public class KhachHangServiceImpl implements KhachHangService {

    @Autowired
    private RepoKhachHang repoKhachHang;

    @Autowired
    private DiaChiService diaChiService;

    // ================== CÁC THAO TÁC CRUD CƠ BẢN ==================

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
            throw new RuntimeException("Lỗi khi lấy danh sách khách hàng: " + e.getMessage(), e);
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
            throw new RuntimeException("Lỗi khi lấy thông tin khách hàng: " + e.getMessage(), e);
        }
    }

    @Override
    public void addKhachHang(KhachHang khachHang) {
        try {
            if (khachHang == null) {
                throw new IllegalArgumentException("Thông tin khách hàng không được null");
            }

            // Thiết lập ngày tạo nếu chưa có
            if (khachHang.getNgayTao() == null) {
                khachHang.setNgayTao(new Date());
            }
            khachHang.setNgayCapNhat(new Date());

            // Tạo mã khách hàng nếu chưa có
            if (khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().isEmpty()) {
                khachHang.setMaKhachHang(generateMaKhachHang());
            }

            // Thiết lập trạng thái mặc định
            if (khachHang.getTrangThai() == null) {
                khachHang.setTrangThai(1);
            }

            // Kiểm tra trùng lặp
            if (existsBySdt(khachHang.getSdt())) {
                throw new IllegalArgumentException("Số điện thoại đã tồn tại: " + khachHang.getSdt());
            }

            validateKhachHangData(khachHang);
            repoKhachHang.save(khachHang);
            System.out.println("Created customer: " + khachHang.getHoTen() + " (ID: " + khachHang.getId() + ")");

        } catch (Exception e) {
            System.err.println("Error adding customer: " + e.getMessage());
            throw new RuntimeException("Lỗi khi thêm khách hàng: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateKhachHang(KhachHang khachHang) {
        try {
            if (khachHang == null || khachHang.getId() == null) {
                throw new IllegalArgumentException("Thông tin khách hàng hoặc ID không được null");
            }

            Optional<KhachHang> existing = getKhachHangById(khachHang.getId());
            if (existing.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + khachHang.getId());
            }

            validateKhachHangData(khachHang);

            KhachHang existingKH = existing.get();
            if (!khachHang.getSdt().equals(existingKH.getSdt())) {
                if (existsBySdt(khachHang.getSdt())) {
                    throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi khách hàng khác");
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
            throw new RuntimeException("Lỗi khi cập nhật khách hàng: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteKhachHang(Integer id) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID khách hàng không hợp lệ");
            }

            Optional<KhachHang> khachHang = getKhachHangById(id);
            if (khachHang.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + id);
            }

            KhachHang kh = khachHang.get();
            if (!canDeleteKhachHang(id)) {
                throw new IllegalStateException("Không thể xóa khách hàng này do còn dữ liệu liên quan");
            }

            // Soft delete
            kh.setTrangThai(0);
            kh.setNgayCapNhat(new Date());
            repoKhachHang.save(kh);

            System.out.println("Soft deleted customer: " + kh.getHoTen() + " (ID: " + id + ")");

        } catch (Exception e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            throw new RuntimeException("Lỗi khi xóa khách hàng: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public KhachHang findByTaiKhoanId(Integer taiKhoanId) {
        try {
            System.out.println("🔍 DEBUG KhachHangService - Tìm khách hàng theo TaiKhoan ID: " + taiKhoanId);

            // Tìm theo tai_khoan_id trong bảng khach_hang
            Optional<KhachHang> result = repoKhachHang.findByTaiKhoanId(taiKhoanId);

            if (result.isPresent()) {
                KhachHang khachHang = result.get();
                System.out.println("✅ DEBUG KhachHangService - Tìm thấy: " +
                        "KH.ID=" + khachHang.getId() +
                        ", Tên=" + khachHang.getHoTen() +
                        ", TaiKhoan.ID=" + (khachHang.getTaiKhoan() != null ? khachHang.getTaiKhoan().getId() : "null"));
                return khachHang;
            } else {
                System.out.println("❌ DEBUG KhachHangService - Không tìm thấy khách hàng cho TaiKhoan ID: " + taiKhoanId);
                return null;
            }
        } catch (Exception e) {
            System.out.println("💥 DEBUG KhachHangService - Lỗi: " + e.getMessage());
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
            throw new RuntimeException("Không thể xóa khách hàng: " + e.getMessage(), e);
        }
    }

    // ================== CÁC PHƯƠNG THỨC TÌM KIẾM ==================

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

    // ================== CÁC PHƯƠNG THỨC TÌM KIẾM CƠ BẢN ==================

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
                        // Filter theo khoảng thời gian
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
            throw new RuntimeException("Tìm kiếm nâng cao thất bại: " + e.getMessage(), e);
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
                        // Filter theo địa chỉ
                        if (diaChi != null && !diaChi.trim().isEmpty()) {
                            if (!searchInAddress(kh, diaChi)) {
                                return false;
                            }
                        }

                        // Filter theo khoảng thời gian
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
            throw new RuntimeException("Tìm kiếm nâng cao khách hàng thất bại: " + e.getMessage(), e);
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

                            // districtCode deprecated - không sử dụng nữa

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

    // ================== CÁC PHƯƠNG THỨC VALIDATION ==================

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

    // ================== CÁC PHƯƠNG THỨC THỐNG KÊ ==================

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

            // Khách hàng mới (trong 30 ngày qua)
            Date thirtyDaysAgo = new Date(System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L));
            stats.put("recent", allCustomers.stream()
                    .filter(kh -> kh.getNgayTao() != null && kh.getNgayTao().after(thirtyDaysAgo))
                    .count());

            stats.put("profileCompleted", allCustomers.stream()
                    .filter(KhachHang::isProfileCompleted)
                    .count());

            // Khách hàng hôm nay
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

    // ================== CÁC PHƯƠNG THỨC NGHIỆP VỤ ==================

    @Override
    @Transactional
    public KhachHang completeProfile(Integer id, KhachHangDto profileData) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID khách hàng không hợp lệ");
            }

            if (profileData == null) {
                throw new IllegalArgumentException("Dữ liệu profile không được null");
            }

            Optional<KhachHang> customerOpt = findByIdWithEagerLoading(id);
            if (customerOpt.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + id);
            }

            KhachHang customer = customerOpt.get();

            List<String> validationErrors = profileData.getProfileCompletionErrors();
            if (!validationErrors.isEmpty()) {
                throw new IllegalArgumentException("Dữ liệu không hợp lệ: " + String.join(", ", validationErrors));
            }

            if (!profileData.getSdt().equals(customer.getSdt())) {
                if (isPhoneNumberUsed(profileData.getSdt(), id)) {
                    throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi khách hàng khác");
                }
            }

            customer.updateBasicInfo(profileData.getHoTen(), profileData.getSdt());
            updateKhachHang(customer);

            System.out.println("Profile completed for customer: " + customer.getHoTen() + " (ID: " + id + ")");
            return customer;

        } catch (Exception e) {
            System.err.println("Error completing customer profile: " + e.getMessage());
            throw new RuntimeException("Lỗi khi hoàn thiện profile: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void updateStatus(Integer id, Integer trangThai) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID khách hàng không hợp lệ");
            }

            if (trangThai == null || (trangThai != 0 && trangThai != 1)) {
                throw new IllegalArgumentException("Trạng thái không hợp lệ (chỉ nhận 0 hoặc 1)");
            }

            Optional<KhachHang> customerOpt = findByIdWithEagerLoading(id);
            if (customerOpt.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + id);
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
            throw new RuntimeException("Lỗi khi cập nhật trạng thái: " + e.getMessage(), e);
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

            // Ghép địa chỉ đầy đủ (2-level addressing)
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

            String diaChiDayDu = parts.isEmpty() ? "Chưa có địa chỉ" : String.join(", ", parts);
            info.setDiaChiDayDu(diaChiDayDu);

            return info;
        } catch (Exception e) {
            System.err.println("Warning: Error converting DiaChi to DTO: " + e.getMessage());
            return null;
        }
    }

    // ================== CÁC PHƯƠNG THỨC BỔ SUNG ==================

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
                throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + id);
            }
        } catch (Exception e) {
            System.err.println("Error toggling customer status: " + e.getMessage());
            throw new RuntimeException("Lỗi khi thay đổi trạng thái khách hàng: " + e.getMessage(), e);
        }
    }

    // ================== UTILITY METHODS ==================

    /**
     * Tìm kiếm trong địa chỉ của khách hàng
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
     * Tạo mã khách hàng duy nhất
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
     * Validate dữ liệu cơ bản của khách hàng
     */
    private void validateKhachHangData(KhachHang khachHang) {
        if (khachHang == null) {
            throw new IllegalArgumentException("Thông tin khách hàng không được null");
        }

        if (khachHang.getHoTen() == null || khachHang.getHoTen().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        if (khachHang.getHoTen().length() > 255) {
            throw new IllegalArgumentException("Họ tên không được quá 255 ký tự");
        }

        if (khachHang.getSdt() == null || khachHang.getSdt().trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }
        if (!khachHang.getSdt().matches("^0\\d{9}$")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (phải có 10 số và bắt đầu bằng 0)");
        }

        if (khachHang.getTrangThai() == null || (khachHang.getTrangThai() != 0 && khachHang.getTrangThai() != 1)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ (chỉ nhận 0 hoặc 1)");
        }
    }

    /**
     * Validate tên (chỉ chữ cái và khoảng trắng, hỗ trợ tiếng Việt)
     */
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return name.matches("^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềếểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵýỷỹ\\s]+$");
    }

    /**
     * Validate định dạng email
     */
    private boolean isValidEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    /**
     * Validate số điện thoại (10-11 số, bắt đầu bằng 0)
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) return false;
        return phone.matches("^0\\d{9,10}$");
    }
}
