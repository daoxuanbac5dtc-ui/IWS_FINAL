package org.example.iws_websitesneaker.Dto;

public class ProvinceApiDto {
    private String name;
    private Integer code;
    private String division_type;
    private Integer phone_code;
    private String codename;

    // Constructor
    public ProvinceApiDto() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getDivision_type() { return division_type; }
    public void setDivision_type(String division_type) { this.division_type = division_type; }

    public Integer getPhone_code() { return phone_code; }
    public void setPhone_code(Integer phone_code) { this.phone_code = phone_code; }

    public String getCodename() { return codename; }
    public void setCodename(String codename) { this.codename = codename; }
}
