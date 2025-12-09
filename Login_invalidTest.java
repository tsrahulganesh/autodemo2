
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class Login_invalidTest {

    private WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Headless mode for CI
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return new ChromeDriver(options);
    }

    // --- Common locators (adjust if your app differs) ---
    private static final String LOGIN_URL = "https://bankubt.onlinebank.com/Service/UserManager.aspx";

    private static final By USERNAME_INPUT = By
            .xpath("//input[@id='M$layout$content$PCDZ$MW2NO7V$ctl00$webInputForm$txtLoginName']");

    private static final By PASSWORD_INPUT = By
            .xpath("//input[@id='M$layout$content$PCDZ$MW2NO7V$ctl00$webInputForm$txtPassword']");

    // Prefer a class-based selector for the button; change to exact id/xpath if you
    // have one
    private static final By LOGIN_BUTTON = By
            .cssSelector("button.btn.btn-primary, input.btn.btn-primary, button[type='submit']");

    // Optional error indicators (common patterns)
    private static final By[] ERROR_INDICATORS = new By[] {
            By.cssSelector(".alert.alert-danger"),
            By.cssSelector(".validation-summary-errors"),
            By.cssSelector(".text-danger"),
            By.xpath("//*[contains(@class,'error') or contains(@class,'danger') or contains(text(),'Invalid')]")
    };

    private void performLogin(WebDriver driver, String user, String pass) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get(LOGIN_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON));

        usernameField.clear();
        passwordField.clear();

        usernameField.sendKeys(user);
        passwordField.sendKeys(pass);
        loginBtn.click();

        // Small wait for navigation or error rendering
        wait.withTimeout(Duration.ofSeconds(5));
    }

    private boolean isInvalidLogin(WebDriver driver) {
        // Criterion 1: Still on login page (URL contains UserManager.aspx)
        boolean stayedOnLogin = driver.getCurrentUrl().contains("UserManager.aspx");

        // Criterion 2: Presence of an error indicator
        boolean errorShown = false;
        for (By locator : ERROR_INDICATORS) {
            try {
                WebElement el = driver.findElement(locator);
                if (el.isDisplayed()) {
                    errorShown = true;
                    break;
                }
            } catch (NoSuchElementException ignored) {
            }
        }

        // Criterion 3: Did NOT reach dashboard/advanced page
        boolean notOnDashboard = !(driver.getCurrentUrl().contains("Advanced")
                || driver.getTitle().toLowerCase().contains("dashboard"));

        // Treat invalid login as any combination that indicates failure to navigate +
        // error or same page
        return (stayedOnLogin || errorShown) && notOnDashboard;
    }

    // -------------------------------
    // @testcase 1: wrong username
    // -------------------------------
    @Test
    void invalidLogin_wrongUsername() {
        WebDriver driver = getDriver();
        try {
            performLogin(driver, "Pawaradmin00", "Test@2025"); // wrong username, correct password

            assertTrue(
                    isInvalidLogin(driver),
                    "Expected invalid login for wrong username, but navigation suggests success.");
        } finally {
            driver.quit();
        }
    }

    // -------------------------------
    // @testcase 2: wrong password
    // -------------------------------
    @Test
    void invalidLogin_wrongPassword() {
        WebDriver driver = getDriver();
        try {
            performLogin(driver, "Pawaradmin01", "Test@2024"); // correct username, wrong password

            assertTrue(
                    isInvalidLogin(driver),
                    "Expected invalid login for wrong password, but navigation suggests success.");
        } finally {
            driver.quit();
        }
    }

    // -----------------------------------------
    // @testcase 3: blank username and password
    // -----------------------------------------
    @Test
    void invalidLogin_blankCredentials() {
        WebDriver driver = getDriver();
        try {
            performLogin(driver, "", ""); // both blank

            assertTrue(
                    isInvalidLogin(driver),
                    "Expected invalid login for blank credentials, but navigation suggests success.");
        } finally {
            driver.quit();
        }
    }
}
