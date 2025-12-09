
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Login_invalidTest {

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

    // --- Common locators (adjust if your app differs) ---
    private static final String LOGIN_URL = "https://bankubt.onlinebank.com/Service/UserManager.aspx";

    // Stable selectors using ID ends-with (ASP.NET friendly)
    private static final By USERNAME_INPUT = By.cssSelector("input[id$='txtLoginName']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[id$='txtPassword']");

    // Robust login button selector
    private static final By LOGIN_BUTTON = By.cssSelector(
            "button[type='submit'], input[type='submit'], button.btn.btn-primary, a.btn.btn-primary"
    );

    // Optional error indicators (common patterns)
    private static final By[] ERROR_INDICATORS = new By[] {
            By.cssSelector(".alert.alert-danger"),
            By.cssSelector(".validation-summary-errors"),
            By.cssSelector(".text-danger"),
            By.xpath("//*[contains(@class,'error') or contains(@class,'danger') or contains(text(),'Invalid')]")
    };

    private WebDriverWait wait(WebDriver driver, long seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    private void waitForDocumentReady(WebDriver driver) {
        wait(driver, 15).until((ExpectedCondition<Boolean>) d ->
                ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }

    /**
     * Try default content; if inputs aren't found, iterate iframes and switch into the first one that contains the username field.
     */
    private void switchToLoginFrameIfNeeded(WebDriver driver) {
        driver.switchTo().defaultContent();

        // Try default content first
        List<WebElement> direct = driver.findElements(USERNAME_INPUT);
        if (!direct.isEmpty() && direct.get(0).isDisplayed()) {
            return;
        }

        // Try all iframes
        List<WebElement> frames = driver.findElements(By.tagName("iframe"));
        for (int i = 0; i < frames.size(); i++) {
            try {
                driver.switchTo().defaultContent();
                driver.switchTo().frame(i);
                if (!driver.findElements(USERNAME_INPUT).isEmpty()) {
                    return; // Found the right frame
                }
            } catch (WebDriverException ignored) {}
        }

        // If not found in any frame
        driver.switchTo().defaultContent();
        throw new NoSuchElementException("Username input not found in default content or any iframe.");
    }

    private void performLogin(WebDriver driver, String user, String pass) {
        driver.get(LOGIN_URL);
        waitForDocumentReady(driver);

        // Switch to frame if required
        switchToLoginFrameIfNeeded(driver);

        WebElement usernameField = wait(driver, 15)
                .until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
        WebElement passwordField = wait(driver, 15)
                .until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));
        WebElement loginBtn = wait(driver, 15)
                .until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON));

        usernameField.clear();
        passwordField.clear();
        usernameField.sendKeys(user);
        passwordField.sendKeys(pass);
        loginBtn.click();

        // Small wait for navigation or error rendering
        try {
            wait(driver, 5).until((ExpectedCondition<Boolean>) d ->
                    ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        } catch (TimeoutException ignored) {}
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
            } catch (NoSuchElementException ignored) {}
        }

        // Criterion 3: Did NOT reach dashboard/advanced page
        boolean notOnDashboard = !(driver.getCurrentUrl().contains("Advanced")
                || driver.getTitle().toLowerCase().contains("dashboard"));

        // Treat invalid login as any combination that indicates failure to navigate + error or same page
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
                    "Expected invalid login for wrong username, but navigation suggests success. URL: "
                            + driver.getCurrentUrl() + " Title: " + driver.getTitle());
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
                    "Expected invalid login for wrong password, but navigation suggests success. URL: "
                            + driver.getCurrentUrl() + " Title: " + driver.getTitle());
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
                    "Expected invalid login for blank credentials, but navigation suggests success. URL: "
                            + driver.getCurrentUrl() + " Title: " + driver.getTitle());
        } finally {
            driver.quit();
        }
    }
}
