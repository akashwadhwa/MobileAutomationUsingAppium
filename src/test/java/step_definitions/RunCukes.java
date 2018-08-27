package step_definitions;

import java.io.File;
import java.util.Date;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import cucumber.api.CucumberOptions;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import jirautil.JiraUtil;
import mantisutil.ConnectMantis;
import mantisutil.MantisReport;
import testlink.api.java.client.TestLinkAPIResults;
import utilities.AppiumServerService;
import utilities.ConfigReader;
import utilities.DriverUtil;
import utilities.ExcelDataUtil;
import utilities.GlobalUtil;
import utilities.HTMLReportUtil;
import utilities.KeywordUtil;
import utilities.LogUtil;
import utilities.TestLinkUtil;

//Strict: It will try to find the pending steps or missing steps, and fail if not found.
//feature: need to provide path of feature files
//plug-in: will contain the reporting features.
//tag: for tagged hooks, for scenarios.
//monochrome: enhance the readability.
//glue: provide your step definitions class path.
//dryRun: will check if you have pending or missing steps, but will not fail the scripts.

@CucumberOptions(
		features = "classpath:features",
		plugin = {"pretty", "html:target/cucumber-html-report","json:target/cucumber.json"},
		tags = {"@TwoWheeler"}
		)

public class RunCukesTest extends AbstractTestNGCucumberTests{
	
	static ExtentReports extent;
	public static ExtentTest logger;
	String imagePath;
	String pathForLogger;
	String testCaseDescription;

	@BeforeClass
	public void onStart() {
		try {
			
			extent = new ExtentReports(System.getProperty("user.dir") + ConfigReader.getValue("extentReportPath"));
			extent.loadConfig(new File(System.getProperty("user.dir") + ConfigReader.getValue("extentConfigPath")));
			// Get all the common setting from excel file that are required for
			GlobalUtil.setCommonSettings(ExcelDataUtil.getCommonSettings());

			String browser = "";
			browser = GlobalUtil.getCommonSettings().getBrowser();
			System.out.println(browser);

			String executionEnv = "";
			executionEnv = GlobalUtil.getCommonSettings().getExecutionEnv();

			String url = "";
			url = GlobalUtil.getCommonSettings().getUrl();

			if (browser == null) {
				browser = ConfigReader.getValue("defaultBrowser");
			}

			if (executionEnv == null) {
				executionEnv = ConfigReader.getValue("defaultExecutionEnvironment");
			}

			//testlink config
			if(GlobalUtil.getCommonSettings().getManageToolName().equalsIgnoreCase("Testlink")){
				TestLinkUtil.DEV_KEY =  GlobalUtil.getCommonSettings().getTestlinkAPIKey();
				TestLinkUtil.SERVER_URL = "http://"+GlobalUtil.getCommonSettings().getTestLinkHostName()+"/testlink-1.9.16/lib/api/xmlrpc/v1/xmlrpc.php";
				TestLinkUtil.projectName = GlobalUtil.getCommonSettings().getTestlinkProjectName();
				TestLinkUtil.testPlanName = GlobalUtil.getCommonSettings().getTestlinkPlanName();
				TestLinkUtil.buildName = GlobalUtil.getCommonSettings().getBuildNumber();
				TestLinkUtil.needUpdate =  GlobalUtil.getCommonSettings().getTestlinkTool();
				GlobalUtil.testlinkapi = new TestLinkUtil();
			}

			if(GlobalUtil.getCommonSettings().getManageToolName().equalsIgnoreCase("Jira")){
				//Jira Test management config
				JiraUtil.JIRA_CYCLE_ID = GlobalUtil.getCommonSettings().getJiraCycleID();
				JiraUtil.JIRA_PROJECT_ID = GlobalUtil.getCommonSettings().getJiraProjectID();
				JiraUtil.ZEPHYR_URL = ConfigReader.getValue("zephyr_url");
				JiraUtil.ZAPI_ACCESS_KEY = ConfigReader.getValue("zapi_access_key");
				JiraUtil.ZAPI_SECRET_KEY = ConfigReader.getValue("zapi_secret_key");
				
				//remaing details will instailized when Jira is selected a bug tracking tool
			}
			else{
				GlobalUtil.getCommonSettings().setTestlinkTool("NO");
			}

			//setting up of Bug tracking "MANTIS" tool configuration
			if(GlobalUtil.getCommonSettings().getBugToolName().equalsIgnoreCase("Mantis")){
				ConnectMantis.MANTIS_URL = "http://"+GlobalUtil.getCommonSettings().getbugToolHostName()+"/bugTool/api/soap/bugToolconnect.php";
				ConnectMantis.MANTIS_USER = GlobalUtil.getCommonSettings().getbugToolUserName();
				ConnectMantis.MANTIS_PWD = GlobalUtil.getCommonSettings().getbugToolPassword();
				ConnectMantis.MANTIS_PROJET = GlobalUtil.getCommonSettings().getbugToolProjectName();
			}

			//setting up of Bug tracking "Jira" tool configuration
			if(GlobalUtil.getCommonSettings().getBugToolName().equalsIgnoreCase("Jira")){
				JiraUtil.JIRA_URL = GlobalUtil.getCommonSettings().getbugToolHostName();
				JiraUtil.USERNAME = GlobalUtil.getCommonSettings().getbugToolUserName();
				JiraUtil.PASSWORD = GlobalUtil.getCommonSettings().getbugToolPassword();
				JiraUtil.JIRA_PROJECT = GlobalUtil.getCommonSettings().getbugToolProjectName();
				GlobalUtil.jiraapi = new JiraUtil();
			}
			else{
				GlobalUtil.getCommonSettings().setbugTool("NO");
			}
			if (url == null) {
				url = ConfigReader.getValue("BASE_URL");
				GlobalUtil.getCommonSettings().setUrl(url);
			}
			LogUtil.infoLog(getClass(),
					"\n\n+===========================================================================================================+");
			LogUtil.infoLog(getClass()," Suite started" + " at " + new Date());
			LogUtil.infoLog(getClass(), "Suite Execution For Web application on environment : " + executionEnv);
			LogUtil.infoLog(getClass(), "Suite Execution For Android mobile application on version: " + GlobalUtil.getCommonSettings().getAndroidVersion());
			LogUtil.infoLog(getClass(), "Suite Execution For RestAPI on URL: " + GlobalUtil.getCommonSettings().getRestURL());
			LogUtil.infoLog(getClass(),
					"\n\n+===========================================================================================================+");
			
			GlobalUtil.setMDriver(DriverUtil.getMobileApp());

		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.errorLog(getClass(), "Common Settings not properly set may not run the scripts properly");
		}
	}

