package com.u2apple.testman.model;

import java.util.Arrays;

public class AndroidDevice {
    private String macAddress;
    private String vid;
    private String pid;
    private String prot;
    private String sn;
    private String adbDevice;
    private String roProductModel;
    private String roProductBrand;
    private String roProductDevice;
    private String roProductBoard;
    private String roProductManufacturer;
    private String roHardware;
    private String roBuildDisplayId;
    private String customProps;
    private String androidVersion;
    private String createdAt;
    private String identified;
    private String productId;
    private String resolution;
    private String partitions;
    private String cpuHardware;
    private String returnProductId;
    // Only for generate unit test case.
    private String[] vids;

    public AndroidDevice(String vid, String roProductModel, String roProductBrand, String productId) {
        super();
        this.vid = vid;
        this.roProductModel = roProductModel;
        this.roProductBrand = roProductBrand;
        this.productId = productId;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String[] getVids() {
        return vids;
    }

    public void setVids(String[] vids) {
        this.vids = vids;
    }

    public String getRoProductModel() {
        return roProductModel;
    }

    public void setRoProductModel(String roProductModel) {
        this.roProductModel = roProductModel;
    }

    public String getRoProductBrand() {
        return roProductBrand;
    }

    public void setRoProductBrand(String roProductBrand) {
        this.roProductBrand = roProductBrand;
    }

    public String getRoProductDevice() {
        return roProductDevice;
    }

    public void setRoProductDevice(String roProductDevice) {
        this.roProductDevice = roProductDevice;
    }

    public AndroidDevice(String vid, String roProductModel, String roProductBrand) {
        super();
        this.vid = vid;
        this.roProductModel = roProductModel;
        this.roProductBrand = roProductBrand;
    }

    public AndroidDevice() {

    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getPartitions() {
        return partitions;
    }

    public void setPartitions(String partitions) {
        this.partitions = partitions;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getCpuHardware() {
        return cpuHardware;
    }

    public void setCpuHardware(String cpuHardware) {
        this.cpuHardware = cpuHardware;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getProt() {
        return prot;
    }

    public void setProt(String prot) {
        this.prot = prot;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getAdbDevice() {
        return adbDevice;
    }

    public void setAdbDevice(String adbDevice) {
        this.adbDevice = adbDevice;
    }

    public String getRoProductBoard() {
        return roProductBoard;
    }

    public void setRoProductBoard(String roProductBoard) {
        this.roProductBoard = roProductBoard;
    }

    public String getRoProductManufacturer() {
        return roProductManufacturer;
    }

    public void setRoProductManufacturer(String roProductManufacturer) {
        this.roProductManufacturer = roProductManufacturer;
    }

    public String getRoHardware() {
        return roHardware;
    }

    public void setRoHardware(String roHardWare) {
        this.roHardware = roHardWare;
    }

    public String getRoBuildDisplayId() {
        return roBuildDisplayId;
    }

    public void setRoBuildDisplayId(String roBuildDisplayId) {
        this.roBuildDisplayId = roBuildDisplayId;
    }

    public String getCustomProps() {
        return customProps;
    }

    public void setCustomProps(String customProps) {
        this.customProps = customProps;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getIdentified() {
        return identified;
    }

    public void setIdentified(String identified) {
        this.identified = identified;
    }

    public String getReturnProductId() {
        return returnProductId;
    }

    public void setReturnProductId(String returnProductId) {
        this.returnProductId = returnProductId;
    }

    @Override
    public String toString() {
        return "AndroidDevice [macAddress=" + macAddress + ", vid=" + vid + ", pid=" + pid + ", prot=" + prot + ", sn="
                + sn + ", adbDevice=" + adbDevice + ", roProductModel=" + roProductModel + ", roProductBrand="
                + roProductBrand + ", roProductDevice=" + roProductDevice + ", roProductBoard=" + roProductBoard
                + ", roProductManufacturer=" + roProductManufacturer + ", roHardWare=" + roHardware
                + ", roBuildDisplayId=" + roBuildDisplayId + ", customProps=" + customProps + ", androidVersion="
                + androidVersion + ", createdAt=" + createdAt + ", identified=" + identified + ", productId="
                + productId + ", resolution=" + resolution + ", partitions=" + partitions + ", cpuHardware="
                + cpuHardware + ", returnProductId=" + returnProductId + ", productIds=" + Arrays.toString(vids) + "]";
    }

}
