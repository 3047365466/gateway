
package com.edan.rapid.common.exception;

import com.edan.rapid.common.enums.ResponseCode;


public class RapidResponseException extends RapidBaseException {

    private static final long serialVersionUID = -5658789202509039759L;

    public RapidResponseException() {
        this(ResponseCode.INTERNAL_ERROR);
    }

    public RapidResponseException(ResponseCode code) {
        super(code.getMessage(), code);
    }

    public RapidResponseException(Throwable cause, ResponseCode code) {
        super(code.getMessage(), cause, code);
        this.code = code;
    }

}
