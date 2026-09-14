package org.txf.myblogsprinboot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum CodeEnums {
    SUCCESS(200, "success"),
    FAIL(-1, "fail"),
    NO_LOGIN(401, "nologin"),
    PARAM_ERROR(-2, "param error");
    private int code;
    private String description;
}
