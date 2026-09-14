package org.txf.myblogsprinboot.advice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.txf.myblogsprinboot.enums.CodeEnums;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> fail(String msg) {
        Result<T> ret = new Result<>();
        ret.setCode(CodeEnums.FAIL.getCode());
        ret.setMsg(msg);
        return ret;
    }

    public static <T> Result<T> fail(String msg, T data) {
        Result<T> ret = new Result<>();
        ret.setCode(CodeEnums.FAIL.getCode());
        ret.setMsg(msg);
        ret.setData(data);
        return ret;
    }

    public static <T> Result<T> success() {
        Result<T> ret = new Result<>();
        ret.setCode(CodeEnums.SUCCESS.getCode());
        return ret;
    }

    public static <T> Result<T> success(String msg) {
        Result<T> ret = new Result<>();
        ret.setCode(CodeEnums.SUCCESS.getCode());
        ret.setMsg(msg);
        return ret;
    }

    public static <T> Result<T> success(String msg, T data) {
        Result<T> ret = new Result<>();
        ret.setCode(CodeEnums.SUCCESS.getCode());
        ret.setMsg(msg);
        ret.setData(data);
        return ret;
    }
    public static <T> Result<T> nologin() {
        Result<T> ret = new Result<>();
        ret.setCode(CodeEnums.NO_LOGIN.getCode());
        ret.setMsg("用户未登录.");
        return ret;
    }

    public static <T> Result<T> paramError() {
        Result<T> ret = new Result<>();
        ret.setCode(CodeEnums.PARAM_ERROR.getCode());
        ret.setMsg(CodeEnums.PARAM_ERROR.getDescription());
        return ret;
    }

    public static <T> Result<T> paramError(String msg) {
        Result<T> ret = new Result<>();
        ret.setCode(CodeEnums.PARAM_ERROR.getCode());
        ret.setMsg(msg);
        return ret;
    }
}
