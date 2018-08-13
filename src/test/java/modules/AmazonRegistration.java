package modules;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import AmazonPages.AccountCreationPage;
import AmazonPages.LoginPage;
import AmazonPages.ProductDetailPage;
import AmazonPages.ProductListPage;
import utilities.GlobalUtil;
import utilities.KeywordUtil;

public class AmazonRegistration {
	
	static boolean flag;
	
	public static void fillInTheMandatoryFields(HashMap<String, String> dataMap) throws Exception {
		
		KeywordUtil.inputText(AccountCreationPage.username, dataMap.get("Username"),"enter username");
		KeywordUtil.inputText(AccountCreationPage.emailID,dataMap.get("Email"),"Enter email");
		KeywordUtil.inputText(AccountCreationPage.password, dataMap.get("Password"),"Enter password");
		KeywordUtil.inputText(AccountCreationPage.passwordCheck, dataMap.get("Password"),"Repeat password");	
		
	}
	
	public static void login(HashMap<String, String> dataMap) throws Exception{
		
		KeywordUtil.inputText(AccountCreationPage.emailID, dataMap.get("Email"),"Enter the username");
		KeywordUtil.click(LoginPage.continueButton,"Click on continue button");
		KeywordUtil.inputText(AccountCreationPage.password, dataMap.get("Password"),"Enter the password");
		KeywordUtil.click(LoginPage.signInButton,"Click on Sign on Button");
	}
	
	public static void searchForAnItem(HashMap<String, String> dataMap) throws Exception{
		KeywordUtil.selectByVisibleText(ProductListPage.shopbyDropDown, dataMap.get("Category"),"Select the Category");
		KeywordUtil.inputText(ProductListPage.searchText, dataMap.get("SearchText"),"Enter the search text");
		KeywordUtil.delay(1000);
		KeywordUtil.pressEnter(ProductListPage.searchText);
		
		List<WebElement> books = KeywordUtil.getListElements(ProductListPage.bookName,"get the list of search item");
		System.out.println(books);

		int i = 1;
		while (i != 14) {
			int size = books.size();
			System.out.println(size);
		
		
			if (size == 0) {

				Thread.sleep(2000);
				KeywordUtil.scrollingToElementofAPage(ProductListPage.downPage,"Scroll down the page");
				KeywordUtil.click(ProductListPage.nextPage,"Click on next Page");
			} else if (size != 0) {
				KeywordUtil.scrollingToElementofAPage(ProductListPage.bookName,"Scroll to an Element");
				KeywordUtil.click(ProductListPage.bookName,"Select the Book");
				Thread.sleep(2000);
				flag = true;
			}
			i++;
			if (flag == true) {
				break;
			}
		}

		if(GlobalUtil.getDriver().findElement(ProductDetailPage.productTitle).getText().equalsIgnoreCase(dataMap.get("SearchText")))
			System.out.println("Select the item");

		KeywordUtil.delay(2000);

			
	}

}
