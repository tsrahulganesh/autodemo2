
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class Demo2Test {

    private WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        // Prefer the newer headless mode on recent Chrome
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        // Usually not needed; keep only if you know your environment needs it
        // options.addArguments("--remote-allow-origins=*");
        return new ChromeDriver(options);
    }

    @Test
    void openSignInPage() {
        WebDriver driver = getDriver();
        try {
            String url = "https://choosethechief-p2.onlinebank.com/SignIn.aspx?p_r=1";
            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            // Wait for a stable signal that the page is loaded – an element that must
            // exist.
            // Replace locator below with something specific on the SignIn page.
            // Example: a username input or a form container ID/CSS.
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("txtLoginId")));

            // Assert arrived on the right host/path
            assertTrue(driver.getCurrentUrl().startsWith("https://choosethechief-p2.onlinebank.com"),
                    "Unexpected host: " + driver.getCurrentUrl());

            // Optional: Assert title contains expected branding instead of "google"
            String title = driver.getTitle();
            assertNotNull(title);
            assertFalse(title.toLowerCase().contains("google"),
                    "Title should not contain 'google' for this site");
        } finally {
            driver.quit();
        }
    }
}
