package com.amazon;
import Base.CommonAPI;
import Page.HomePage;
import Page.SignInPage;
import Utility.ExcelReader;
import Utility.Utility;
import org.testng.Assert;
import org.testng.annotations.Test;



public class LoginTest extends CommonAPI {
    ExcelReader excelReader = new ExcelReader(Utility.currentDir + "/data/test-data.xlsx", "data");
    String username = Utility.decode(prop.getProperty("username"));
    String password = Utility.decode(prop.getProperty("password"));

    @Test
    public void loginWithValidCred() throws Exception {
        SignInPage signInPage = new SignInPage(getDriver());
        HomePage homePage = new HomePage(getDriver());

        signInPage.enterUsername(username);
        signInPage.enterPassword(password);
        signInPage.signInBtn();
        Assert.assertEquals(excelReader.getDataForGivenHeaderAndKey("key", "home page header"), homePage.getHeaderText());
    }

    @Test
    public void loginWithInvalidUsername() {
        SignInPage signInPage = new SignInPage(getDriver());

        signInPage.enterUsername("");
        signInPage.enterPassword(password);
        signInPage.signInBtn();
        Assert.assertEquals(excelReader.getDataForGivenHeaderAndKey("key", "invalid username error message"), loginPage.errorMessage());
        captureScreenshot();
    }

    //@Test
    public void loginWithInvalidPassword() {
        SignInPage loginPage = new SignInPage(getDriver());

        loginPage.enterUsername(username);
        loginPage.enterPassword("");
        loginPage.signIn();
        Assert.assertEquals(excelReader.getDataForGivenHeaderAndKey("key", "invalid password error message"), loginPage.errorMessage());
    }
}

