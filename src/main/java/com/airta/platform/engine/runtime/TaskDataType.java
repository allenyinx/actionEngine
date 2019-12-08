package com.airta.platform.engine.runtime;

public interface TaskDataType {
    int HTML = 1;
    int SCREEN = 2;
    int LOG = 4;
    int ERROR = 8;
    int ELEMENT = 16;
    int URL = 32;
    int RESULT = 64;
    int ALL = 127;
}
