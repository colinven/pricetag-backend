package com.pricetag.backend.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricetag.backend.dto.response.PropertyData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LookupProcess {

    private final ObjectMapper objectMapper;

    public LookupResult startProcess(String address) {

        try {
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            ProcessBuilder pb = new ProcessBuilder(
                    "scripts/venv/bin/python3",
                    "scripts/property_lookup.py",
                    address);

            Process p = pb.start();

            //Collect stdout/stderr
            StreamGobbler stdoutGobbler = new StreamGobbler(p.getInputStream(), line -> stdout.append(line).append("\n"));
            StreamGobbler stderrGobbler = new StreamGobbler(p.getErrorStream(), line -> stderr.append(line).append("\n"));

            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.execute(stdoutGobbler);
            executor.execute(stderrGobbler);

            //wait for timeout / completion
            boolean finished = p.waitFor(10, TimeUnit.SECONDS);

            executor.shutdown();

            if (finished && p.exitValue() == 0) {
                System.out.println("Lookup process finished with exit code: " + p.exitValue());
                PropertyData propertyData = objectMapper.readValue(stdout.toString(), PropertyData.class);
                return new LookupResult(propertyData, "Property data successfully retrieved.");
            } else if (finished) {
                System.out.println("Python error occurred: " + stderr);
                return new LookupResult(null, "Something went wrong. Please try again later.");
            } else {
                p.destroyForcibly();
                System.out.println("Lookup process timed out after 10 seconds.");
                return new LookupResult(null, "We couldn't find data on that address. Ensure that the address is a valid residential address.");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace(); //TODO: replace with more robust logging
            return new LookupResult(null, "Something went wrong. Please try again later.");
        }
    }
}
