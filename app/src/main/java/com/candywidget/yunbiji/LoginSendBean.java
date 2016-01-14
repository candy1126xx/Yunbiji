package com.candywidget.yunbiji;

/**
 * 登录Bean,包含username、password
 * Created by Candy on 2015/12/19.
 */
public class LoginSendBean {
    private String username;
    private String password;

    public LoginSendBean(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
