package ms.learn.common;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private int code;
    private String msg;
    private long timeStamp;
    private T data;
}
