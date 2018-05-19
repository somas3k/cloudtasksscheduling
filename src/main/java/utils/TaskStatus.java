package utils;

import java.io.Serializable;

public enum TaskStatus implements Serializable {
    CREATED, SCHEDULING, WAITING_FOR_SEND, EXECUTING
}
