package org.apache.camel.test.infra.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.camel.test.infra.common.services.TestService;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CamelJBangInfraRunner {

    private final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    public static void main(String[] args) throws Exception {
        String testService = "artemis";
        String testServiceImplementation = "artemisvm";

//        new CamelJBangInfraRunner().run(testService, testServiceImplementation);
        new CamelJBangInfraRunner().list();
    }

    public void list() throws Exception {
        List<String> infraServices = new ArrayList<>();

        Set<URL> services = Collections.list(
                        Thread.currentThread().getContextClassLoader().getResources("META-INF/services/"))
                .stream()
                .filter(url -> url.getPath().contains("test-infra"))
                .collect(Collectors.toSet());

        List<String> serviceInterfaces = new ArrayList<>();
        for (URL url : services) {
            String jarPath = url.getPath()
                    .replace("file:", "")
                    .replace("jar:", "")
                    .replace("!/META-INF/services/", "");

            Set<String> servicesInJar = new HashSet<>();
            try (ZipFile zip = new ZipFile(jarPath)) {
                 servicesInJar = Collections.list(zip.entries()).stream().map(ZipEntry::getName)
                        .filter(name -> name.startsWith("META-INF/services/org.apache.camel.test.infra"))
                        .map(s -> s.replace("META-INF/services/", ""))
                        .collect(Collectors.toSet());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (String service : servicesInJar) {
                infraServices.addAll(List.of(new String(
                        CamelJBangInfraRunner.class.getResourceAsStream(
                                "/META-INF/services/" + service).readAllBytes())
                        .split("\n"))
                        .stream()
                        .map(s -> s.substring(s.lastIndexOf(".") + 1))
                        .collect(Collectors.toList()));
            }
        }

        System.out.println(MAPPER.writeValueAsString(new AvailableServices(infraServices)));
    }

    record AvailableServices(List<String> services) {}

    public void run(String testService, String testServiceImplementation) throws Exception {
        URL service = Collections.list(
                        Thread.currentThread().getContextClassLoader().getResources("META-INF/services/"))
                .stream()
                .filter(url -> url.getPath().contains("test-infra"))
                .filter(url -> url.getPath().toLowerCase().contains(testService.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Service " + testService + " not found"));

        String jarPath = service.getPath()
                .replace("file:", "")
                .replace("jar:", "")
                .replace("!/META-INF/services/", "");

        String serviceInterface;
        try (ZipFile zip = new ZipFile(jarPath)) {
            serviceInterface = Collections.list(zip.entries()).stream().map(ZipEntry::getName)
                    .filter(name -> name.startsWith("META-INF/services/org.apache.camel.test.infra"))
                    .map(s -> s.replace("META-INF/services/", ""))
                    .findFirst()
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] services = new String(
                CamelJBangInfraRunner.class.getResourceAsStream(
                        "/META-INF/services/" + serviceInterface).readAllBytes())
                .split("\n");

        String serviceImpl = Arrays.stream(services)
                .filter(s -> {
                    if (testServiceImplementation != null) {
                        return s.toLowerCase().contains(testServiceImplementation.toLowerCase());
                    }

                    return true;
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Service implementation " + testServiceImplementation + " not found"));

        TestService actualService = (TestService) Class.forName(serviceImpl)
                .getDeclaredConstructor(null)
                .newInstance(null);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (actualService != null) {
                try {
                    actualService.afterAll(null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }));

        actualService.beforeAll(new CamelJBangExtensionContext());

        Method[] serviceMethods = Class.forName(serviceInterface).getDeclaredMethods();
        HashMap properties = new HashMap();
        for (Method method : serviceMethods) {
            properties.put(method.getName(), method.invoke(actualService));
        }

        System.out.println(MAPPER.writeValueAsString(properties));
    }
}
