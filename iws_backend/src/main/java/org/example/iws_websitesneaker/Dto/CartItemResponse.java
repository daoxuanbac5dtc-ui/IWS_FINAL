package org.example.iws_websitesneaker.Dto;

import lombok.Data;
import java.util.Date;

@Data
public class CartItemResponse {
    private Integer id;                    // âœ… Khá»›p vá»›i frontend
    private Integer productDetailId;       // âœ… Khá»›p
    private String name;                   // âœ… Khá»›p (productName -> name)
    private String code;                   // âœ… Khá»›p (productCode -> code)
    private String image;                  // âœ… Khá»›p (imageUrl -> image)
    private Double price;                  // âœ… Khá»›p
    private Integer quantity;              // âœ… Khá»›p
    private Integer stock;                 // âœ… Khá»›p
    private Integer points;                // âœ… THÃŠM - frontend cÃ³
    private Double totalPrice;             // âœ… Khá»›p

    // Size vÃ  Color - THAY Äá»”I Ä‘á»ƒ khá»›p frontend
    private String size;                   // âœ… Frontend dÃ¹ng string
    private String color;               // âœ… Frontend dÃ¹ng object

    private Date createdDate;              // âœ… Khá»›p
    private Date updatedDate;              // âœ… Khá»›p

    // Inner class cho color info
    @Data
    public static class ColorInfo {
        private Integer id;
        private String name;
        private String code;

        public ColorInfo() {}

        public ColorInfo(Integer id, String name, String code) {
            this.id = id;
            this.name = name;
            this.code = code;
        }
    }

    // Constructors
    public CartItemResponse() {}

    public CartItemResponse(Integer id, Integer productDetailId, String name,
                            String code, String image, Double price, Integer quantity,
                            String size, String color, Integer stock,
                            Integer points, Double totalPrice, Date createdDate, Date updatedDate) {
        this.id = id;
        this.productDetailId = productDetailId;
        this.name = name;
        this.code = code;
        this.image = image;
        this.price = price;
        this.quantity = quantity;
        this.size = size;
        this.color = color;
        this.stock = stock;
        this.points = points;
        this.totalPrice = totalPrice;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProductDetailId() { return productDetailId; }
    public void setProductDetailId(Integer productDetailId) { this.productDetailId = productDetailId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public Date getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(Date updatedDate) { this.updatedDate = updatedDate; }
}
