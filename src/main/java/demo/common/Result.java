package demo.common;

import lombok.Data;

/**
 * 全局统一响应结果
 * @param <T> 返回数据类型
 */
@Data
public class Result<T> {
    private Integer code;   // 状态码 200成功
    private String msg;     // 提示信息
    private T data;         // 返回数据

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }
}