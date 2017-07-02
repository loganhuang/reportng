//=============================================================================
// Copyright 2006-2013 Daniel W. Dyer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//=============================================================================

package org.uncommons.reportng;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.velocity.VelocityContext;
import org.testng.*;
import org.testng.xml.XmlSuite;
import org.uncommons.reportng.sender.MailSender;

/**
 * Enhanced HTML reporter for TestNG that uses Velocity templates to generate its
 * output.
 *
 * @author huangzhouwu@163.com
 */
public class HTMLReporter extends AbstractReporter {
    private static final String FRAMES_PROPERTY = "org.uncommons.reportng.frames";
    private static final String ONLY_FAILURES_PROPERTY = "org.uncommons.reportng.failures-only";

    private static final String TEMPLATES_PATH = "org/uncommons/reportng/templates/html/";
    private static final String INDEX_FILE = "index.html";
    private static final String SUITES_FILE = "suites.html";
    private static final String OVERVIEW_FILE = "overview.html";
    private static final String EMAILABLE_FILE = "emailable.html";
    private static final String EMAILABLE_DETAILS_FILE = "emailable-details.html";
    private static final String GROUPS_FILE = "groups.html";
    private static final String RESULTS_FILE = "results.html";
    private static final String OUTPUT_FILE = "output.html";
    private static final String CUSTOM_STYLE_FILE = "custom.css";

    private static final String SUITE_KEY = "suite";
    private static final String SUITES_KEY = "suites";
    private static final String GROUPS_KEY = "groups";
    private static final String RESULT_KEY = "result";
    private static final String FAILED_CONFIG_KEY = "failedConfigurations";
    private static final String SKIPPED_CONFIG_KEY = "skippedConfigurations";
    private static final String FAILED_TESTS_KEY = "failedTests";
    private static final String SKIPPED_TESTS_KEY = "skippedTests";
    private static final String PASSED_TESTS_KEY = "passedTests";
    private static final String ONLY_FAILURES_KEY = "onlyReportFailures";

    private static final String REPORT_DIRECTORY = "html";

    private static final Comparator<ITestNGMethod> METHOD_COMPARATOR = new TestMethodComparator();
    private static final Comparator<ITestResult> RESULT_COMPARATOR = new TestResultComparator();
    private static final Comparator<IClass> CLASS_COMPARATOR = new TestClassComparator();
    private static HashMap<String, HashMap<String, HashMap<String, List<DataBean>>>> suitesMap = new HashMap<>();
    private static HashMap<String, String> suitesNameMap = new HashMap<>();
    private static HashMap<String, HashMap<String, String >> resultsNameMap = new HashMap<>();

    public HTMLReporter() {
        super(TEMPLATES_PATH);
    }


    /**
     * Generates a set of HTML files that contain data about the outcome of
     * the specified test suites.
     *
     * @param suites              Data about the test runs.
     * @param outputDirectoryName The directory in which to create the report.
     */
    public void generateReport(List<XmlSuite> xmlSuites,
                               List<ISuite> suites,
                               String outputDirectoryName) {
        removeEmptyDirectories(new File(outputDirectoryName));

        boolean useFrames = System.getProperty(FRAMES_PROPERTY, "true").equals("true");
        boolean onlyFailures = System.getProperty(ONLY_FAILURES_PROPERTY, "false").equals("true");

        File outputDirectory = new File(outputDirectoryName, REPORT_DIRECTORY);
        outputDirectory.mkdirs();

        try {
            if (useFrames) {
                createFrameset(outputDirectory);
            }
            createOverview(suites, outputDirectory, !useFrames, onlyFailures);
            createEmailable(suites, outputDirectory, onlyFailures);
            createSuiteList(suites, outputDirectory, onlyFailures);
            createGroups(suites, outputDirectory);
            createResults(suites, outputDirectory, onlyFailures);
            createLog(outputDirectory, onlyFailures);
            copyResources(outputDirectory);
            createEmailableDetails(suites, outputDirectory, onlyFailures);
            //send mail if exist email.properties
            MailSender mailSender = new MailSender();
            mailSender.sendHtmlMail();
        } catch (Exception ex) {
            throw new ReportNGException("Failed generating HTML report.", ex);
        }
    }


    /**
     * Create the index file that sets up the frameset.
     *
     * @param outputDirectory The target directory for the generated file(s).
     */
    private void createFrameset(File outputDirectory) throws Exception {
        VelocityContext context = createContext();
        generateFile(new File(outputDirectory, INDEX_FILE),
                INDEX_FILE + TEMPLATE_EXTENSION,
                context);
    }


    private void createOverview(List<ISuite> suites,
                                File outputDirectory,
                                boolean isIndex,
                                boolean onlyFailures) throws Exception {
        VelocityContext context = createContext();
        context.put(SUITES_KEY, suites);
        context.put(ONLY_FAILURES_KEY, onlyFailures);
        generateFile(new File(outputDirectory, isIndex ? INDEX_FILE : OVERVIEW_FILE),
                OVERVIEW_FILE + TEMPLATE_EXTENSION,
                context);
    }

