package Page;

import Base.CommonAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;


public class SignInPage extends CommonAPI {
    Logger LOG = LogManager.getLogger(SignInPage.class.getName());

    public SignInPage(WebDriver driver){
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = "#ap_email")
    private WebElement usernameemailField;

    @FindBy(css = "#ap_password")
    private WebElement passwordField;

    @FindBy(css = "#signInSubmit")
    private WebElement signInBtn;

    @FindBy(css = "")
    private WebElement errorMessage;

    //in this class I have to create all the object and the reusable steps

    public boolean checkUsernameFieldPresence(){
        boolean checkUsernameFieldPresence = isPresent(usernameemailField);
        LOG.info("username field presence: "+checkUsernameFieldPresence);
        return isPresent(usernameemailField);
    }

    public boolean checkPasswordFieldPresence(){
        boolean checkPasswordFieldPresence = isPresent(passwordField);
        LOG.info("password field presence: "+checkPasswordFieldPresence);
        return isPresent(passwordField);
    }

    public boolean checkLoginButtonPresence(){
        boolean checkLoginBtnPresence = isPresent(signInBtn);
        LOG.info("login button presence: "+checkLoginBtnPresence);
        return checkLoginBtnPresence;
    }

    public void enterUsername(String username){
        type(usernameemailField, username);
        LOG.info("username entered");
    }

    public void enterPassword(String password){
        type(passwordField, password);
        LOG.info("password entered");
    }

}
