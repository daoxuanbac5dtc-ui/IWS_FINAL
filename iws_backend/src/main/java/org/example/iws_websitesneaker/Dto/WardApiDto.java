package org.example.iws_websitesneaker.Dto;

public class WardApiDto {
    private String name;
    private Integer code;
    private String codename;
    private String division_type;
    private Integer province_code;

    // Constructor
    public WardApiDto() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getCodename() { return codename; }
    public void setCodename(String codename) { this.codename = codename; }

    public String getDivision_type() { return division_type; }
    public void setDivision_type(String division_type) { this.division_type = division_type; }

    public Integer getProvince_code() { return province_code; }
    public void setProvince_code(Integer province_code) { this.province_code = province_code; }
}