	@AfterClass
	public void onFinish() {
		GlobalUtil.getMDriver().quit();
		AppiumServerService.stopAppium();
		LogUtil.infoLog(getClass()," suite finished" + " at " + new Date());
		LogUtil.infoLog(getClass(),
				"\n\n+===========================================================================================================+");
		extent.flush();
		extent.close();
		
		KeywordUtil.onExecutionFinish();
	}
	
	@SuppressWarnings("deprecation")
	@Before
	public void beforeMobileMethod(Scenario scenario) throws Exception {

		GlobalUtil.getMDriver().startActivity("io.ionic.agentApp", "io.ionic.agentApp.MainActivity");
		testCaseDescription = scenario.getName().split("_")[1];
		RunCukesTest.logger = RunCukesTest.extent.startTest(testCaseDescription);

		LogUtil.infoLog(getClass(),
				"\n+----------------------------------------------------------------------------------------------------------------------------+");
		LogUtil.infoLog(getClass(), "Mobile Tests Started: " + scenario.getName());

		LogUtil.infoLog("Mobile Test Environment",
				"Mobile Test is executed in OS: " + GlobalUtil.getCommonSettings().getAndroidName());

	}
	
	@After
	public void afterMobileMethodSmoke(Scenario scenario) {
		String testName = scenario.getName().split("_")[0].trim();
		if (scenario.isFailed()) {
			try {
				String scFileName = "ScreenShot_" + System.currentTimeMillis();
				String screenshotFilePath = ConfigReader.getValue("screenshotPath") + "\\" + scFileName + ".png";

				imagePath = HTMLReportUtil.testFailMobileTakeScreenshot(screenshotFilePath);
				pathForLogger = RunCukesTest.logger.addScreenCapture(imagePath);
				RunCukesTest.logger.log(LogStatus.FAIL,
						HTMLReportUtil.failStringRedColor("Failed at point: " + pathForLogger) + GlobalUtil.e);

				// scenario.write("Current Page URL is " +
				// GlobalUtil.getDriver().getCurrentUrl());

				byte[] screenshot = KeywordUtil.takeMobileScreenshot(screenshotFilePath);
				scenario.embed(screenshot, "image/png");

				// report the bug
				String bugID = "Please check the Bug tool Configuration";
				if (GlobalUtil.getCommonSettings().getBugToolName().equalsIgnoreCase("Mantis")) {
					bugID = MantisReport.reporIssue(scenario.getName(), GlobalUtil.ErrorMsg, "General",
							"Automated on Android Device Version: " + GlobalUtil.getCommonSettings().getAndroidVersion()
									+ " and Build Name: " + GlobalUtil.getCommonSettings().getBuildNumber(),
							screenshotFilePath);
				}

				if (GlobalUtil.getCommonSettings().getBugToolName().equalsIgnoreCase("Jira")) {
					bugID = GlobalUtil.jiraapi.reporIssue(scenario.getName(),
							"Automated on Android Device Version: " + GlobalUtil.getCommonSettings().getAndroidVersion()
									+ ",\n Build Name: " + GlobalUtil.getCommonSettings().getBuildNumber()
									+ ". \n\n\n\n" + GlobalUtil.ErrorMsg,
							screenshotFilePath);
				}

				// updating the results in Testmangement tool
				if (GlobalUtil.getCommonSettings().getManageToolName().equalsIgnoreCase("TestLink")) {
					GlobalUtil.testlinkapi
							.updateTestLinkResult(
									testName, "Please find the BUGID in "
											+ GlobalUtil.getCommonSettings().getBugToolName() + " : " + bugID,
									TestLinkAPIResults.TEST_PASSED);
				}
				if (GlobalUtil.getCommonSettings().getManageToolName().equalsIgnoreCase("Jira")) {
					GlobalUtil.jiraapi.updateJiraTestResults(testName, "Please find the BUGID in "
							+ GlobalUtil.getCommonSettings().getBugToolName() + " : " + bugID, "Fail");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			LogUtil.infoLog("TestEnded",
					"Test has ended closing Application: " + GlobalUtil.getCommonSettings().getAndroidName());
			// updating the results in Testmangement tool
			if (GlobalUtil.getCommonSettings().getManageToolName().equalsIgnoreCase("TestLink")) {
				GlobalUtil.testlinkapi.updateTestLinkResult(testName, "This test is passed",
						TestLinkAPIResults.TEST_PASSED);
			}
			if (GlobalUtil.getCommonSettings().getManageToolName().equalsIgnoreCase("Jira")) {
				GlobalUtil.jiraapi.updateJiraTestResults(testName, "This test is passed", "Pass");
			}
		}

		// close the browsers
		//GlobalUtil.getMDriver().quit();
		GlobalUtil.getMDriver().closeApp();
		RunCukesTest.extent.endTest(RunCukesTest.logger);
	}

}
