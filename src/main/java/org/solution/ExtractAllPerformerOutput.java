package org.solution;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ExtractAllPerformerOutput {
    public static void ExtractData(Set<String> uniqueDepartments, String finalOutput, String dispPerfFilePath, String perfOutputNamingConvention) {
        perfOutputNamingConvention = perfOutputNamingConvention.replace("[worker]", "");
        String namingConvention = "(.*)" + perfOutputNamingConvention;

        // List files in the folder
        File folder = new File(dispPerfFilePath);
        File[] files = folder.listFiles((dir, name) -> name.matches(namingConvention));

        if (files != null) {
            List<PerformerCollection> allPerformerList = new ArrayList<>();
            for (File filename : files) {
                Gson gson = new Gson();
                try (Reader reader = new FileReader(filename)) {
                    Type PerformerCollectionType = new TypeToken<ArrayList<PerformerCollection>>() {
                    }.getType();
                    List<PerformerCollection> singlePerformerList = gson.fromJson(reader, PerformerCollectionType);
                    allPerformerList.addAll(singlePerformerList);

                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }

            System.out.println("---------------------------------------------------------------------------------");
            Map<String, List<DetailCollection>> departments = new HashMap<>();
            for (String department : uniqueDepartments) {
                List<DetailCollection> detailPerDepartment = new ArrayList<>();
                for (PerformerCollection item : allPerformerList) {
                    if (item.departmentName.equalsIgnoreCase(department)) {
                        DetailCollection detailOutput = new DetailCollection(item.title, item.location, item.description, item.qualification, item.job_type, item.postedBy);
                        detailPerDepartment.add(detailOutput);
                        System.out.println("Department Name: " + department +
                                " --- Title: " + detailOutput.title +
                                " --- Location: " + detailOutput.location +
                                " --- Type: " + detailOutput.job_type +
                                " --- Posted By: " + detailOutput.postedBy +
                                " --- Descriptions: " + Arrays.toString(detailOutput.description) +
                                " --- Qualification: " + Arrays.toString(detailOutput.qualification));
                    }
                }
                System.out.println("---------------------------------------------------------------------------------");
                DepartmentCollection departmentObj = new DepartmentCollection(detailPerDepartment);
                departments.put(department, departmentObj.detailCollections);
            }

            try (Writer writer = new FileWriter(finalOutput)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(departments, writer);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        try (FileReader fileReader = new FileReader(finalOutput)) {
            JsonObject jsonObject = JsonParser.parseReader(fileReader).getAsJsonObject();
            int departmentCount = 0;
            int allJobsCount = 0;

            // Iterate through the JSON object to count departments
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String departmentName = entry.getKey();
                JsonElement jobsJsonArray = entry.getValue();
                int numberOfJobs = jobsJsonArray.getAsJsonArray().size();
                System.out.println("Department Name: " + departmentName + " --- Total Jobs: " + numberOfJobs);
                departmentCount++;
                allJobsCount = allJobsCount + numberOfJobs;
            }

            // Print the total number of departments
            System.out.println("Overall Departments: " + departmentCount);
            System.out.println("Overall Jobs: " + allJobsCount);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
