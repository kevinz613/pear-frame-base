package io.kevinz613.pear.convention.result;

import io.kevinz613.pear.convention.httpcode.BaseHttpCode;

/**
 * 响应生成器
 *
 * @author kevinz613
 */
public class ResponseBuilder {

    /**
     * 成功响应
     *
     * @return 成功响应信息
     */
    public static <T> Response<T> success() {
        Response<T> Response = new Response<>();
        Response.setCode(BaseHttpCode.SUCCESS.code());
        Response.setMessage(BaseHttpCode.SUCCESS.message());
        return Response;
    }

    public static <T> Response<T> success(T data) {
        Response<T> Response = new Response<>();
        Response.setCode(BaseHttpCode.SUCCESS.code());
        Response.setMessage(BaseHttpCode.SUCCESS.message());
        Response.setData(data);
        return Response;
    }

    public static <T> Response<T> success(T data, String message) {
        Response<T> Response = new Response<>();
        Response.setCode(BaseHttpCode.SUCCESS.code());
        Response.setMessage(message);
        Response.setData(data);
        return Response;
    }

    public static <T> Response<T> success(String message) {
        Response<T> Response = new Response<>();
        Response.setCode(BaseHttpCode.SUCCESS.code());
        Response.setMessage(message);
        return Response;
    }


    /**
     * 错误响应
     *
     * @param code    响应码
     * @param message 响应消息
     * @return 错误响应
     */
    public static <T> Response<T> error(String code, String message) {
        Response<T> Response = new Response<>();
        Response.setCode(code);
        Response.setMessage(message);
        return Response;
    }

    public static <T> Response<T> error(BaseHttpCode httpCode, String message) {
        Response<T> Response = new Response<>();
        Response.setCode(httpCode.code());
        Response.setMessage(message);
        return Response;
    }

    public static <T> Response<T> error(BaseHttpCode httpCode) {
        Response<T> Response = new Response<>();
        Response.setCode(httpCode.code());
        Response.setMessage(httpCode.message());
        return Response;
    }
}
