package org.solution;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.apache.commons.lang3.StringUtils;
import org.opentest4j.AssertionFailedError;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PerformerExtractData extends Thread {

    private final Integer worker;
    private final Queue<DispatcherCollection> queue;
    private final String performerOutputPath;
    private Integer currentPage;
    private Integer jobNumber;

    public PerformerExtractData(Integer worker, Queue<DispatcherCollection> queue, String performerOutputPath) {
        this.worker = worker;
        this.queue = queue;
        this.performerOutputPath = performerOutputPath;
        this.currentPage = 1;
        this.jobNumber = 0;
    }

    @Override
    public void run() {
        try (Playwright playwright = Playwright.create()) {
            List<PerformerCollection> collectionList = new ArrayList<>();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50));
            Page mainPage = browser.newPage();
            mainPage.navigate("https://www.cermati.com/karir");
            mainPage.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("View All Jobs").setExact(true)).click();
            mainPage.waitForTimeout(5_000); // Wait 5 sec

            while (true) {
                if (queue.peek() == null) {
                    break;
                } else {
                    // Remove elements from the queue
                    DispatcherCollection elementQueue = queue.poll();
                    String selector = elementQueue.selector;
                    String departmentName = elementQueue.department;
                    jobNumber = jobNumber + 1;
                    try {
                        Page jobPage = navigateJob(worker, mainPage, selector);
                        PerformerCollection output = extractJob(worker, mainPage, jobPage, departmentName);
                        collectionList.add(output);
                    } catch (AssertionFailedError e) {
                        currentPage = currentPage + 1;
                        Locator nextPage = mainPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(String.valueOf(currentPage)));
                        assertThat(nextPage).isVisible();
                        nextPage.click();
                        mainPage.waitForTimeout(5_000); // Wait 5 sec
                        Page jobPage = navigateJob(worker, mainPage, selector);
                        PerformerCollection output = extractJob(worker, mainPage, jobPage, departmentName);
                        collectionList.add(output);
                    }
                }
            }

            Boolean jsonStatus = createJsonFile(performerOutputPath, collectionList);
            if (jsonStatus){
                System.out.println("Worker: " + worker + " done and completed: " + jobNumber + " scraping jobs");
            }
        }
    }

    private static Boolean createJsonFile(String performerOutputPath, List<PerformerCollection> collectionList){
        try (FileWriter writer = new FileWriter(performerOutputPath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(collectionList, new TypeToken<List<PerformerCollection>>() {}.getType(), writer);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return true;
    }

    private static PerformerCollection extractJob(Integer worker, Page mainPage, Page jobPage, String departmentName) {
        String output = "Worker: " + worker + " start extracting...";
        System.out.println(output);

        String jobTitle = jobPage.locator("//h1[@class='job-title']").textContent();
        String jobLocation = jobPage.locator("css=.c-spl-job-location__place").textContent();
        String jobType = jobPage.locator("//li[@itemprop='employmentType']").textContent();
        String jobPostedBy = "";
        try {
            Locator locJobPostedBy = jobPage.locator("div").filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Posted by.+$"))).nth(2);
            assertThat(locJobPostedBy).isVisible();
            jobPostedBy = locJobPostedBy.textContent();
            jobPostedBy = StringUtils.capitalize(jobPostedBy.toLowerCase().replace("posted by", ""));
        } catch (AssertionFailedError e) {
            jobPostedBy = "";
        }

        Locator listJobDescription = jobPage.locator("//div[@itemprop='responsibilities'] //ul //li");
        String[] jobDescription = {};
        if (listJobDescription.count() == 0) {
            String[] paraJobDescription = jobPage.locator("//div[@itemprop='responsibilities']").textContent().split("\\n");
            jobDescription = new String[paraJobDescription.length];
            for (int i = 0; i < paraJobDescription.length; i++) {
                jobDescription[i] = paraJobDescription[i]
                        .replace("\u00a0", "") // remove &nbsp
                        .replace("•", "")
                        .trim();
            }
        } else if (listJobDescription.count() > 0) {
            jobDescription = new String[listJobDescription.count()];
            for (int i = 0; i < listJobDescription.count(); i++) {
                jobDescription[i] = listJobDescription.nth(i).textContent()
                        .replace("\u00a0", "") // remove &nbsp
                        .replace("•", "")
                        .trim();
            }
        }

        String[] jobQualification = {};
        Locator listJobQualification = jobPage.locator("//div[@itemprop='qualifications'] //ul //li");
        if (listJobQualification.count() == 0) {
            String[] paraJobQualification = jobPage.locator("//div[@itemprop='qualifications']").textContent().split("\\n");
            jobQualification = new String[paraJobQualification.length];
            for (int i = 0; i < paraJobQualification.length; i++) {
                jobQualification[i] = paraJobQualification[i]
                        .replace("\u00a0", "") // remove &nbsp
                        .replace("•", "")
                        .trim();
            }
        } else if (listJobQualification.count() > 0) {
            jobQualification = new String[listJobQualification.count()];
            for (int i = 0; i < listJobQualification.count(); i++) {
                jobQualification[i] = listJobQualification.nth(i).textContent()
                        .replace("\u00a0", "") // remove &nbsp
                        .replace("•", "")
                        .trim();
            }
        }
        PerformerCollection collectionJob = new PerformerCollection(departmentName, jobTitle, jobLocation, jobDescription, jobQualification, jobType, jobPostedBy);
        jobPage.close();

//        System.out.println("Worker: " + worker + " title: " + collectionJob.title);
//        System.out.println("Worker: " + worker + " location: " + collectionJob.location);
//        System.out.println("Worker: " + worker + " description: " + Arrays.toString(collectionJob.description));
//        System.out.println("Worker: " + worker + " qualification: " + Arrays.toString(collectionJob.qualification));
//        System.out.println("Worker: " + worker + " job_type: " + collectionJob.job_type);
//        System.out.println("Worker: " + worker + " postedBy: " + collectionJob.postedBy);
//        System.out.println("Worker: " + worker + " finish extracting...");

        return collectionJob;
    }

    private static Page navigateJob(Integer worker, Page mainPage, String selector) {
        String output = "Worker: " + worker + " Navigate to Selector: " + selector;
        System.out.println(output);

        Locator jobButton = mainPage.locator("div").filter(
                new Locator.FilterOptions().setHasText(
                        Pattern.compile("^" + selector + "$")));
        assertThat(jobButton).isVisible();
        Page newPage = mainPage.waitForPopup(() -> {
            mainPage.locator("div").filter(
                    new Locator.FilterOptions().setHasText(
                            Pattern.compile("^" + selector + "$"))).getByRole(AriaRole.LINK).click();
        });

        return newPage;
    }
}
