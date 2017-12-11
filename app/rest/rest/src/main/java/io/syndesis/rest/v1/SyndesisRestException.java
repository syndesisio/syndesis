package io.syndesis.rest.v1;

/**
 * A fully customizable rest exception.
 */
public class SyndesisRestException extends RuntimeException {

    private String developerMsg;

    private String userMsg;

    private String userMsgDetail;

    private Integer errorCode;

    public SyndesisRestException(String developerMsg, String userMsg, String userMsgDetail, Integer errorCode) {
        super(developerMsg);
        this.developerMsg = developerMsg;
        this.userMsg = userMsg;
        this.userMsgDetail = userMsgDetail;
        this.errorCode = errorCode;
    }

    public SyndesisRestException(String developerMsg, String userMsg, String userMsgDetail, Integer errorCode, Throwable cause) {
        super(developerMsg, cause);
        this.developerMsg = developerMsg;
        this.userMsg = userMsg;
        this.userMsgDetail = userMsgDetail;
        this.errorCode = errorCode;
    }

    public String getDeveloperMsg() {
        return developerMsg;
    }

    public String getUserMsg() {
        return userMsg;
    }

    public String getUserMsgDetail() {
        return userMsgDetail;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
