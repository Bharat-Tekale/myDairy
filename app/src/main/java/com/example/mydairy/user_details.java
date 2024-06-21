package com.example.mydairy;

public class user_details {
    private String name, dairy_name , mobile, email;

    public user_details(){

    }

    public user_details(String name, String dairy_name, String mobile, String email) {
        this.name = name;
        this.dairy_name = dairy_name;
        this.mobile = mobile;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDairy_name() {
        return dairy_name;
    }

    public void setDairy_name(String dairy_name) {
        this.dairy_name = dairy_name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
