package Base;

import Reporting.ExtentManager;
import Reporting.ExtentTestManager;
import Utility.Utility;
import com.relevantcodes.extentreports.LogStatus;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class CommonAPI {
    Logger LOG = LogManager.getLogger(CommonAPI.class.getName());

    protected Properties prop = Utility.loadProperties();

    String browserstackUsername = Utility.decode(prop.getProperty("browserstack.username"));
    String browserstackPassword = Utility.decode(prop.getProperty("browserstack.password"));
    String saucelabsUsername = Utility.decode(prop.getProperty("saucelabs.username"));
    String saucelabsPassword = Utility.decode(prop.getProperty("saucelabs.password"));
    String implicitWait = prop.getProperty("implicit.wait", "5");
    String maximizeBrowser = prop.getProperty("maximize.browser", "false");
    String takeScreenshot = prop.getProperty("take.screenshot", "false");

    private WebDriver driver;

    public static com.relevantcodes.extentreports.ExtentReports extent;

    @BeforeSuite
    public void extentSetup(ITestContext context) {
        ExtentManager.setOutputDirectory(context);
        extent = ExtentManager.getInstance();
    }

    @BeforeMethod
    public void startExtent(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName().toLowerCase();
        ExtentTestManager.startTest(method.getName());
        ExtentTestManager.getTest().assignCategory(className);
    }

    protected String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    @AfterMethod
    public void afterEachTestMethod(ITestResult result) {
        ExtentTestManager.getTest().getTest().setStartedTime(getTime(result.getStartMillis()));
        ExtentTestManager.getTest().getTest().setEndedTime(getTime(result.getEndMillis()));

        for (String group : result.getMethod().getGroups()) {
            ExtentTestManager.getTest().assignCategory(group);
        }

        if (result.getStatus() == 1) {
            ExtentTestManager.getTest().log(LogStatus.PASS, "Test Passed");
        } else if (result.getStatus() == 2) {
            ExtentTestManager.getTest().log(LogStatus.FAIL, getStackTrace(result.getThrowable()));
        } else if (result.getStatus() == 3) {
            ExtentTestManager.getTest().log(LogStatus.SKIP, "Test Skipped");
        }
        ExtentTestManager.endTest();
        extent.flush();
        if (takeScreenshot.equalsIgnoreCase("true")) {
            if (result.getStatus() == ITestResult.FAILURE) {
                takeScreenshot(result.getName());
            }
        }
        driver.quit();
    }

    @AfterSuite
    public void generateReport() {
        extent.close();
    }

    private Date getTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.getTime();
    }

    public void getCloudDriver(String envName, String username, String password, String os, String osVersion, String browser, String browserVersion) throws MalformedURLException {
        DesiredCapabilities cap = new DesiredCapabilities();
        cap.setCapability("os", os);
        cap.setCapability("os_version", osVersion);
        cap.setCapability("browser", browser);
        cap.setCapability("browser_version", browserVersion);
        if (envName.equalsIgnoreCase("browserstack")) {
            cap.setCapability("resolution", "1024x768");
            driver = new RemoteWebDriver(new URL("http://" + username + ":" + password + "@hub-cloud.browserstack.com:80/wd/hub"), cap);
        } else if (envName.equalsIgnoreCase("saucelabs")) {
            driver = new RemoteWebDriver(new URL("http://" + username + ":" + password + "@ondemand.saucelabs.com:80/wd/hub"), cap);
        }
    }

    public void getLocalDriver(String os, String browser) {
        if (os.equalsIgnoreCase("OS X")) {
            if (browser.equalsIgnoreCase("chrome")) {
                System.setProperty("webdriver.chrome.driver", Utility.currentDir + "/driver/mac/chromedriver101");
                driver = new ChromeDriver();
            } else if (browser.equalsIgnoreCase("firefox")) {
                System.setProperty("webdriver.gecko.driver", Utility.currentDir + "/driver/mac/geckodriver");
                driver = new FirefoxDriver();
            }
        } else if (os.equalsIgnoreCase("windows")) {
            if (browser.equalsIgnoreCase("chrome")) {
                System.setProperty("webdriver.chrome.driver", Utility.currentDir + "\\driver\\windows\\chromedriver101.exe");
                driver = new ChromeDriver();
            } else if (browser.equalsIgnoreCase("firefox")) {
                System.setProperty("webdriver.gecko.driver", Utility.currentDir + "\\driver\\windows\\geckodriver.exe");
                driver = new FirefoxDriver();
            }
        }
    }

    @Parameters({"useCloudEnv", "envName", "os", "osVersion", "browser", "browserVersion", "url"})
    @BeforeMethod
    public void setUp(@Optional("false") boolean useCloudEnv, @Optional("browserstack") String envName,
                      @Optional("mac") String os, @Optional("Sierra") String osVersion,
                      @Optional("chrome") String browser, @Optional("100") String browserVersion,
                      @Optional("http://www.google.com") String url) throws MalformedURLException {
        if (useCloudEnv) {
            if (envName.equalsIgnoreCase("browserstack")) {
                getCloudDriver(envName, browserstackUsername, browserstackPassword, os, osVersion, browser, browserVersion);
            } else if (envName.equalsIgnoreCase("saucelabs")) {
                getCloudDriver(envName, saucelabsUsername, saucelabsPassword, os, osVersion, browser, browserVersion);
            }

        } else {
            getLocalDriver(os, browser);
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Integer.parseInt(implicitWait)));
        if (maximizeBrowser.equalsIgnoreCase("true")) {
            driver.manage().window().maximize();
        }
        driver.get(url);
    }

    public WebDriver getDriver() {
        return driver;
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getElementText(WebElement element) {
        return element.getText();
    }

    public void click(WebElement element) {
        element.click();
    }

    public void type(WebElement element, String text) {
        element.sendKeys(text);
    }

    public void clear(WebElement element) {
        element.clear();
    }

    public void typeAndEnter(WebElement element, String text) {
        element.sendKeys(text, Keys.ENTER);
    }

    public void selectFromDropdown(WebElement dropdown, String option) {
        Select select = new Select(dropdown);
        try {
            select.selectByVisibleText(option);
        } catch (Exception e) {
            select.selectByValue(option);
        }
    }

    public void hoverOver(WebDriver driver, WebElement element) {
        Actions actions = new Actions(driver);
        actions.moveToElement(element).build().perform();
    }

    public void rightClick(WebDriver driver, WebElement element) {
        Actions actions = new Actions(driver);
        actions.contextClick(element).build().perform();
    }

    public void dragAndDrop(WebDriver driver, WebElement draggable, WebElement droppable) {
        Actions actions = new Actions(driver);
        actions.dragAndDrop(draggable, droppable).build().perform();
    }

    public void doubleClick(WebDriver driver, WebElement element) {
        Actions actions = new Actions(driver);
        actions.doubleClick(element).build().perform();
    }

    public void clickAndHold(WebDriver driver, WebElement element) {
        Actions actions = new Actions(driver);
        actions.clickAndHold(element).build().perform();
    }

    public void clickWithActions(WebDriver driver, WebElement element) {
        Actions actions = new Actions(driver);
        actions.click(element).build().perform();
    }

    public void typeWithActions(WebDriver driver, WebElement element) {
        Actions actions = new Actions(driver);
        actions.sendKeys(element).build().perform();
    }

    public void scrollToElementWithActions(WebDriver driver, WebElement element) {
        Actions actions = new Actions(driver);
        actions.scrollToElement(element).build().perform();
    }

    public boolean isPresent(WebElement element) {
        return element.isDisplayed();
    }

    public boolean isInteractible(WebElement element) {
        return element.isEnabled();
    }

    public boolean isChecked(WebElement element) {
        return element.isSelected();
    }

    public void scrollToView(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public void clickWithJS(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", element);
    }

    public void captureScreenshot() {
        File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(file, new File("screenshots/screenshot.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void takeScreenshot(String screenshotName) {
        DateFormat df = new SimpleDateFormat("MMddyyyyHHmma");
        Date date = new Date();
        df.format(date);

        File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(file, new File(System.getProperty("user.dir") + "\\screenshots\\" + screenshotName + " " + df.format(date) + ".jpeg"));
            LOG.info("Screenshot captured");
        } catch (Exception e) {
            String path = Utility.currentDir + "/screenshots/" + screenshotName + " " + df.format(date) + ".jpeg";
            LOG.info("Exception while taking screenshot " + e.getMessage());
        }
    }
}

