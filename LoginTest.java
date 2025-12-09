
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginTest {

    private WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Headless mode for CI
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--ignore-certificate-errors");
        options.setAcceptInsecureCerts(true);
        return new ChromeDriver(options);
    }

    private static final String LOGIN_URL =
            "https://bankubt.onlinebank.com/Service/UserManager.aspx";

    // Stable selectors using ID ends-with (ASP.NET friendly)
    private static final By USERNAME_INPUT = By.cssSelector("input[id$='txtLoginName']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[id$='txtPassword']");

    // Robust login button selector (covers <button>, <input> submit, with bootstrap classes)
    private static final By LOGIN_BUTTON = By.cssSelector(
            "button[type='submit'], input[type='submit'], button.btn.btn-primary, a.btn.btn-primary"
    );

    private WebDriverWait wait(WebDriver driver, long seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    private void waitForDocumentReady(WebDriver driver) {
        wait(driver, 15).until((ExpectedCondition<Boolean>) d ->
                ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

