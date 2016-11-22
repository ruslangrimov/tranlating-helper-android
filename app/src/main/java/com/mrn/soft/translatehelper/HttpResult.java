package com.mrn.soft.translatehelper;

/**
 * Created by Ruslan Grimov on 06.10.2016.
 */

public class HttpResult {
    int code;
    String status;
    String answer;

    HttpResult(int code,  String status, String answer) {
        this.code = code;
        this.status = status;
        this.answer = answer;
    }
}
