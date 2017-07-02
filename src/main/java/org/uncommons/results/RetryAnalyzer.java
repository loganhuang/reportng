package org.uncommons.results;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.Reporter;

/**
 * Created by huangzhw on 2016/11/24.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private final int defaultMaxRetries = 1;
    private final int defaultSleepBetweenRetries = 1000;
    private int currentTry;
    private String previousTest = null;
    private String currentTest = null;
    private int configMaxRetries = defaultMaxRetries;
    private int configSleepBetweenRetries = defaultSleepBetweenRetries;
    private boolean isReadParameter = false;

    public RetryAnalyzer() {
        currentTry = 0;
    }

    @Override
    public boolean retry(ITestResult result) {

        boolean retValue = false;

        //read parameters if not read before
        if (!isReadParameter) {
            getParameters(result);
        }
        Reporter.log("------------------retried--------------------");
        currentTest = result.getTestContext().getCurrentXmlTest().getName();
        if (previousTest == null) {
            previousTest = currentTest;
        }
        //previous test not equals to current test, reset currentTry
        if (!(previousTest.equals(currentTest))) {
            currentTry = 0;
        }

        if (currentTry < configMaxRetries && !result.isSuccess()) {
            try {
                Thread.sleep(configSleepBetweenRetries);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            currentTry++;
            result.setStatus(ITestResult.SUCCESS_PERCENTAGE_FAILURE);
            retValue = true;

        } else {
            currentTry = 0;
        }
        previousTest = currentTest;

        return retValue;
    }

    /**
     * read parameters from testng runnable xml file
     *
     * @param result
     */
    private void getParameters(ITestResult result) {
        String maxRetriesStr = result.getTestContext().getSuite().getParameter("maxRetries");
        if (maxRetriesStr != null) {
            try {
                configMaxRetries = Integer.parseInt(maxRetriesStr);
                if (configMaxRetries <= 1) {
                    configMaxRetries = 1;
                }
            } catch (final NumberFormatException e) {
                System.out.println("NumberFormatException while parsing configMaxRetries from suite file." + e);
            }
        }

        String sleepBetweenRetriesStr = result.getTestContext().getSuite().getParameter("sleepBetweenRetries");
        if (sleepBetweenRetriesStr != null) {
            try {
                configSleepBetweenRetries = Integer.parseInt(sleepBetweenRetriesStr);
                if (configSleepBetweenRetries < 1000) {
                    configSleepBetweenRetries = 1000;
                }
            } catch (final NumberFormatException e) {
                System.out.println("NumberFormatException while parsing sleepBetweenRetries from suite file." + e);
            }
        }
        isReadParameter = true;
    }
}