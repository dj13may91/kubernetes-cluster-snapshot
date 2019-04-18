package com.cluster.snapshot;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.util.Config;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ClusterDetails {
    String KUBE_CONFIG = "/Users/divya.jain/IdeaProjects/Cluster snapshot/src/main/java/com/cluster/snapshot/kubeconfig";


    FileWriter fileWriter;
    PrintWriter printWriter;
    int count[] = {0};
    private boolean areAllNameSpaceRequired = true;
    public String ingressIp;
    ApiClient defaultClient = null;
    DumperOptions options = new DumperOptions();
    Yaml yaml;
    CoreV1Api api = null;

    ClusterDetails() throws IOException {
        String FILE_LOCATION = "LOGS-" + new Date() + ".html";
        fileWriter = new FileWriter(FILE_LOCATION);
        printWriter = new PrintWriter(fileWriter);
        printWriter.println(Tags.getHtmlHeadScript());
        defaultClient = Config.defaultClient();//.fromConfig(KUBE_CONFIG);
        api = new CoreV1Api(defaultClient);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        yaml = new Yaml(options);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Starting logging");
        long time = System.currentTimeMillis();
        ClusterDetails details = new ClusterDetails();
        details.getLogEnvDeploymentOfPods();
        details.printWriter.println("</body></html>");
        details.printWriter.println(Tags.createIngressIpLink(details));
        details.printWriter.close();
        time = System.currentTimeMillis() - time;
        System.out.println("Time taken : " + time + "ms");
    }

    public List<ConfigMaps> createConfigMapObjects(BufferedReader reader) throws IOException {
        String line = null;
        int count = 0;
        List<ConfigMaps> configMapsList = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (count > 0) {
                ConfigMaps configMaps = new ConfigMaps(line.split(" "));
                configMapsList.add(configMaps);
            }
            count++;
        }
        return configMapsList;
    }

    public Map<String, PodMemoryDetail> createPodMemoryObjects() throws IOException {
        BufferedReader reader = getBIS(ClusterCommands.getMemoryOfPods);
        String line = null;
        int count = 0;
        Map<String, PodMemoryDetail> podMemoryDetails = new HashMap<>();
        while ((line = reader.readLine()) != null) {
            if (count > 0) {
                PodMemoryDetail podMemoryDetail = new PodMemoryDetail(line.split(" "));
                podMemoryDetails.put(podMemoryDetail.getNAME(), podMemoryDetail);
            }
            count++;
        }
        return podMemoryDetails;
    }

    public void getLogEnvDeploymentOfPods() throws IOException {
        BufferedReader reader = getBIS(ClusterCommands.getAllPods);
        final List<PodDetails> podDetails = new ArrayList<>();
        //System.out.println("Pod objects : " + podDetails);
        try {
            podDetails.addAll(createPodObjects(reader));
        } catch (Exception e) {
            System.out.println("##### ERROR : " + e.getMessage());
            System.exit(-1);
        }
        Thread clusterDetailsThread = new Thread(() -> {
            try {
                getClusterDetails();
            } catch (Exception e) {
                System.out.println("Failed to create cluster details");
            }
        });

        ConcurrentHashMap<String, PodDetails> podLogs = new ConcurrentHashMap<>();
        Thread podLogThread = new Thread(() -> {
            try {
                podLogs.putAll(getPodLogs(podDetails));
            } catch (Exception e) {
                System.out.println("Error reading pod logs. " + e.getMessage());
            }
        });

        ConcurrentHashMap<String, String> podDeploymentLogs = new ConcurrentHashMap<>();
        Thread podDeploymentThread = new Thread(() -> {
            podDeploymentLogs.putAll(getPodDeploymentDetails(podDetails));
        });

        ConcurrentHashMap<String, String> podEnvironmentLogs = new ConcurrentHashMap<>();
        Thread podEnvThread = new Thread(() -> {
            podEnvironmentLogs.putAll(getPodEnvironment(podDetails));
        });

        ConcurrentHashMap<String, String> serviceLogs = new ConcurrentHashMap<>();
        Thread serviceThread = new Thread(() -> {
            try {
                serviceLogs.putAll(getServiceDetails());
            } catch (Exception e) {
                System.out.println("failed to create service logs");
            }
        });

        ConcurrentHashMap<String, String> configMapLogs = new ConcurrentHashMap<>();
        Thread configThread = new Thread(() -> {
            try {
                configMapLogs.putAll(getConfigMapsDetails());
            } catch (Exception e) {
                System.out.println("failed to create config map logs");
            }
        });

        ConcurrentHashMap<String, String> secretDetails = new ConcurrentHashMap<>();
        Thread secretThread = new Thread(() -> {
            try {
                secretDetails.putAll(getSecretDetails());
            } catch (Exception e) {
                System.out.println("failed to create secret logs" + e.getMessage());
            }
        });

//        BooleanHolder countBool = new BooleanHolder(true);
//        Thread count = new Thread(() -> {
//            while (countBool.value) {
//                try {
//                    System.out.println("#### ACTIVE THREAD COUNT = " + ManagementFactory.getThreadMXBean().getThreadCount());
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    System.out.println("Count interrupted");
//                }
//            }
//        });

        try {
//            count.start();
            clusterDetailsThread.start();
            Thread.sleep(300);
//          podEnvThread.start();
            Thread.sleep(300);
            podLogThread.start();
            Thread.sleep(300);
            podDeploymentThread.start();
            Thread.sleep(300);
            serviceThread.start();
            Thread.sleep(300);
            configThread.start();
            Thread.sleep(300);
            secretThread.start();
            Thread.sleep(3000);
            clusterDetailsThread.join();
            podLogThread.join();
            podDeploymentThread.join();
//          podEnvThread.join();
            serviceThread.join();
            configThread.join();
            secretThread.join();
        } catch (InterruptedException e) {
            System.out.println("thread interrupted :" + e.getMessage());
        }

        System.out.println("Creating Html file");

        createDivIndentationForPod(podLogs, "Pod Logs");
        createDivIndentation(serviceLogs, "Services");
        createDivIndentation(podDeploymentLogs, "Pod Deployment");
//      createDivIndentation(podEnvironmentLogs, "Pod Environment");
        createDivIndentation(configMapLogs, "Config Map Details");
        createDivIndentation(secretDetails, "Secret Details");

        System.out.println("Setting countBool false");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        countBool.value = false;
    }

    public List<PodDetails> createPodObjects(BufferedReader reader) throws IOException {
        String line = null;
        int count = 0;
        List<PodDetails> podDetails = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (count > 0) {
                PodDetails pod = new PodDetails(line.split(" "));
                podDetails.add(pod);
            }
            count++;
        }
        return podDetails;
    }

    public List<Services> createServiceObjects(BufferedReader reader) throws IOException {
        String line = null;
        int count = 0;
        List<Services> servicesListDetails = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (count > 0) {
                Services services = new Services(line.split(" "));
                servicesListDetails.add(services);
                if (services.getNAME().equalsIgnoreCase("cp-nginx-ingress-controller")) {
                    System.out.println("---Setting ingress ip to :" + services.getEXTERNAL_IP());
                    ingressIp = services.getEXTERNAL_IP();
                }
            }
            count++;
        }
        return servicesListDetails;
    }

    public ConcurrentHashMap<String, String> getConfigMapsDetails() throws IOException {
        BufferedReader reader = getBIS(ClusterCommands.getAllConfigMaps);
        List<ConfigMaps> configMaps = createConfigMapObjects(reader);
        ConcurrentHashMap<String, String> configMapDetails = new ConcurrentHashMap<>();
        configMaps.stream().parallel().forEach(configMap -> {
            if (StringUtils.isNotBlank(configMap.getNAME()) && (configMap.getNAMESPACE().equalsIgnoreCase("platform")) || areAllNameSpaceRequired) {
                String confiMapName = configMap.getNAME();
                String commandToDescribeConfigMap = ClusterCommands.getCommandToDescribeConfigMap(configMap);
                try {
                    configMapDetails.put(commandToDescribeConfigMap, api.readNamespacedConfigMap(confiMapName, configMap.getNAMESPACE(), null, null, null).toString());
                    System.out.println("Finished reading config map details : " + confiMapName);
                } catch (Exception e) {
                    System.out.println("--Can not read logs for config map " + confiMapName + ". FAILED to execute :" + commandToDescribeConfigMap);
                    configMapDetails.put(commandToDescribeConfigMap, "<b>Can not read config map details for " + confiMapName + " </b>");
                }

                System.out.println("Finished creating logs for config map: " + confiMapName);
            }
        });
        return configMapDetails;
    }

    public void getClusterDetails() throws IOException {
        printWriter.println("<h1>CLUSTER " + Tags.RightArrow + " " + getCommandOutput(ClusterCommands.clusterName) + "</h1>");
        printWriter.println("<h3> " + Tags.Watch + new Date() + "</h3>");
        ConcurrentHashMap<String, String> clusterDetailsMap = new ConcurrentHashMap<>();
        ClusterCommands.clusterCommandsMap.values().stream().parallel().forEach(command -> {
            try {
                clusterDetailsMap.put(command, String.valueOf(getCommandOutput(command)));
                System.out.println("Finished running: " + command);
            } catch (IOException e) {
                System.out.println("Failed to execute : " + command);
                clusterDetailsMap.put(command, "Failed to execute : " + command + " \nReason: " + e.getMessage());
            }
        });
        createDivIndentation(clusterDetailsMap, "Cluster Info");
        System.out.println("Created ClusterDetails HTML data");
    }

    private void readLogs(ConcurrentHashMap<String, String> podLogs, String podName, String getLogsCommand) {
        try {
            getCommandOutput(getLogsCommand);
            podLogs.put(getLogsCommand, String.valueOf(getCommandOutput(getLogsCommand)));
        } catch (IOException e) {
            System.out.println("--Can not read logs for " + podName + ". FAILED to execute :" + getLogsCommand);
            podLogs.put(getLogsCommand, podLogs.get(podName) + "<b>Can not read environment details for pod" + podName + " </b>");
        }
    }

    public ConcurrentHashMap<String, PodDetails> getPodLogs(List<PodDetails> podDetails) throws IOException {
        ConcurrentHashMap<String, PodDetails> podLogs = new ConcurrentHashMap<>();
        Map<String, PodMemoryDetail> podMemoryDetailMap = createPodMemoryObjects();
        podDetails.stream().parallel().forEach(pod -> {
            if (StringUtils.isNotBlank(pod.getPodName())
                    && (pod.getNAMESPACE().equalsIgnoreCase("platform") || areAllNameSpaceRequired)) {
                String podName = pod.getPodName();
                try {
                    if (podMemoryDetailMap.get(podName) != null && StringUtils.isNotBlank(podMemoryDetailMap.get(podName).getMEMORY_in_bytes()))
                        pod.UpdatePod(podMemoryDetailMap.get(podName).getMEMORY_in_bytes());
                    else
                        pod.UpdatePod("Memory usage not found");
                } catch (Exception e) {
                    System.out.println("ERROR in reading pod: " + podName + " exception: " + e.getLocalizedMessage());
                    e.printStackTrace();
                    pod.UpdatePod("Memory usage not found");
                }
                String getPodLogsCommand = ClusterCommands.getGetPodLogsCommand(pod);
                try {
                    pod.setLOGS(api.readNamespacedPodLog(podName, pod.getNAMESPACE(), null, null, null, null, null, null, 1000, true));
                    podLogs.put(getPodLogsCommand, pod);
                } catch (Exception e) {
                    System.out.println("----Can not read logs for " + podName + " ; Failed to execute :" + getPodLogsCommand);
                    pod.setLOGS("----Can not read logs for " + podName + " ------");
                    podLogs.put(getPodLogsCommand, pod);
                }
                System.out.println("Finished reading logs for pod : " + podName + " -n " + pod.getNAMESPACE());
            }
        });
        return podLogs;
    }

    public ConcurrentHashMap<String, String> getPodDeploymentDetails(List<PodDetails> podDetails) {
        ConcurrentHashMap<String, String> podDescription = new ConcurrentHashMap<>();
        podDetails.stream().parallel().forEach(pod -> {
            if (StringUtils.isNotBlank(pod.getPodName()) && (pod.getNAMESPACE().equalsIgnoreCase("platform") || areAllNameSpaceRequired)) {
                String podName = pod.getPodName();
                try {
                    podDescription.put(ClusterCommands.getGetDescribePodCommand(pod), api.readNamespacedPod(podName, pod.getNAMESPACE(), null, null, null).toString());
                } catch (Exception e) {
                    try {
                        podDescription.put(ClusterCommands.getGetDescribePodCommand(pod), api.readNamespacedPod(podName, pod.getNAMESPACE(), null, null, null).toString());
                    } catch (ApiException e1) {
                        System.out.println("Error reading deployment of: " + podName);
                        podDescription.put(ClusterCommands.getGetDescribePodCommand(pod), "<b>Can not read logs for " + podName + " </b>");
                    }
                    podDescription.put(ClusterCommands.getGetDescribePodCommand(pod), "<b>Can not read logs for " + podName + " </b>");
                }

                System.out.println("Finished describing pod for : " + podName);
            }
        });
        return podDescription;
    }

    public ConcurrentHashMap<String, String> getPodEnvironment(List<PodDetails> podDetails) {
        ConcurrentHashMap<String, String> podLogs = new ConcurrentHashMap<>();
        podDetails.parallelStream().forEach(pod -> {
            if (StringUtils.isNotBlank(pod.getPodName()) && (pod.getNAMESPACE().equalsIgnoreCase("platform") || areAllNameSpaceRequired)) {
                String podName = pod.getPodName();
                readLogs(podLogs, podName, ClusterCommands.getGetPodEnvironmentCommand(pod));
                System.out.println("Finished reading environment logs for : " + podName);
            }
        });
        return podLogs;
    }

    public ConcurrentHashMap<String, String> getServiceDetails() throws IOException {
        BufferedReader reader = getBIS(ClusterCommands.getAllServices);
        List<Services> serviceObjects = createServiceObjects(reader);
        ConcurrentHashMap<String, String> serviceDetails = new ConcurrentHashMap<>();
        serviceObjects.stream().parallel().forEach(svc -> {
            if (StringUtils.isNotBlank(svc.getNAME()) && (svc.getNAMESPACE().equalsIgnoreCase("platform") || areAllNameSpaceRequired)) {
                String svcName = svc.getNAME();
                try {
                    serviceDetails.put(ClusterCommands.getCommandToDescribeService(svc), api.readNamespacedService(svc.getNAME(), svc.getNAMESPACE(), null, null, null).toString());
                } catch (Exception e1) {
                    serviceDetails.put(ClusterCommands.getCommandToDescribeService(svc), svc.getNAME() + "<b>Can not read logs for " + svc.getNAME() + " </b>");
                }
                System.out.println("Finished Creating logs for service: " + svcName);
            }
        });
        return serviceDetails;
    }

    public ConcurrentHashMap<String, String> getSecretDetails() throws ApiException {
        ConcurrentHashMap<String, String> secretDetails = new ConcurrentHashMap<>();
        List<V1Secret> secretList = api.listSecretForAllNamespaces(null, null, null, null, null, null, null, null, null).getItems();
        for (V1Secret secret : secretList) {
            try {
                String secretName = secret.getMetadata().getName();
                String namespace = secret.getMetadata().getNamespace();
                secretDetails.put(ClusterCommands.getCommandToDescribeSecret(secretName, namespace), getSecretString(secret));
                System.out.println("Finished creating secret details for " + secretName);
            } catch (Exception e) {
                String secretName = secret.getMetadata().getName();
                String namespace = secret.getMetadata().getNamespace();
                String error = ", ##error: " + e;
                secretDetails.put(ClusterCommands.getCommandToDescribeSecret(secretName, namespace), "Failed to generate secret details" + error);
                System.out.println("Finished creating secret details for " + secretName + error);
            }
        }
        return secretDetails;
    }

    public String getSecretString(V1Secret secret) {
        StringBuilder sb = new StringBuilder();
        sb.append("    apiVersion: ").append(secret.getApiVersion()).append("\n");
        sb.append("    data: ").append(secretDataDecoded(secret.getData())).append("\n");
        sb.append("    kind: ").append(secret.getKind()).append("\n");
        sb.append("    metadata: ").append(secret.getMetadata()).append("\n");
        sb.append("    stringData: ").append(secret.getStringData()).append("\n");
        sb.append("    type: ").append(secret.getType()).append("\n");
        return sb.toString();
    }

    public String secretDataDecoded(Map<String, byte[]> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        data.forEach((key, value) -> {
            if (!key.equalsIgnoreCase("namespace"))
                sb.append("\t" + key + " : " + Base64.getEncoder().encode(value)).append("\n");
            //sb.append("\t" + key + " : " + Base64.encodeBase64String(value)).append("\n");
        });
        sb.append("\t}");
        return sb.toString();
    }

    public void createDivIndentation(ConcurrentHashMap<String, String> logs, String tag) {
        int random = count[0]++ * 1000;
        beforeDiv(tag, random);
        createHtmlData(logs);
        afterDiv(tag, random);
    }

    public void createDivIndentationForPod(ConcurrentHashMap<String, PodDetails> podLogs, String tag) {
        int random = count[0]++ * 1000;
        beforeDiv(tag, random);
        printWriter.println("<br>&nbsp;&nbsp; <b>Memory usage :</b>");
        printWriter.println("&nbsp;&nbsp; " + Tags.MemoryNotFound + "Memory usage not found");
        printWriter.println("&nbsp;&nbsp; " + Tags.LowMemoryUsage + "Low (<500Mi)");
        printWriter.println("&nbsp;&nbsp; " + Tags.MediumMemoryUsage + "Medium (500-1000Mi)");
        printWriter.println("&nbsp;&nbsp; " + Tags.HighMemoryUsage + "High (>1000Mi) <br>");
        printWriter.println("&nbsp;&nbsp; Restarts: " + Tags.Restarts);
        printWriter.println("&nbsp;&nbsp; CrashLoopBackOff: " + Tags.CrashLoopBackOff + "<br>");
        createHtmlDataForLogs(podLogs);
        afterDiv(tag, random);
    }

    public void beforeDiv(String id, int divId) {
        printWriter.println("<h3><button><a href='javascript:toggle(" + divId + ")'>" + id + "</a></button></h3>" + "<div class='off' id='id_" + divId + "'>");
        printWriter.println(getSearchScript(divId));
        printWriter.println("<input type='text' id='searchBox_" + divId + "' onkeyup='myFunction_" + divId + "()' placeholder='Search for names..'><br>");
    }

    public String getSearchScript(int divId) {
        return "<script>\n" +
                "function myFunction_" + divId + "() {\n" +
                "    var input, filter, ul, li, a, i;\n" +
                "    input = document.getElementById('searchBox_" + divId + "');\n" +
                "    filter = input.value.toUpperCase();\n" +
                "    ul = document.getElementById('id_" + divId + "');\n" +
                "    li = ul.getElementsByTagName('div');\n" +
                "    for (i = 0; i < li.length; i++) {\n" +
                "          a = li[i].getElementsByTagName(\"a\")[0];\n" +
                "          if (a.innerHTML.toUpperCase().indexOf(filter) > -1) {\n" +
                "              li[i].style.display = \"\";\n" +
                "          } else {\n" +
                "              li[i].style.display = \"none\";\n" +
                "\n" +
                "        \t}\n" +
                "    \t}\n" +
                "\t}" +
                "</script>";
    }

    public void afterDiv(String id, int divId) {
        printWriter.println("<br><button><a href='javascript:toggle(" + divId + ")'><b>COLLAPSE</b> #" + id + "</a></button><br>" + "</div>");
    }

    private void createHtmlData(ConcurrentHashMap<String, String> logDetails) {
        Map<String, String> sorted = new TreeMap<>(logDetails);
        sorted.forEach((command, logs) -> {
            printWriter.println(getTagForEachCommandLog(count[0], command, logs));
            count[0]++;
        });
    }

    private void createHtmlDataForLogs(ConcurrentHashMap<String, PodDetails> podDetails) {
        Map<String, PodDetails> sorted = new TreeMap<>(podDetails);
        sorted.forEach((podCommand, pod) -> {
            pod.setLOGS("<b>Memory usage: " + pod.getPODMEMORY() + "</b>\n" + pod.getLOGS());
            printWriter.println(getTagForEachCommandLog(count[0], podCommand, pod));
            count[0]++;
        });
    }

    public BufferedReader getBIS(String command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command.split(" "));
        builder.redirectErrorStream(true);
        Process process = builder.start();
        try {
            process.waitFor(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Interrupted running : " + command + " Error : " + e.getMessage());
        }
        InputStream is = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        return reader;
    }

    public String getTagForEachCommandLog(int id, String command, PodDetails pod) {
        return "<div>" + pod.getICON() +
                " <a href='javascript:toggle(" + id + ")' style='color:" + pod.getCOLOUR() + "'>" + command + "</a><br>" +
                "<div class='off' id='id_" + id + "'>" +
                "<pre>" + pod.getLOGS() + "</pre>" +
                "<button><a href='javascript:toggle(" + id + ")'><b>COLLAPSE</b> #" + command + "</a></button>" +
                "</div>" +
                "</div>";
    }

    public String getTagForEachCommandLog(int id, String command, String log) {
        return "<div>" +
                "<a href='javascript:toggle(" + id + ")' style='color: blue'>" + Tags.MapIcon + "  &nbsp" + command + "</a>" +
                "<div class='off' id='id_" + id + "'>" +
                "<pre>" + log + "</pre>" +
                "<button><a href='javascript:toggle(" + id + ")'><b>COLLAPSE</b> #" + command + "</a></button>" +
                "</div>" +
                "</div>";
    }

    public StringBuilder getCommandOutput(String command) throws IOException {
        BufferedReader reader = getBIS(command);
        String line = null;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output = output.append("\n" + line);
        }
        return output;
    }

}


//<a href='javascript:toggle(5)'># kubectl get nodes</a><br>
//<div class='off' id='id_5'>
//<pre> </pre>
//<a href='javascript:toggle(5)'><b>COLLAPSE</b> # kubectl get nodes</a><br><br>
//</div>