    private void createEmailable(List<ISuite> suites,
                                 File outputDirectory,
                                 boolean onlyFailures) throws Exception {
        VelocityContext context = createContext();
        context.put(SUITES_KEY, suites);
        context.put(ONLY_FAILURES_KEY, onlyFailures);
        generateFile(new File(outputDirectory, EMAILABLE_FILE),
                EMAILABLE_FILE + TEMPLATE_EXTENSION,
                context);
    }

    private void createEmailableDetails(List<ISuite> suites,
                                        File outputDirectory,
                                        boolean onlyFailures) throws Exception {
        VelocityContext context = createContext();
        context.put(SUITES_KEY, suites);
        context.put(ONLY_FAILURES_KEY, onlyFailures);

        int suiteIndex = 0;
        for (ISuite suite : suites) {
            suitesNameMap.put(suiteIndex+"",suite.getName());
            int resultIndex = 0;
            HashMap<String, HashMap<String, List<DataBean>>> resultsMap = new HashMap<>();
            HashMap<String, String> resultNameMap = new HashMap<>();
            for (ISuiteResult suiteResult : suite.getResults().values()) {
                resultNameMap.put(resultIndex+"", suiteResult.getTestContext().getName());
                HashMap<String, List<DataBean>> result = new HashMap<>();
                ITestContext testContext = suiteResult.getTestContext();
                // 把数据填入上下文
                //ITestNGMethod[] allTests = testContext.getAllTestMethods();//所有的测试方法
                //Collection<ITestNGMethod> excludeTests = testContext.getExcludedMethods();//未执行的测试方法
                IResultMap passedTests = testContext.getPassedTests();//测试通过的测试方法
                IResultMap failedTests = testContext.getFailedTests();//测试失败的测试方法
                if (failedTests.getAllMethods().size() > 0) {
                    System.setProperty("mail.result", "FAIL");
                } else {
                    System.setProperty("mail.result", "PASS");
                }

                IResultMap skippedTests = testContext.getSkippedTests();//测试跳过的测试方法

                result.put("passed", map2List(passedTests));
                result.put("failed", map2List(failedTests));
                result.put("skipped", map2List(skippedTests));

                resultsMap.put(resultIndex + "", result);
                resultIndex++;
            }
            resultsNameMap.put(suiteIndex+"",resultNameMap);
            suitesMap.put(suiteIndex + "", resultsMap);
            suiteIndex++;
        }
        context.put("suitesMap", suitesMap);
        context.put("suitesNameMap", suitesNameMap);
        context.put("resultsNameMap", resultsNameMap);
        generateFile(new File(outputDirectory, EMAILABLE_DETAILS_FILE),
                EMAILABLE_DETAILS_FILE + TEMPLATE_EXTENSION,
                context);
    }

    /**
     * Create the navigation frame.
     *
     * @param outputDirectory The target directory for the generated file(s).
     */
    private void createSuiteList(List<ISuite> suites,
                                 File outputDirectory,
                                 boolean onlyFailures) throws Exception {
        VelocityContext context = createContext();
        context.put(SUITES_KEY, suites);
        context.put(ONLY_FAILURES_KEY, onlyFailures);
        generateFile(new File(outputDirectory, SUITES_FILE),
                SUITES_FILE + TEMPLATE_EXTENSION,
                context);
    }


    /**
     * Generate a results file for each test in each suite.
     *
     * @param outputDirectory The target directory for the generated file(s).
     */
    private void createResults(List<ISuite> suites,
                               File outputDirectory,
                               boolean onlyShowFailures) throws Exception {
        int index = 1;
        for (ISuite suite : suites) {
            int index2 = 1;
            for (ISuiteResult result : suite.getResults().values()) {
                boolean failuresExist = result.getTestContext().getFailedTests().size() > 0
                        || result.getTestContext().getFailedConfigurations().size() > 0;
                if (!onlyShowFailures || failuresExist) {
                    VelocityContext context = createContext();
                    context.put(RESULT_KEY, result);
                    context.put(FAILED_CONFIG_KEY, sortByTestClass(result.getTestContext().getFailedConfigurations()));
                    context.put(SKIPPED_CONFIG_KEY, sortByTestClass(result.getTestContext().getSkippedConfigurations()));
                    context.put(FAILED_TESTS_KEY, sortByTestClass(result.getTestContext().getFailedTests()));
                    context.put(SKIPPED_TESTS_KEY, sortByTestClass(result.getTestContext().getSkippedTests()));
                    context.put(PASSED_TESTS_KEY, sortByTestClass(result.getTestContext().getPassedTests()));
                    String fileName = String.format("suite%d_test%d_%s", index, index2, RESULTS_FILE);
                    generateFile(new File(outputDirectory, fileName),
                            RESULTS_FILE + TEMPLATE_EXTENSION,
                            context);
                }
                ++index2;
            }
            ++index;
        }
    }


