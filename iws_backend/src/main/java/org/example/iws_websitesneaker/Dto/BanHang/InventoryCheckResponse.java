package org.example.iws_websitesneaker.Dto.BanHang;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckResponse {
    private Integer chiTietSanPhamId;
    private String tenSanPham;
    private String mauSac;
    private String kichCo;
    private Integer soLuongTon;
    private Integer soLuongCanBan; // THÃŠM field nÃ y
    private Boolean coTheban;
    private String thongBao;

    // ThÃªm method tiá»‡n Ã­ch
    public boolean isAvailable() {
        return Boolean.TRUE.equals(coTheban);
    }

    public int getSoLuongThieu() {
        if (Boolean.TRUE.equals(coTheban)) {
            return 0;
        }
        return Math.max(0, (soLuongCanBan != null ? soLuongCanBan : 0) -
                (soLuongTon != null ? soLuongTon : 0));
    }
}
