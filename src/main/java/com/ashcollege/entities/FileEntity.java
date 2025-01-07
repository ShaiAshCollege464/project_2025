package com.ashcollege.entities;

public class FileEntity extends BaseEntity{
    private String name;
    UserEntity userId;
    MaterialEntity materialId;
    String url;
    public FileEntity(String name, UserEntity userId, MaterialEntity materialId,String url) {
        this.name = name;
        this.userId = userId;
        this.materialId = materialId;
        this.url = url;
    }

    public FileEntity() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserEntity getUserId() {
        return userId;
    }

    public void setUserId(UserEntity userId) {
        this.userId = userId;
    }

    public MaterialEntity getMaterialId() {
        return materialId;
    }

    public void setMaterialId(MaterialEntity materialId) {
        this.materialId = materialId;
    }
}