    /**
     * Group test methods by class and sort alphabetically.
     */
    private SortedMap<IClass, List<ITestResult>> sortByTestClass(IResultMap results) {
        SortedMap<IClass, List<ITestResult>> sortedResults = new TreeMap<IClass, List<ITestResult>>(CLASS_COMPARATOR);
        for (ITestResult result : results.getAllResults()) {
            List<ITestResult> resultsForClass = sortedResults.get(result.getTestClass());
            if (resultsForClass == null) {
                resultsForClass = new ArrayList<ITestResult>();
                sortedResults.put(result.getTestClass(), resultsForClass);
            }
            int index = Collections.binarySearch(resultsForClass, result, RESULT_COMPARATOR);
            if (index < 0) {
                index = Math.abs(index + 1);
            }
            resultsForClass.add(index, result);
        }
        return sortedResults;
    }


    /**
     * Generate a groups list for each suite.
     *
     * @param outputDirectory The target directory for the generated file(s).
     */
    private void createGroups(List<ISuite> suites,
                              File outputDirectory) throws Exception {
        int index = 1;
        for (ISuite suite : suites) {
            SortedMap<String, SortedSet<ITestNGMethod>> groups = sortGroups(suite.getMethodsByGroups());
            if (!groups.isEmpty()) {
                VelocityContext context = createContext();
                context.put(SUITE_KEY, suite);
                context.put(GROUPS_KEY, groups);
                String fileName = String.format("suite%d_%s", index, GROUPS_FILE);
                generateFile(new File(outputDirectory, fileName),
                        GROUPS_FILE + TEMPLATE_EXTENSION,
                        context);
            }
            ++index;
        }
    }


    /**
     * Generate a groups list for each suite.
     *
     * @param outputDirectory The target directory for the generated file(s).
     */
    private void createLog(File outputDirectory, boolean onlyFailures) throws Exception {
        if (!Reporter.getOutput().isEmpty()) {
            VelocityContext context = createContext();
            context.put(ONLY_FAILURES_KEY, onlyFailures);
            generateFile(new File(outputDirectory, OUTPUT_FILE),
                    OUTPUT_FILE + TEMPLATE_EXTENSION,
                    context);
        }
    }


    /**
     * Sorts groups alphabetically and also sorts methods within groups alphabetically
     * (class name first, then method name).  Also eliminates duplicate entries.
     */
    private SortedMap<String, SortedSet<ITestNGMethod>> sortGroups(Map<String, Collection<ITestNGMethod>> groups) {
        SortedMap<String, SortedSet<ITestNGMethod>> sortedGroups = new TreeMap<String, SortedSet<ITestNGMethod>>();
        for (Map.Entry<String, Collection<ITestNGMethod>> entry : groups.entrySet()) {
            SortedSet<ITestNGMethod> methods = new TreeSet<ITestNGMethod>(METHOD_COMPARATOR);
            methods.addAll(entry.getValue());
            sortedGroups.put(entry.getKey(), methods);
        }
        return sortedGroups;
    }

    // 测试结果Set<ITestResult>转为list，再按执行时间排序 ，返回list
    public List<ITestResult> sortByTime(Set<ITestResult> str) {
        List<ITestResult> list = new ArrayList<ITestResult>();
        for (ITestResult r : str) {
            list.add(r);
        }
        Collections.sort(list);
        return list;


    }

    public List<DataBean> map2List(IResultMap map) {
        // 测试结果详细数据
        List<DataBean> list = new ArrayList<DataBean>();
        map.getAllResults().size();
        for (ITestResult result : sortByTime(map.getAllResults())) {
            DataBean data = new DataBean();
            ReportNGUtils reportNGUtils = new ReportNGUtils();
            data.setTestName(result.getName());
            data.setDuration(reportNGUtils.formatDuration(result.getEndMillis()
                    - result.getStartMillis()));
            data.setParams(reportNGUtils.getArguments(result));
            data.setOutput(Reporter.getOutput(result));
            if (result.getThrowable() != null) {
                data.setStackTrace(result.getThrowable().getStackTrace());
            }
            list.add(data);
        }
        return list;
    }

    /**
     * Reads the CSS and JavaScript files from the JAR file and writes them to
     * the output directory.
     *
     * @param outputDirectory Where to put the resources.
     * @throws IOException If the resources can't be read or written.
     */
    private void copyResources(File outputDirectory) throws IOException {
        copyClasspathResource(outputDirectory, "reportng.css", "reportng.css");
        copyClasspathResource(outputDirectory, "reportng.js", "reportng.js");
        copyClasspathResource(outputDirectory, "jquery.min.js", "jquery.min.js");
        File customStylesheet = META.getStylesheetPath();

        if (customStylesheet != null) {
            if (customStylesheet.exists()) {
                copyFile(outputDirectory, customStylesheet, CUSTOM_STYLE_FILE);
            } else {
                // If not found, try to read the file as a resource on the classpath
                // useful when reportng is called by a jarred up library
                InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(customStylesheet.getPath());
                if (stream != null) {
                    copyStream(outputDirectory, stream, CUSTOM_STYLE_FILE);
                }
            }
        }
    }
}
