package com.amazon;

import static org.junit.Assert.assertTrue;

import Base.CommonAPI;
import Utility.ExcelReader;
import org.testng.Assert;
import org.testng.annotations.Test;

class BasicComponentsTest extends CommonAPI {
    ExcelReader excelReader = new ExcelReader(Utility.currentDir +"/Data/data.xlsx", "data");
    @Test
    public void checkUserLandsOnCorrectWebsite() {
        String title = getPageTitle();
        System.out.println(title);
        Assert.assertEquals(excelReader.getDataForGivenHeaderAndKey("key", "login page title"), title);
    }

}
