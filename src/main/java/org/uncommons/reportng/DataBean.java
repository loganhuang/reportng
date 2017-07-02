package org.uncommons.reportng;

import java.util.List;

/**
 * Created by logan on 1/8/17.
 */
public class DataBean {
    private String testName; //测试用例名
    private String duration; //单个测试时间
    private String params; //测试用例参数
    private List<String> output; //Reporter Output

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public List<String> getOutput() {
        return output;
    }

    public void setOutput(List<String> output) {
        this.output = output;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    private StackTraceElement[] stackTrace; // 异常堆栈信息

}
