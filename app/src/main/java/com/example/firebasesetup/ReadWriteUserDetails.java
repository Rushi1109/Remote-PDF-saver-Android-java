package com.example.firebasesetup;

public class ReadWriteUserDetails {
    private String doB, gender, mobile;

    public ReadWriteUserDetails(){};

    public ReadWriteUserDetails(String textDoB, String textGender, String textMobile) {
        this.doB = textDoB;
        this.gender = textGender;
        this.mobile = textMobile;
    }

    public String getDoB() {
        return doB;
    }

    public void setDoB(String doB) {
        this.doB = doB;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
